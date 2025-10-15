package vn.edu.usth.flickrbrowser.ui.favorites;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import vn.edu.usth.flickrbrowser.core.model.PhotoItem;
import vn.edu.usth.flickrbrowser.data.FavoritesRepository;

public class FavoritesViewModel extends AndroidViewModel {

    private final FavoritesRepository repo;

    public FavoritesViewModel(@NonNull Application application) {
        super(application);
        repo = FavoritesRepository.get(application);
    }

    public LiveData<List<PhotoItem>> getFavorites() { return repo.getFavorites(); }

    public void addFavorite(PhotoItem item) { repo.addFavorite(item); }

    public void removeFavorite(PhotoItem item) { repo.removeFavorite(item); }

    public void toggleFavorite(PhotoItem item) { repo.toggleFavorite(item); }

    public boolean isFavorite(String id) { return repo.isFavorite(id); }

    public void refresh() { repo.refresh(); }
}
