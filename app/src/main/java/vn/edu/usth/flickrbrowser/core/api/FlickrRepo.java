package vn.edu.usth.flickrbrowser.core.api;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.usth.flickrbrowser.core.model.PhotoItem;

public class FlickrRepo {

    // Callback domain cho UI
    public interface CB {
        void ok(List<PhotoItem> items);
        void err(Throwable e);
    }

    private static final String TAG = "API";
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private static FlickrApi API;
    private static FlickrApi api() {
        if (API == null) {
            // dùng ApiClient.getClient() như bạn đã triển khai
            API = ApiClient.getClient().create(FlickrApi.class);
        }
        return API;
    }

    // Giữ request search đang chạy để có thể huỷ khi user gõ tiếp
    private static Call<ResponseBody> inFlightSearch;

    public static void cancelSearch() {
        if (inFlightSearch != null && !inFlightSearch.isCanceled()) {
            inFlightSearch.cancel();
        }
        inFlightSearch = null;
    }

    // ---- getRecent ----
    public static void getRecent(int page, int perPage, CB cb) {
        Map<String, String> p = new HashMap<>();
        p.put("page", String.valueOf(Math.max(1, page)));
        p.put("per_page", String.valueOf(Math.max(1, perPage)));

        api().getRecent(p).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> r) {
                try {
                    String body = r.body() != null ? r.body().string()
                            : (r.errorBody() != null ? r.errorBody().string() : "");
                    Log.d(TAG, "getRecent code=" + r.code() + " body=" + body);
                    if (r.isSuccessful()) {
                        List<PhotoItem> out = parseToPhotos(body);
                        MAIN.post(() -> cb.ok(out));
                    } else {
                        MAIN.post(() -> cb.err(new RuntimeException("HTTP " + r.code())));
                    }
                } catch (Exception e) {
                    MAIN.post(() -> cb.err(e));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "getRecent fail: " + t);
                MAIN.post(() -> cb.err(t));
            }
        });
    }

    // ---- search ----
    public static void search(String query, int page, int perPage, CB cb) {
        cancelSearch();

        Map<String, String> p = new HashMap<>();
        p.put("text", query == null ? "" : query.trim());
        p.put("page", String.valueOf(Math.max(1, page)));
        p.put("per_page", String.valueOf(Math.max(1, perPage)));

        inFlightSearch = api().search(p);
        inFlightSearch.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> r) {
                try {
                    String body = r.body() != null ? r.body().string()
                            : (r.errorBody() != null ? r.errorBody().string() : "");
                    Log.d(TAG, "search code=" + r.code() + " body=" + body);
                    if (r.isSuccessful()) {
                        List<PhotoItem> out = parseToPhotos(body);
                        MAIN.post(() -> cb.ok(out));
                    } else {
                        MAIN.post(() -> cb.err(new RuntimeException("HTTP " + r.code())));
                    }
                } catch (Exception e) {
                    MAIN.post(() -> cb.err(e));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (call.isCanceled()) {
                    Log.d(TAG, "search canceled");
                } else {
                    Log.d(TAG, "search fail: " + t);
                }
                MAIN.post(() -> cb.err(t));
            }
        });
    }

    // ---- Parser JSON -> List<PhotoItem> ----
    private static List<PhotoItem> parseToPhotos(String json) {
        List<PhotoItem> out = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(json);

            // Case 1: Flickr chuẩn (photos -> photo[])
            JSONObject photos = root.optJSONObject("photos");
            JSONArray arr = photos != null ? photos.optJSONArray("photo") : null;
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.optJSONObject(i);
                    if (o == null) continue;
                    PhotoItem p = new PhotoItem();
                    p.id     = o.optString("id", "");
                    p.title  = o.optString("title", "");
                    p.server = o.optString("server", "");
                    p.secret = o.optString("secret", "");
                    p.owner  = o.optString("owner", "");
                    out.add(p);
                }
                return out;
            }

            // Case 2: Public feed fallback (items[])
            JSONArray items = root.optJSONArray("items");
            if (items != null) {
                for (int i = 0; i < items.length(); i++) {
                    JSONObject o = items.optJSONObject(i);
                    if (o == null) continue;
                    PhotoItem p = new PhotoItem();
                    p.title = o.optString("title", "");
                    p.owner = o.optString("author", "");
                    JSONObject media = o.optJSONObject("media");
                    String url = media != null ? media.optString("m", "") : "";
                    p.thumbUrl = url;
                    p.fullUrl  = url;
                    out.add(p);
                }
            }
        } catch (Exception ignore) {
            Log.d(TAG, "parse error: " + ignore);
        }
        return out;
    }
}
