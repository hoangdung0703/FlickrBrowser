package vn.edu.usth.flickrbrowser.ui.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import java.util.List;

import vn.edu.usth.flickrbrowser.R;
import vn.edu.usth.flickrbrowser.core.api.FlickrRepo;
import vn.edu.usth.flickrbrowser.core.model.PhotoItem;
import vn.edu.usth.flickrbrowser.databinding.FragmentSearchBinding;
import vn.edu.usth.flickrbrowser.ui.common.GridSpacingDecoration;
import vn.edu.usth.flickrbrowser.ui.search.PhotosAdapter;
import vn.edu.usth.flickrbrowser.ui.state.PhotoState;

public class SearchFragment extends Fragment {
    private FragmentSearchBinding binding;
    private PhotosAdapter adapter;
    private int page = 1;
    private final int perPage = 24;
    private boolean isLoading = false;
    private boolean endReached = false;
    private String currentQuery = "";
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        adapter = new PhotosAdapter(item -> {
            android.content.Intent i = new android.content.Intent(requireContext(), vn.edu.usth.flickrbrowser.ui.detail.DetailActivity.class);
            i.putExtra("PHOTO_ITEM", item);
            startActivity(i);
        });
        binding.rvPhotos.setAdapter(adapter);

        // 1) AppBar title
        binding.topAppBar.setTitle(R.string.search_hint);

        // 2) RecyclerView grid 2 cột
        int span = 2;
        GridLayoutManager glm = new GridLayoutManager(getContext(), span);
        binding.rvPhotos.setLayoutManager(glm);

        // 3) Spacing theo token
        int spacingPx = getResources().getDimensionPixelSize(R.dimen.spacing_m);
        binding.rvPhotos.addItemDecoration(new GridSpacingDecoration(span, spacingPx, true));

