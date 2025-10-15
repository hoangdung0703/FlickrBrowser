package vn.edu.usth.flickrbrowser.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import vn.edu.usth.flickrbrowser.core.model.PhotoItem;

public final class FavoritesRepository {

    private static FavoritesRepository INSTANCE;
    private static final Object LOCK = new Object(); // KHÓA CHUNG TOÀN APP

    private static final String PREF_NAME = "favorites_pref";
    private static final String KEY_FAVORITES = "favorites_json";

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();
    private final Type listType = new TypeToken<ArrayList<PhotoItem>>(){}.getType();

    // LiveData chia sẻ duy nhất trong app
    private final MutableLiveData<List<PhotoItem>> favoritesLive = new MutableLiveData<>(new ArrayList<>());

    private FavoritesRepository(@NonNull Context appCtx) {
        prefs = appCtx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        favoritesLive.setValue(loadFromPrefs());
    }

    public static FavoritesRepository get(@NonNull Context ctx) {
        if (INSTANCE == null) {
            synchronized (FavoritesRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new FavoritesRepository(ctx.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    public LiveData<List<PhotoItem>> getFavorites() { return favoritesLive; }

    public boolean isFavorite(String id) {
        if (id == null) return false;
        List<PhotoItem> cur = favoritesLive.getValue();
        if (cur == null) return false;
        for (PhotoItem p : cur) if (id.equals(p.id)) return true;
        return false;
    }

    public void addFavorite(@NonNull PhotoItem item) {
        if (item.id == null) return;
        synchronized (LOCK) {
            List<PhotoItem> cur = loadFromPrefs();
            for (PhotoItem p : cur) if (item.id.equals(p.id)) return; // đã có
            cur.add(item);
            persist(cur);
        }
    }

    public void removeFavorite(@NonNull PhotoItem item) {
        if (item.id == null) return;
        synchronized (LOCK) {
            List<PhotoItem> cur = loadFromPrefs();
            for (Iterator<PhotoItem> it = cur.iterator(); it.hasNext();) {
                if (item.id.equals(it.next().id)) { it.remove(); break; }
            }
            persist(cur);
        }
    }

    public void toggleFavorite(@NonNull PhotoItem item) {
        synchronized (LOCK) {
            if (isFavorite(item.id)) removeFavorite(item);
            else addFavorite(item);
        }
    }

    public void refresh() {
        favoritesLive.setValue(loadFromPrefs());
    }

    // ==== private helpers ====
    private List<PhotoItem> loadFromPrefs() {
        try {
            String json = prefs.getString(KEY_FAVORITES, null);
            List<PhotoItem> list = (json != null && !json.isEmpty())
                    ? gson.fromJson(json, listType) : null;
            return (list != null) ? list : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void persist(List<PhotoItem> list) {
        try {
            // commit để tuần tự hóa ghi khi click liên tiếp
            prefs.edit().putString(KEY_FAVORITES, gson.toJson(list, listType)).commit();
        } catch (Exception ignored) {}
        // phát sự kiện mới cho toàn bộ UI đang observe
        favoritesLive.setValue(new ArrayList<>(list));
    }
}
