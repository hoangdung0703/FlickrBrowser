package vn.edu.usth.flickrbrowser.ui.favorites;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import vn.edu.usth.flickrbrowser.core.model.PhotoItem;

public class FavoritesViewModel extends AndroidViewModel {

    private final MutableLiveData<List<PhotoItem>> favorites = new MutableLiveData<>(new ArrayList<>());

    public FavoritesViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<PhotoItem>> getFavorites() {
        return favorites;
    }

    public void addFavorite(PhotoItem item) {
        if (item == null || item.id == null) return;
        List<PhotoItem> cur = new ArrayList<>(getSafe());
        boolean exists = false;
        for (PhotoItem p : cur) {
            if (p.id.equals(item.id)) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            cur.add(item);
            favorites.setValue(cur);
        }
    }

    public void removeFavorite(PhotoItem item) {
        if (item == null || item.id == null) return;
        List<PhotoItem> cur = new ArrayList<>(getSafe());
        cur.removeIf(p -> p.id.equals(item.id));
        favorites.setValue(cur);
    }

    public void toggleFavorite(PhotoItem item) {
        if (item == null || item.id == null) return;
        List<PhotoItem> cur = new ArrayList<>(getSafe());
        boolean exists = false;
        for (PhotoItem p : cur) {
            if (p.id.equals(item.id)) {
                exists = true;
                break;
            }
        }
        if (exists) {
            cur.removeIf(p -> p.id.equals(item.id));
        } else {
            cur.add(item);
        }
        favorites.setValue(cur);
    }

    public boolean isFavorite(String id) {
        if (id == null) return false;
        for (PhotoItem p : getSafe()) {
            if (p.id.equals(id)) return true;
        }
        return false;
    }

    private List<PhotoItem> getSafe() {
        List<PhotoItem> val = favorites.getValue();
        return (val != null) ? val : new ArrayList<>();
    }
}
