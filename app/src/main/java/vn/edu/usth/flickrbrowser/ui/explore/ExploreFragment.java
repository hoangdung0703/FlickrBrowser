package vn.edu.usth.flickrbrowser.ui.explore;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Random;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import vn.edu.usth.flickrbrowser.R;
import vn.edu.usth.flickrbrowser.core.api.FlickrRepo;
import vn.edu.usth.flickrbrowser.core.model.PhotoItem;
import vn.edu.usth.flickrbrowser.core.util.NetUtils;
import vn.edu.usth.flickrbrowser.ui.detail.DetailActivity;
import vn.edu.usth.flickrbrowser.ui.favorites.FavoritesViewModel;
import vn.edu.usth.flickrbrowser.ui.state.PhotoState;

public class ExploreFragment extends Fragment {

    // Views
    private SwipeRefreshLayout swipe;
    private RecyclerView rv;
    private ViewGroup shimmerGrid;
    private View emptyRoot;
    private TextView emptyText;

    // Adapter
    private ExploreAdapter adapter;

    // Paging
    private static final int PER_PAGE = 12;
    private int currentPage = 1;
    private boolean isLoading = false;

    // Favorites
    private FavoritesViewModel favVM;

    // Detail launcher
    private final ActivityResultLauncher<Intent> detailLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    PhotoItem returned = (PhotoItem) result.getData().getSerializableExtra("PHOTO_ITEM");
                    boolean isFav = result.getData().getBooleanExtra("is_favorite", false);
                    if (returned != null) {
                        if (isFav) favVM.addFavorite(returned);
                        else favVM.removeFavorite(returned);
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup parent, @Nullable Bundle b) {
        View v = inf.inflate(R.layout.fragment_explore, parent, false);

        swipe = v.findViewById(R.id.swipe);
        rv = v.findViewById(R.id.recyclerViewExplore);

        shimmerGrid = v.findViewById(R.id.shimmerGrid);
        emptyRoot = v.findViewById(R.id.emptyView);
        emptyText = emptyRoot.findViewById(R.id.emptyTitle);

        // Recycler
        GridLayoutManager glm = new GridLayoutManager(requireContext(), 2);
        rv.setLayoutManager(glm);
        adapter = new ExploreAdapter();
        rv.setAdapter(adapter);

        /** 👇 Added: cho loading item chiếm đủ 2 cột để ProgressBar nằm giữa */
        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int viewType = adapter.getItemViewType(position);
                return (viewType == ExploreAdapter.TYPE_LOADING) ? 2 : 1;
            }
        });

        // Pull-to-refresh
        swipe.setOnRefreshListener(this::refresh);

        // Endless scroll (tiệm cận đáy)
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy <= 0) return;
                int total = adapter.getItemCount();
                int last = glm.findLastVisibleItemPosition();
                if (!isLoading && last >= total - 8) {
                    loadMorePhotos();
                }
            }
        });

        // Click -> Detail
        adapter.setOnPhotoClickListener((p, position) -> {
            try {
                Intent intent = new Intent(requireContext(), DetailActivity.class);
                intent.putExtra(DetailActivity.EXTRA_PHOTOS, new ArrayList<>(adapter.getCurrentData()));
                intent.putExtra(DetailActivity.EXTRA_START_INDEX, position);
                intent.putExtra("PHOTO_ITEM", p); // fallback cũ
                intent.putExtra("is_favorite", favVM != null && favVM.isFavorite(p.id));
                detailLauncher.launch(intent);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Error opening detail: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        super.onViewCreated(v, b);
        favVM = new ViewModelProvider(requireActivity()).get(FavoritesViewModel.class);
        // Load lần đầu
        refresh();
    }

    // Pull-to-refresh / load lần đầu
    private void refresh() {
        int newPage = new Random().nextInt(10) + 1; // ví dụ 10 trang
        if (newPage == currentPage) newPage = (newPage % 10) + 1;
        currentPage = newPage;
        isLoading = true;

        swipe.setRefreshing(true);
        setState(new PhotoState.Loading());

        //  Pre-check mạng: mất mạng -> show đúng thông báo
        if (!NetUtils.hasNetwork(requireContext())) {
            isLoading = false;
            swipe.setRefreshing(false);
            setState(new PhotoState.Error(getString(R.string.no_connection))); // "No internet connection. Please try again."
            return;
        }

        FlickrRepo.getRecent(currentPage, PER_PAGE, new FlickrRepo.CB() {
            @Override
            public void ok(List<PhotoItem> items) {
                isLoading = false;
                swipe.setRefreshing(false);
                if (items == null || items.isEmpty()) {
                    setState(new PhotoState.Empty());
                } else {
                    setState(new PhotoState.Success(items));
                }
            }

            @Override
            public void err(Throwable t) {
                isLoading = false;
                swipe.setRefreshing(false);
                String msg = (t != null && t.getMessage() != null && !t.getMessage().isEmpty())
                        ? t.getMessage()
                        : getString(R.string.load_failed);
                setState(new PhotoState.Error(msg));
            }
        });
    }

    // Load thêm trang tiếp theo
    private void loadMorePhotos() {
        if (isLoading) return;
        isLoading = true;
        int nextPage = currentPage + 1;
        adapter.addLoadingFooter();


        // ✅ Check mạng cho load-more
        if (!NetUtils.hasNetwork(requireContext())) {
            isLoading = false;
            Toast.makeText(requireContext(), R.string.no_connection, Toast.LENGTH_SHORT).show();
            return;
        }

        FlickrRepo.getRecent(nextPage, PER_PAGE, new FlickrRepo.CB() {
            @Override
            public void ok(List<PhotoItem> items) {
                adapter.removeLoadingFooter();
                isLoading = false;
                if (items != null && !items.isEmpty()) {
                    adapter.addMore(items);
                    currentPage = nextPage;
                }
            }

            @Override
            public void err(Throwable t) {
                adapter.removeLoadingFooter();
                isLoading = false;
                String msg = (t != null && t.getMessage() != null && !t.getMessage().isEmpty())
                        ? t.getMessage()
                        : getString(R.string.load_more_failed);
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ---------- UI state ----------
    private void setState(@NonNull PhotoState state) {
        if (state instanceof PhotoState.Loading) {
            shimmerGrid.setVisibility(View.VISIBLE);
            startShimmers(shimmerGrid);

            rv.setVisibility(View.GONE);
            emptyRoot.setVisibility(View.GONE);
        } else if (state instanceof PhotoState.Success) {
            List<PhotoItem> items = ((PhotoState.Success) state).getItems();

            stopShimmers(shimmerGrid);
            shimmerGrid.setVisibility(View.GONE);
            emptyRoot.setVisibility(View.GONE);

            rv.setVisibility(View.VISIBLE);
            adapter.setData(items);
        } else if (state instanceof PhotoState.Empty) {
            stopShimmers(shimmerGrid);
            shimmerGrid.setVisibility(View.GONE);

            rv.setVisibility(View.GONE);
            emptyRoot.setVisibility(View.VISIBLE);
            emptyText.setText(R.string.empty_default);
        } else if (state instanceof PhotoState.Error) {
            stopShimmers(shimmerGrid);
            shimmerGrid.setVisibility(View.GONE);

            rv.setVisibility(View.GONE);
            emptyRoot.setVisibility(View.GONE);

            String msg = ((PhotoState.Error) state).getMessage();
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }

    // ---------- Shimmer helpers ----------
    private void startShimmers(View root) {
        if (root instanceof com.facebook.shimmer.ShimmerFrameLayout) {
            ((com.facebook.shimmer.ShimmerFrameLayout) root).startShimmer();
        }
        if (root instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) root;
            for (int i = 0; i < vg.getChildCount(); i++) {
                startShimmers(vg.getChildAt(i));
            }
        }
    }

    private void stopShimmers(View root) {
        if (root instanceof com.facebook.shimmer.ShimmerFrameLayout) {
            ((com.facebook.shimmer.ShimmerFrameLayout) root).stopShimmer();
        }
        if (root instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) root;
            for (int i = 0; i < vg.getChildCount(); i++) {
                stopShimmers(vg.getChildAt(i));
            }
        }
    }
}