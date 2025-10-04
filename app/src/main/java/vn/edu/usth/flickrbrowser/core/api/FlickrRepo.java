package vn.edu.usth.flickrbrowser.core.api;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
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

    public interface CB {
        void ok(List<PhotoItem> items);
        void err(Throwable e);
    }

    private static final String TAG = "API";
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private static FlickrApi API;
    private static FlickrApi api() {
        if (API == null) {
            API = ApiClient.getClient().create(FlickrApi.class);
        }
        return API;
    }

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
                    String body = r.body() != null ? r.body().string() : (r.errorBody() != null ? r.errorBody().string() : "");
                    if (r.isSuccessful()) {
                        List<PhotoItem> out = parseToPhotos(body);
                        if (out.isEmpty()) {
                            getRecentFallback(cb);
                        } else {
                            MAIN.post(() -> cb.ok(out));
                        }
                    } else {
                        getRecentFallback(cb);
                    }
                } catch (Exception e) {
                    getRecentFallback(cb);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                getRecentFallback(cb);
            }
        });
    }

    // ---- getRecent Fallback ----
    private static void getRecentFallback(CB cb) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL("https://www.flickr.com/services/feeds/photos_public.gne?format=json&nojsoncallback=1");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                int code = conn.getResponseCode();

                if (code == 200) {
                    // ... (logic đọc dữ liệu thành công giữ nguyên) ...
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    List<PhotoItem> out = parseToPhotos(response.toString());
                    MAIN.post(() -> cb.ok(out));
                } else {
                    // --- NÂNG CẤP XỬ LÝ LỖI ---
                    // Ném ra lỗi HTTP với thông báo rõ ràng hơn
                    throw new IOException("Lỗi HTTP: " + code + " - " + conn.getResponseMessage());
                }
                // --- NÂNG CẤP XỬ LÝ LỖI ---
                // Bắt từng loại lỗi mạng cụ thể
            } catch (java.net.SocketTimeoutException e) {
                postError(cb, "Mạng quá yếu hoặc server không phản hồi.", e);
            } catch (java.net.UnknownHostException e) {
                postError(cb, "Không có kết nối Internet.", e);
            } catch (Exception e) {
                postError(cb, "Đã xảy ra lỗi khi tải dữ liệu dự phòng.", e);
            } finally {
                // Đảm bảo kết nối luôn được đóng dù thành công hay thất bại
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }).start();
    }
    private static void postError(CB cb, String userMessage, Throwable cause) {
        Log.e(TAG, userMessage, cause);
        MAIN.post(() -> cb.err(new Exception(userMessage, cause)));
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
                    String body = r.body() != null ? r.body().string() : (r.errorBody() != null ? r.errorBody().string() : "");
                    if (r.isSuccessful()) {
                        List<PhotoItem> out = parseToPhotos(body);
                        if (out.isEmpty() && query != null && !query.isEmpty()) {
                            searchFallback(query, cb);
                        } else {
                            MAIN.post(() -> cb.ok(out));
                        }
                    } else {
                        searchFallback(query, cb);
                    }
                } catch (Exception e) {
                    searchFallback(query, cb);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!call.isCanceled()) {
                    searchFallback(query, cb);
                }
            }
        });
    }

    // ---- search Fallback ----
    // ---- search Fallback ----
    private static void searchFallback(String query, CB cb) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                String tags = String.join(",", query.trim().split("\\s+"));
                String encodedTags = URLEncoder.encode(tags, "UTF-8");
                String urlStr = "https://www.flickr.com/services/feeds/photos_public.gne?format=json&nojsoncallback=1&tags=" + encodedTags;

                URL url = new URL(urlStr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                int code = conn.getResponseCode();

                if (code == 200) {
                    // ... (logic đọc dữ liệu thành công giữ nguyên) ...
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    List<PhotoItem> photos = parseToPhotos(response.toString());
                    MAIN.post(() -> cb.ok(photos));
                } else {
                    // --- NÂNG CẤP XỬ LÝ LỖI ---
                    throw new IOException("Lỗi HTTP: " + code + " - " + conn.getResponseMessage());
                }
                // --- NÂNG CẤP XỬ LÝ LỖI ---
            } catch (java.net.SocketTimeoutException e) {
                postError(cb, "Mạng quá yếu hoặc server không phản hồi.", e);
            } catch (java.net.UnknownHostException e) {
                postError(cb, "Không có kết nối Internet.", e);
            } catch (Exception e) {
                postError(cb, "Đã xảy ra lỗi khi tìm kiếm dữ liệu dự phòng.", e);
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }).start();
    }

    // ---- Parser JSON -> List<PhotoItem> ----
    private static List<PhotoItem> parseToPhotos(String json) {
        List<PhotoItem> out = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(json);

            // Case 1: API chính
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

            // Case 2: Fallback
            JSONArray items = root.optJSONArray("items");
            if (items != null) {
                for (int i = 0; i < items.length(); i++) {
                    JSONObject o = items.optJSONObject(i);
                    if (o == null) continue;
                    PhotoItem p = new PhotoItem();

                    String link = o.optString("link", "");
                    if (!link.isEmpty()) {
                        String[] parts = link.split("/");
                        if (parts.length >= 5) {
                            p.id = "fallback_" + parts[4];
                        }
                    }

                    JSONObject media = o.optJSONObject("media");
                    String url = media != null ? media.optString("m", "") : "";

                    if (p.id == null || p.id.isEmpty()) {
                        p.id = "fallback_" + Math.abs(url.hashCode());
                    }

                    p.title = o.optString("title", "");
                    p.owner = o.optString("author", "");
                    p.thumbUrl = url;
                    p.fullUrl  = url.replace("_m.jpg", "_b.jpg");
                    out.add(p);
                }
            }
        } catch (Exception ignore) {
            // Ignore parse error
        }
        return out;
    }
}