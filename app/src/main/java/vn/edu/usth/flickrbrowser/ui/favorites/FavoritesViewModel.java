package vn.edu.usth.flickrbrowser.ui.favorites;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class FavoritesViewModel extends ViewModel {

    // LiveData for Fragment observe → UI update
    private final MutableLiveData<List<String>> favorites = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<String>> getFavorites() {
        return favorites;
    }

    /** Add item into favorites (if it is empty) */
    public void addFavorite(String item) {
        List<String> cur = new ArrayList<>(getSafe());
        if (!cur.contains(item)) {
            cur.add(item);
            favorites.setValue(cur);
        }
    }

    /** Delete item from favorites */
    public void removeFavorite(String item) {
        List<String> cur = new ArrayList<>(getSafe());
        if (cur.remove(item)) {
            favorites.setValue(cur);
        }
    }

    /** Toggle item from favorites */
    public void toggleFavorite(String item) {
        List<String> cur = new ArrayList<>(getSafe());
        if (cur.contains(item)) {
            cur.remove(item);
        } else {
            cur.add(item);
        }
        favorites.setValue(cur);
    }

    /** Check if item is favorite */
    public boolean isFavorite(String item) {
        return getSafe().contains(item);
    }

    /** Seed mock data */
    public void seedMock(int count) {
        List<String> mock = new ArrayList<>();
        for (int i = 1; i <= count; i++) mock.add("Mock item " + i);
        favorites.setValue(mock);
    }

    private List<String> getSafe() {
        List<String> val = favorites.getValue();
        return (val != null) ? val : new ArrayList<>();
    }
}
