package vn.edu.usth.flickrbrowser.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.shimmer.ShimmerFrameLayout;

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

    private android.content.BroadcastReceiver favChangedReceiver;
    private boolean favReceiverRegistered = false;

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

        // Khởi tạo ViewModels
        favVM = new ViewModelProvider(requireActivity()).get(FavoritesViewModel.class);
        homeVM = new ViewModelProvider(this).get(HomeViewModel.class);

        // Ánh xạ Views
        swipeRefreshHome = view.findViewById(R.id.swipeRefreshHome);
        recyclerViewHome = view.findViewById(R.id.recyclerViewHome);
        shimmerHome = view.findViewById(R.id.shimmerHome);
        emptyViewHome = view.findViewById(R.id.emptyViewHome);

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Lắng nghe thay đổi từ ViewModel
        observeViewModel();

        // Đăng ký nhận broadcast từ Detail
        registerFavReceiver();

        // Gán sự kiện cho pull-to-refresh
        swipeRefreshHome.setOnRefreshListener(() -> homeVM.loadPhotos(true));
    }

    @Override
    public void onDestroyView() {
        unregisterFavReceiver();
        super.onDestroyView();
    }

    private void setupRecyclerView() {
        adapter = new HomeAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerViewHome.setLayoutManager(layoutManager);
        recyclerViewHome.setAdapter(adapter);

        // ... logic xử lý click không thay đổi ...
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

            // ... các hàm click khác ...
            @Override
            public void onFavoriteClick(PhotoItem photo) {
                boolean isCurrentlyFavorite = favVM.isFavorite(photo.id);
                boolean willBeFavorite = !isCurrentlyFavorite;

                // 1) Cập nhật VM + UI như bạn đang làm
                if (isCurrentlyFavorite) favVM.removeFavorite(photo);
                else                     favVM.addFavorite(photo);
                adapter.updateFavoriteStatus(photo, willBeFavorite);

                // 2) PHÁT BROADCAST để Detail (và màn khác) nhận ngay
                try {
                    LayoutInflater inflater = LayoutInflater.from(requireContext());
                    View layout = inflater.inflate(R.layout.toast_favorites, null);

                    ImageView imgIcon = layout.findViewById(R.id.imgIcon);
                    TextView tvMessage = layout.findViewById(R.id.tvMessage);

                    if (willBeFavorite) {
                        imgIcon.setImageResource(R.drawable.baseline_favorite_24);
                        tvMessage.setText("Added to Favorites");
                    } else {
                        imgIcon.setImageResource(R.drawable.outline_favorite_24);
                        tvMessage.setText("Removed from Favorites");
                    }

                    Toast toast = new Toast(requireContext().getApplicationContext());
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.setView(layout);
                    toast.show();
                } catch (Exception e) {
                    Toast.makeText(
                            requireContext(),
                            willBeFavorite ? "Added to Favorites" : "Removed from Favorites",
                            Toast.LENGTH_SHORT
                    ).show();
                }
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
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    // ===== Broadcast receiver đăng ký động =====
    private void registerFavReceiver() {
        if (favReceiverRegistered) return;

        favChangedReceiver = new android.content.BroadcastReceiver() {
            @Override
            public void onReceive(android.content.Context context, Intent intent) {
                if (!DetailActivity.ACTION_FAV_CHANGED.equals(intent.getAction())) return;

                PhotoItem p = (PhotoItem) intent.getSerializableExtra(DetailActivity.RESULT_PHOTO);
                boolean isFav = intent.getBooleanExtra(DetailActivity.RESULT_IS_FAVORITE, false);
                if (p == null) return;

                // 1) Cập nhật single source (VM)
                if (isFav) favVM.addFavorite(p); else favVM.removeFavorite(p);

                // 2) Cập nhật UI Home ngay lập tức
                if (adapter != null) adapter.updateFavoriteStatus(p, isFav);
            }
        };

        IntentFilter filter = new IntentFilter(DetailActivity.ACTION_FAV_CHANGED);
        // Android 13+ yêu cầu flag rõ ràng khi đăng ký động
        ContextCompat.registerReceiver(
                requireContext(),
                favChangedReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
        );
        favReceiverRegistered = true;
    }

    private void unregisterFavReceiver() {
        if (!favReceiverRegistered || favChangedReceiver == null) return;
        try {
            requireContext().unregisterReceiver(favChangedReceiver);
        } catch (Exception ignored) {}
        favReceiverRegistered = false;
        favChangedReceiver = null;
    }
}