        // 4) Infinite scroll listener
        final int visibleThreshold = 6; // tải thêm khi còn cách đáy N item
        binding.rvPhotos.addOnScrollListener(new androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull androidx.recyclerview.widget.RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);
                if (dy <= 0) return; // chỉ quan tâm scroll xuống
                int total = glm.getItemCount();
                int lastVisible = glm.findLastVisibleItemPosition();
                if (!isLoading && !endReached && !currentQuery.isEmpty() && lastVisible >= total - visibleThreshold) {
                    loadMore();
                }
            }
        });

        // 5) Pull-to-refresh ( fix loi keo lên xoay mãi)
        binding.swipeRefresh.setOnRefreshListener(() -> {
            String q = binding.edtQuery.getText() != null ? binding.edtQuery.getText().toString() : "";
            doSearch(q, true);
        });

        // Search action on keyboard
        binding.edtQuery.setOnEditorActionListener((v, actionId, ev) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                doSearch(v.getText() != null ? v.getText().toString() : "");
                return true;
            }
            return false;
        });

        // Initial empty state
        setState(new PhotoState.Empty());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;  // tránh leak
    }

    private void setState(@NonNull PhotoState state) {
        if (state instanceof PhotoState.Loading) {
            binding.shimmerGrid.getRoot().setVisibility(View.VISIBLE);
            startShimmers(binding.shimmerGrid.getRoot());

            binding.rvPhotos.setVisibility(View.GONE);
            if (binding.emptyView != null) binding.emptyView.getRoot().setVisibility(View.GONE);
        }
        else if (state instanceof PhotoState.Success) {
            List<PhotoItem> items = ((PhotoState.Success) state).getItems();
            stopShimmers(binding.shimmerGrid.getRoot());
            binding.shimmerGrid.getRoot().setVisibility(View.GONE);

            if (binding.emptyView != null) binding.emptyView.getRoot().setVisibility(View.GONE);
            binding.rvPhotos.setVisibility(View.VISIBLE);
            adapter.submitList(items);
        }
        else if (state instanceof PhotoState.Empty) {
            stopShimmers(binding.shimmerGrid.getRoot());
            binding.shimmerGrid.getRoot().setVisibility(View.GONE);

            binding.rvPhotos.setVisibility(View.GONE);
            if (binding.emptyView != null) binding.emptyView.getRoot().setVisibility(View.VISIBLE);
        }
        else if (state instanceof PhotoState.Error) {
            stopShimmers(binding.shimmerGrid.getRoot());
            binding.shimmerGrid.getRoot().setVisibility(View.GONE);

            binding.rvPhotos.setVisibility(View.GONE);
            if (binding.emptyView != null) binding.emptyView.getRoot().setVisibility(View.GONE);

            String msg = ((PhotoState.Error) state).getMessage();
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }

    private void doSearch(String query) { doSearch(query, false); }

    private void doSearch(String query, boolean fromSwipeRefresh) {
        // Reset pagination state
        currentQuery = query == null ? "" : query.trim();
        page = 1;
        endReached = false;
        isLoading = true;

        // Cancel any in-flight before starting
        FlickrRepo.cancelSearch();
        if (!fromSwipeRefresh) {
            // chỉ show shimmer khi không phải pull-to-refresh
            setState(new PhotoState.Loading());
        } else {
            // khi refresh, giữ nguyên list hiển thị
            if (binding.shimmerGrid != null) {
                stopShimmers(binding.shimmerGrid.getRoot());
                binding.shimmerGrid.getRoot().setVisibility(View.GONE);
            }
            binding.rvPhotos.setVisibility(View.VISIBLE);
            if (binding.emptyView != null) binding.emptyView.getRoot().setVisibility(View.GONE);
        }
        FlickrRepo.search(currentQuery, page, perPage, new FlickrRepo.CB() {
            @Override
            public void ok(List<PhotoItem> items) {
                isLoading = false;
                if (binding != null) binding.swipeRefresh.setRefreshing(false);
                if (items == null || items.isEmpty()) {
                    setState(new PhotoState.Empty());
                    endReached = true;
                } else {
                    setState(new PhotoState.Success(items));
                    // đánh dấu nếu đã hết trang (nếu kết quả ít hơn perPage)
                    if (items.size() < perPage) endReached = true;
                }
            }

            @Override
            public void err(Throwable e) {
                isLoading = false;
                if (binding != null) binding.swipeRefresh.setRefreshing(false);
                setState(new PhotoState.Error("Search failed"));
            }
        });
    }

    private void loadMore() {
        if (isLoading || endReached || currentQuery.isEmpty()) return;
        isLoading = true;

        // Không show shimmer full-screen khi load-more, chỉ append
        FlickrRepo.search(currentQuery, page + 1, perPage, new FlickrRepo.CB() {
            @Override
            public void ok(List<PhotoItem> items) {
                isLoading = false;
                if (items == null || items.isEmpty()) {
                    endReached = true;
                    return;
                }
                adapter.addMore(items);
                page++;
                if (items.size() < perPage) endReached = true;
            }

            @Override
            public void err(Throwable e) {
                isLoading = false;
                Toast.makeText(requireContext(), R.string.load_more_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startShimmers(View root){
        if (root instanceof com.facebook.shimmer.ShimmerFrameLayout){
            ((com.facebook.shimmer.ShimmerFrameLayout)root).startShimmer();
        }
        if (root instanceof ViewGroup){
            ViewGroup vg = (ViewGroup) root;
            for (int i = 0; i <vg.getChildCount(); i++){
                startShimmers(vg.getChildAt(i));
            }
        }
    }

    private void stopShimmers(View root){
        if (root instanceof com.facebook.shimmer.ShimmerFrameLayout){
            ((com.facebook.shimmer.ShimmerFrameLayout) root).stopShimmer();
        }
        if (root instanceof ViewGroup){
            ViewGroup vg = (ViewGroup) root;
            for (int i = 0; i <vg.getChildCount(); i++){
                stopShimmers(vg.getChildAt(i));
            }
        }
    }
}