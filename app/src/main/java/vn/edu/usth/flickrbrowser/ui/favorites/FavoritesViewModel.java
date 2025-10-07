package vn.edu.usth.flickrbrowser.ui.favorites;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import vn.edu.usth.flickrbrowser.core.model.PhotoItem;

public class FavoritesViewModel extends ViewModel {

    // LiveData cho Fragment observe → cập nhật UI
    private final MutableLiveData<List<PhotoItem>> favorites =
            new MutableLiveData<>(new ArrayList<>());

    /** Trả về LiveData cho các Fragment observe */
    public LiveData<List<PhotoItem>> getFavorites() {
        return favorites;
    }

    /** Thêm một ảnh vào favorites (nếu chưa có) */
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

    /** Xoá một ảnh khỏi favorites (nếu đang tồn tại) */
    public void removeFavorite(PhotoItem item) {
        if (item == null || item.id == null) return;
        List<PhotoItem> cur = new ArrayList<>(getSafe());
        cur.removeIf(p -> p.id.equals(item.id));
        favorites.setValue(cur);
    }

    /** Toggle ảnh trong favorites (thêm hoặc xoá tuỳ trạng thái hiện tại) */
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

    /** Kiểm tra ảnh có trong favorites hay không */
    public boolean isFavorite(String id) {
        if (id == null) return false;
        for (PhotoItem p : getSafe()) {
            if (p.id.equals(id)) return true;
        }
        return false;
    }

    /** Cập nhật danh sách khi nhận result từ DetailActivity */
    public void setFavoriteState(String id, boolean isFav, PhotoItem fullItem) {
        if (isFav) {
            if (fullItem != null) addFavorite(fullItem);
        } else {
            if (id != null) removeFavoriteById(id);
        }
    }

    /** Xoá ảnh theo id */
    private void removeFavoriteById(String id) {
        List<PhotoItem> cur = new ArrayList<>(getSafe());
        cur.removeIf(p -> p.id.equals(id));
        favorites.setValue(cur);
    }

    /** Hàm tiện ích: đảm bảo không null */
    private List<PhotoItem> getSafe() {
        List<PhotoItem> val = favorites.getValue();
        return (val != null) ? val : new ArrayList<>();
    }
}
