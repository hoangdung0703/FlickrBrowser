package vn.edu.usth.flickrbrowser.ui.favorites;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import vn.edu.usth.flickrbrowser.core.model.PhotoItem;

public class FavoritesViewModel extends AndroidViewModel {

    private final MutableLiveData<List<PhotoItem>> favorites = new MutableLiveData<>(new ArrayList<>());

    // ==== PERSISTENCE ====
    private static final String PREF_NAME = "favorites_pref";
    private static final String KEY_FAVORITES = "favorites_json";
    private final SharedPreferences prefs;
    private final Gson gson = new Gson();
    private final Type listType = new TypeToken<ArrayList<PhotoItem>>(){}.getType();

    public FavoritesViewModel(@NonNull Application application) {
        super(application);
        prefs = application.getSharedPreferences(PREF_NAME, Application.MODE_PRIVATE);
        loadFavorites();
    }

    public LiveData<List<PhotoItem>> getFavorites() {
        return favorites;
    }

    public void addFavorite(PhotoItem item) {
        if (item == null || item.id == null) return;
        List<PhotoItem> cur = new ArrayList<>(getSafe());
        for (PhotoItem p : cur) {
            if (p.id.equals(item.id)) {
                return; // đã có
            }
        }
        cur.add(item);                // THÊM FULL OBJECT
        favorites.setValue(cur);
        saveFavorites(cur);           // LƯU XUỐNG DISK
    }

    public void removeFavorite(PhotoItem item) {
        if (item == null || item.id == null) return;
        List<PhotoItem> cur = new ArrayList<>(getSafe());
        cur.removeIf(p -> p.id.equals(item.id));
        favorites.setValue(cur);
        saveFavorites(cur);           // LƯU XUỐNG DISK
    }

    public void toggleFavorite(PhotoItem item) {
        if (item == null || item.id == null) return;
        List<PhotoItem> cur = new ArrayList<>(getSafe());
        int idx = -1;
        for (int i = 0; i < cur.size(); i++) {
            if (cur.get(i).id.equals(item.id)) { idx = i; break; }
        }
        if (idx >= 0) cur.remove(idx);
        else cur.add(item);           // THÊM FULL OBJECT
        favorites.setValue(cur);
        saveFavorites(cur);           // LƯU XUỐNG DISK
    }

    public boolean isFavorite(String id) {
        if (id == null) return false;
        for (PhotoItem p : getSafe()) {
            if (p.id.equals(id)) return true;
        }
        return false;
    }

    public void refresh() {
        String json = prefs.getString(KEY_FAVORITES, null);
        List<PhotoItem> list = null;
        if (json != null && !json.isEmpty()) {
            list = gson.fromJson(json, listType);
        }
        if (list == null) list = new ArrayList<>();
        favorites.setValue(list);
    }

    private List<PhotoItem> getSafe() {
        List<PhotoItem> val = favorites.getValue();
        return (val != null) ? val : new ArrayList<>();
    }

    private void saveFavorites(List<PhotoItem> list) {
        String json = gson.toJson(list, listType);
        prefs.edit().putString(KEY_FAVORITES, json).apply();
    }

    private void loadFavorites() {
        String json = prefs.getString(KEY_FAVORITES, null);
        if (json != null && !json.isEmpty()) {
            List<PhotoItem> list = gson.fromJson(json, listType);
            if (list != null) {
                favorites.setValue(list);
            }
        }
    }

}
