package vn.edu.usth.flickrbrowser.ui.home;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

import vn.edu.usth.flickrbrowser.R;
import vn.edu.usth.flickrbrowser.core.model.PhotoItem;
import vn.edu.usth.flickrbrowser.ui.detail.DetailActivity;
import vn.edu.usth.flickrbrowser.ui.favorites.FavoritesViewModel;
import vn.edu.usth.flickrbrowser.ui.state.PhotoState;

public class HomeFragment extends Fragment {

    // Views
    private SwipeRefreshLayout swipeRefreshHome;
    private RecyclerView recyclerViewHome;
    private ShimmerFrameLayout shimmerHome;
    private View emptyViewHome;

    // Adapters and ViewModels
    private HomeAdapter adapter;
    private FavoritesViewModel favVM;
    private HomeViewModel homeVM;
    
    // Flag để chỉ restore scroll position 1 lần
    private boolean hasRestoredPosition = false;

    // BroadcastReceiver để scroll to top + refresh khi tap Home lại
    private final BroadcastReceiver homeReselectedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("HomeFragment", "Received HOME_RESELECTED broadcast");
            scrollToTopAndRefresh();
        }
    };

    private final ActivityResultLauncher<Intent> detailLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                // ... logic xử lý kết quả từ DetailActivity không thay đổi ...
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar tb = view.findViewById(R.id.toolbar);
        setupFlickrToolbar(tb);

        // Khởi tạo ViewModels - Dùng Activity scope để giữ data khi switch tabs
        favVM = new ViewModelProvider(requireActivity()).get(FavoritesViewModel.class);
        homeVM = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        // Ánh xạ Views
        swipeRefreshHome = view.findViewById(R.id.swipeRefreshHome);
        recyclerViewHome = view.findViewById(R.id.recyclerViewHome);
        shimmerHome = view.findViewById(R.id.shimmerHome);
        emptyViewHome = view.findViewById(R.id.emptyViewHome);

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Lắng nghe thay đổi từ ViewModel
        observeViewModel();

        // Gán sự kiện cho pull-to-refresh với theming
        swipeRefreshHome.setColorSchemeResources(
            R.color.md_theme_primary,
            R.color.md_theme_secondary
        );
        swipeRefreshHome.setProgressBackgroundColorSchemeResource(R.color.md_theme_surface);
        swipeRefreshHome.setOnRefreshListener(() -> homeVM.loadPhotos(true));
        
        // Trigger current state to restore UI when returning to fragment
        PhotoState currentState = homeVM.photosState.getValue();
        if (currentState != null) {
            swipeRefreshHome.setRefreshing(false);
            if (currentState instanceof PhotoState.Success) {
                setSuccessState(((PhotoState.Success) currentState).getItems());
            }
        }
    }

    private void setupFlickrToolbar(MaterialToolbar tb) {
        // Tắt title mặc định
        tb.setTitle(null);
        tb.getMenu().clear();
        tb.setElevation(0f);

        // Inflate view_toolbar_flickr.xml
        View brand = LayoutInflater.from(tb.getContext())
                .inflate(R.layout.view_toolbar_flickr, tb, false);

        // Thêm vào toolbar ở vị trí START, canh giữa theo chiều dọc
        Toolbar.LayoutParams lp = new Toolbar.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.START | Gravity.CENTER_VERTICAL
        );
        tb.addView(brand, lp);
    }
    private void setupRecyclerView() {
        adapter = new HomeAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerViewHome.setLayoutManager(layoutManager);
        recyclerViewHome.setAdapter(adapter);

        adapter.setOnPhotoInteractionListener(new HomeAdapter.OnPhotoInteractionListener() {
            @Override
            public void onPhotoClick(PhotoItem photo, int position) {
                try {
                    Intent intent = new Intent(requireContext(), DetailActivity.class);
                    intent.putExtra(DetailActivity.EXTRA_PHOTOS, adapter.getCurrentData());
                    intent.putExtra(DetailActivity.EXTRA_START_INDEX, position);
                    intent.putExtra("is_favorite", favVM.isFavorite(photo.id));
                    detailLauncher.launch(intent);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Error opening detail", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFavoriteClick(PhotoItem photo) {
                boolean willBeFavorite = !favVM.isFavorite(photo.id);

                if (willBeFavorite) favVM.addFavorite(photo);
                else                favVM.removeFavorite(photo);

                adapter.updateFavoriteStatus(photo, willBeFavorite);

                // broadcast cho Detail / nơi khác
                try {
                    Intent i = new Intent(DetailActivity.ACTION_FAV_CHANGED);
                    i.setPackage(requireContext().getPackageName());
                    i.putExtra(DetailActivity.RESULT_PHOTO, photo);
                    i.putExtra(DetailActivity.RESULT_IS_FAVORITE, willBeFavorite);
                    requireContext().sendBroadcast(i);
                } catch (Exception ignored) {}
            }

            @Override
            public void onShareClick(PhotoItem photo) {
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, photo.getPageUrl());
                    startActivity(Intent.createChooser(shareIntent, "Share photo via"));
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Cannot share photo", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onOwnerClick(PhotoItem photo) {
                if (photo.getPageUrl() != null && !photo.getPageUrl().isEmpty()) {
                    try {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(photo.getPageUrl()));
                        startActivity(browserIntent);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Cannot open link", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        // Cuộn vô tận sẽ gọi hàm trong ViewModel
        recyclerViewHome.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy <= 0) return;
                int totalItemCount = layoutManager.getItemCount();
                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                if (lastVisibleItem >= totalItemCount - 3) { // Tải thêm khi còn 3 item
                    homeVM.loadMorePhotos();
                }
            }
        });
    }

    private void observeViewModel() {
        homeVM.photosState.observe(getViewLifecycleOwner(), state -> {
            swipeRefreshHome.setRefreshing(false); // Luôn tắt icon refresh
            if (state instanceof PhotoState.Loading) {
                setLoadingState();
            } else if (state instanceof PhotoState.Success) {
                setSuccessState(((PhotoState.Success) state).getItems());
            } else if (state instanceof PhotoState.Empty) {
                setEmptyState();
            } else if (state instanceof PhotoState.Error) {
                setErrorState(((PhotoState.Error) state).getMessage());
            }
        });
    }

    // Các hàm quản lý UI chỉ cập nhật View
    private void setLoadingState() {
        shimmerHome.setVisibility(View.VISIBLE);
        shimmerHome.startShimmer();
        recyclerViewHome.setVisibility(View.GONE);
        emptyViewHome.setVisibility(View.GONE);
    }

    private void setSuccessState(List<PhotoItem> items) {
        shimmerHome.stopShimmer();
        shimmerHome.setVisibility(View.GONE);
        emptyViewHome.setVisibility(View.GONE);
        recyclerViewHome.setVisibility(View.VISIBLE);
        adapter.setData(items, favVM.getFavorites().getValue());
        
        // Chỉ restore scroll position 1 lần khi mới vào fragment
        if (!hasRestoredPosition) {
            recyclerViewHome.post(() -> {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerViewHome.getLayoutManager();
                if (layoutManager != null && homeVM != null) {
                    int position = homeVM.getScrollPosition();
                    int offset = homeVM.getScrollOffset();
                    if (position > 0 || offset != 0) {
                        layoutManager.scrollToPositionWithOffset(position, offset);
                    }
                }
            });
            hasRestoredPosition = true;
        }
    }

    private void setEmptyState() {
        shimmerHome.stopShimmer();
        shimmerHome.setVisibility(View.GONE);
        recyclerViewHome.setVisibility(View.GONE);
        emptyViewHome.setVisibility(View.VISIBLE);
    }

    private void setErrorState(String message) {
        shimmerHome.stopShimmer();
        shimmerHome.setVisibility(View.GONE);
        recyclerViewHome.setVisibility(View.GONE);
        emptyViewHome.setVisibility(View.GONE);
        if (getContext() != null && message != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("HomeFragment", "onResume - Registering receiver");
        // Register receiver để lắng nghe khi tap Home lại
        if (getContext() != null) {
            IntentFilter filter = new IntentFilter("ACTION_HOME_RESELECTED");
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                requireContext().registerReceiver(homeReselectedReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                requireContext().registerReceiver(homeReselectedReceiver, filter);
            }
            Log.d("HomeFragment", "Receiver registered successfully");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Save scroll position when leaving fragment
        if (recyclerViewHome != null && homeVM != null) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerViewHome.getLayoutManager();
            if (layoutManager != null) {
                int position = layoutManager.findFirstVisibleItemPosition();
                View firstVisibleView = layoutManager.findViewByPosition(position);
                int offset = (firstVisibleView == null) ? 0 : firstVisibleView.getTop();
                homeVM.saveScrollPosition(position, offset);
            }
        }
        
        // Reset flag để lần sau vào lại sẽ restore position
        hasRestoredPosition = false;
        
        // Unregister receiver
        try {
            if (getContext() != null) {
                requireContext().unregisterReceiver(homeReselectedReceiver);
            }
        } catch (IllegalArgumentException ignored) {
            // Receiver chưa được register
        }
    }
    
    private void scrollToTopAndRefresh() {
        Log.d("HomeFragment", "scrollToTopAndRefresh called");
        if (recyclerViewHome != null) {
            Log.d("HomeFragment", "Scrolling to top...");
            // Scroll to top smooth
            recyclerViewHome.smoothScrollToPosition(0);
            
            // Reset scroll position trong ViewModel
            if (homeVM != null) {
                homeVM.saveScrollPosition(0, 0);
            }
            
            // Reset flag vì đã refresh
            hasRestoredPosition = true;
            
            // Trigger refresh
            if (swipeRefreshHome != null) {
                Log.d("HomeFragment", "Triggering refresh...");
                swipeRefreshHome.setRefreshing(true);
                homeVM.loadPhotos(true);
            }
        } else {
            Log.e("HomeFragment", "recyclerViewHome is null!");
        }
    }
}
