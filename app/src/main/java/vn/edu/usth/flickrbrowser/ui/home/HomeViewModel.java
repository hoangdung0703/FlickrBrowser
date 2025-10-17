package vn.edu.usth.flickrbrowser.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import vn.edu.usth.flickrbrowser.core.api.FlickrRepo;
import vn.edu.usth.flickrbrowser.core.model.PhotoItem;
import vn.edu.usth.flickrbrowser.ui.state.PhotoState;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<PhotoState> _photosState = new MutableLiveData<>();
    public LiveData<PhotoState> photosState = _photosState;

    private final List<PhotoItem> photoList = new ArrayList<>();
    private int currentPage = 1;
    private static final int PER_PAGE = 10;
    private boolean isLoading = false;
    private boolean endReached = false;
    
    // Save scroll position
    private int scrollPosition = 0;
    private int scrollOffset = 0;
    
    public void saveScrollPosition(int position, int offset) {
        this.scrollPosition = position;
        this.scrollOffset = offset;
    }
    
    public int getScrollPosition() {
        return scrollPosition;
    }
    
    public int getScrollOffset() {
        return scrollOffset;
    }

    public HomeViewModel() {
        // Chỉ tải dữ liệu lần đầu tiên khi ViewModel được tạo và danh sách trống
        if (photoList.isEmpty()) {
            loadPhotos(false);
        } else {
            // Nếu đã có data, emit lại để fragment hiển thị và restore scroll
            _photosState.setValue(new PhotoState.Success(new ArrayList<>(photoList)));
        }
    }

    public void loadPhotos(boolean isRefreshing) {
        if (isLoading && !isRefreshing) return; // Tránh gọi lại khi đang tải
        isLoading = true;

        if (isRefreshing) {
            currentPage = 1;
            endReached = false;
        }

        // Chỉ hiển thị Shimmer khi danh sách hoàn toàn trống
        if (photoList.isEmpty()) {
            _photosState.setValue(new PhotoState.Loading());
        }

        int pageToLoad = isRefreshing ? new Random().nextInt(10) + 1 : currentPage;

        FlickrRepo.getRecent(pageToLoad, PER_PAGE, new FlickrRepo.CB() {
            @Override
            public void ok(List<PhotoItem> items) {
                isLoading = false;
                if (isRefreshing) {
                    photoList.clear();
                }

                if (items != null && !items.isEmpty()) {
                    photoList.addAll(items);
                    currentPage = pageToLoad + 1; // Cập nhật trang tiếp theo
                } else {
                    endReached = true;
                }

                if (photoList.isEmpty()) {
                    _photosState.setValue(new PhotoState.Empty());
                } else {
                    // Gửi đi một bản sao của danh sách để UI cập nhật
                    _photosState.setValue(new PhotoState.Success(new ArrayList<>(photoList)));
                }
            }

            @Override
            public void err(Throwable t) {
                isLoading = false;
                _photosState.setValue(new PhotoState.Error(t != null ? t.getMessage() : "Unknown error"));
            }
        });
    }

    public void loadMorePhotos() {
        if (isLoading || endReached) return;
        isLoading = true;

        FlickrRepo.getRecent(currentPage, PER_PAGE, new FlickrRepo.CB() {
            @Override
            public void ok(List<PhotoItem> items) {
                isLoading = false;
                if (items != null && !items.isEmpty()) {
                    photoList.addAll(items);
                    _photosState.setValue(new PhotoState.Success(new ArrayList<>(photoList)));
                    currentPage++;
                } else {
                    endReached = true;
                }
            }

            @Override
            public void err(Throwable t) {
                isLoading = false;
                // Có thể gửi một event lỗi riêng để Fragment hiển thị Toast
            }
        });
    }
}

