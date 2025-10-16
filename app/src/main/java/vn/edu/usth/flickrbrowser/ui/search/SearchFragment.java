package vn.edu.usth.flickrbrowser.ui.search;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import vn.edu.usth.flickrbrowser.R;
import vn.edu.usth.flickrbrowser.core.api.FlickrRepo;
import vn.edu.usth.flickrbrowser.core.model.PhotoItem;
import vn.edu.usth.flickrbrowser.databinding.FragmentSearchBinding;
import vn.edu.usth.flickrbrowser.ui.common.GridSpacingDecoration;
import vn.edu.usth.flickrbrowser.ui.favorites.FavoritesViewModel;
import vn.edu.usth.flickrbrowser.ui.state.PhotoState;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private PhotosAdapter adapter;
    private FavoritesViewModel favVM;

    // THÊM CÁC BIẾN MỚI
    private RecyclerView rvSuggestions;
    private SuggestionAdapter suggestionAdapter;

    private int page = 1;
    private final int perPage = 24;
    private boolean isLoading = false;
    private boolean endReached = false;
    private String currentQuery = "";


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
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState); // Thêm dòng này
        favVM = new ViewModelProvider(requireActivity()).get(FavoritesViewModel.class);
        adapter = new PhotosAdapter((item, position) -> {
            android.content.Intent i =
                    new android.content.Intent(requireContext(),
                            vn.edu.usth.flickrbrowser.ui.detail.DetailActivity.class);

            // Gửi danh sách đang hiển thị + vị trí bấm
            i.putExtra(vn.edu.usth.flickrbrowser.ui.detail.DetailActivity.EXTRA_PHOTOS,
                    adapter.getCurrentData());
            i.putExtra(vn.edu.usth.flickrbrowser.ui.detail.DetailActivity.EXTRA_START_INDEX,
                    position);

            detailLauncher.launch(i);
        });
        binding.rvPhotos.setAdapter(adapter);

        // AppBar title


        // Grid 2 cột
        int span = 2;
        GridLayoutManager glm = new GridLayoutManager(getContext(), span);
        binding.rvPhotos.setLayoutManager(glm);

        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int viewType = adapter.getItemViewType(position);
                return (viewType == PhotosAdapter.TYPE_LOADING) ? 2 : 1;
            }
        });

        // Spacing
        int spacingPx = getResources().getDimensionPixelSize(R.dimen.spacing_m);
        binding.rvPhotos.addItemDecoration(new GridSpacingDecoration(span, spacingPx, true));

        // Infinite scroll
        final int visibleThreshold = 6;
        binding.rvPhotos.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);
                if (dy <= 0) return;
                int total = glm.getItemCount();
                if (total <= 0) return;
                int lastVisible = glm.findLastVisibleItemPosition();
                if (!isLoading && !endReached && !currentQuery.isEmpty() && lastVisible >= total - visibleThreshold) {
                    loadMore();
                }
            }
        });

        // Pull-to-refresh: giữ list, không show shimmer full
        binding.swipeRefresh.setOnRefreshListener(() -> {
            String q = binding.edtQuery.getText() != null ? binding.edtQuery.getText().toString() : "";
            doSearch(q, true);
        });

        // IME action = Search
        binding.edtQuery.setOnEditorActionListener((v, actionId, ev) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                doSearch(v.getText() != null ? v.getText().toString() : "");
                return true;
            }
            return false;
        });

        // THÊM PHẦN KHỞI TẠO SUGGESTIONS
        rvSuggestions = view.findViewById(R.id.rv_suggestions);
        setupSuggestions();

        setState(new PhotoState.Empty());
    }

    // THÊM HÀM MỚI NÀY
    private void setupSuggestions() {
        List<Suggestion> suggestionList = new ArrayList<>();
        suggestionList.add(new Suggestion("Hanoi"));
        suggestionList.add(new Suggestion("Sai Gon"));
        suggestionList.add(new Suggestion("Thanh Hoa"));
        suggestionList.add(new Suggestion("France"));
        suggestionList.add(new Suggestion("Japan"));
        suggestionList.add(new Suggestion("Cats"));
        suggestionList.add(new Suggestion("Nature"));


        suggestionAdapter = new SuggestionAdapter(suggestionList, query -> {
            binding.edtQuery.setText(query);
            doSearch(query);
        });

        rvSuggestions.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvSuggestions.setAdapter(suggestionAdapter);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setState(@NonNull vn.edu.usth.flickrbrowser.ui.state.PhotoState state) {
        if (binding == null) return;

        if (state instanceof vn.edu.usth.flickrbrowser.ui.state.PhotoState.Loading) {
            binding.shimmerGrid.getRoot().setVisibility(View.VISIBLE);
            startShimmers(binding.shimmerGrid.getRoot());

            binding.rvPhotos.setVisibility(View.GONE);
            if (binding.emptyView != null) binding.emptyView.getRoot().setVisibility(View.GONE);
        } else if (state instanceof vn.edu.usth.flickrbrowser.ui.state.PhotoState.Success) {
            List<PhotoItem> items = ((vn.edu.usth.flickrbrowser.ui.state.PhotoState.Success) state).getItems();
            stopShimmers(binding.shimmerGrid.getRoot());
            binding.shimmerGrid.getRoot().setVisibility(View.GONE);
            if (binding.emptyView != null) binding.emptyView.getRoot().setVisibility(View.GONE);
            binding.rvPhotos.setVisibility(View.VISIBLE);
            adapter.submitList(items);
        } else if (state instanceof vn.edu.usth.flickrbrowser.ui.state.PhotoState.Empty) {
            stopShimmers(binding.shimmerGrid.getRoot());
            binding.shimmerGrid.getRoot().setVisibility(View.GONE);

            binding.rvPhotos.setVisibility(View.GONE);
            if (binding.emptyView != null) binding.emptyView.getRoot().setVisibility(View.VISIBLE);
        } else if (state instanceof vn.edu.usth.flickrbrowser.ui.state.PhotoState.Error) {
            stopShimmers(binding.shimmerGrid.getRoot());
            binding.shimmerGrid.getRoot().setVisibility(View.GONE);

            binding.rvPhotos.setVisibility(View.GONE);
            if (binding.emptyView != null) binding.emptyView.getRoot().setVisibility(View.GONE);
            String msg = ((PhotoState.Error) state).getMessage();
            Toast.makeText(requireContext(),
                    (msg == null || msg.isEmpty()) ? getString(R.string.search_failed) : msg,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void doSearch(String query) { doSearch(query, false); }

    private void doSearch(String query, boolean fromSwipeRefresh) {
        if (binding == null) return;

        // Chuẩn hoá query
        currentQuery = query == null ? "" : query.trim();

        // Nếu rỗng → không gọi API, show Empty luôn
        if (currentQuery.isEmpty()) {
            isLoading = false;
            endReached = true;
            page = 1;
            binding.swipeRefresh.setRefreshing(false);
            adapter.clearData();
            setState(new PhotoState.Empty());
            return;
        }

        // Reset phân trang
        page = 1;
        endReached = false;
        isLoading = true;
        // 👉 Nếu refresh thì random page
        if (fromSwipeRefresh) {
            page = new java.util.Random().nextInt(10) + 1; // random từ 1 tới 10
        } else {
            page = 1;
        }


        // Huỷ in-flight
        FlickrRepo.cancelSearch();

        if (!fromSwipeRefresh) {
            setState(new vn.edu.usth.flickrbrowser.ui.state.PhotoState.Loading());
        } else {
            /** added: xoá toàn bộ dữ liệu cũ ngay khi refresh **/
            adapter.clearData();

            // Refresh: giữ list, tắt shimmer
            stopShimmers(binding.shimmerGrid.getRoot());
            binding.shimmerGrid.getRoot().setVisibility(View.GONE);
            binding.rvPhotos.setVisibility(View.VISIBLE);
            if (binding.emptyView != null) binding.emptyView.getRoot().setVisibility(View.GONE);
        }

        FlickrRepo.search(currentQuery, page, perPage, new FlickrRepo.CB() {
            @Override
            public void ok(List<PhotoItem> items) {
                isLoading = false;
                binding.swipeRefresh.setRefreshing(false);

                if (items == null || items.isEmpty()) {
                    setState(new vn.edu.usth.flickrbrowser.ui.state.PhotoState.Empty());
                    endReached = true;
                } else {
                    setState(new vn.edu.usth.flickrbrowser.ui.state.PhotoState.Success(items));
                    if (items.size() < perPage) endReached = true;
                }
            }

            @Override
            public void err(Throwable e) {
                isLoading = false;
                binding.swipeRefresh.setRefreshing(false);
                String msg = (e != null && e.getMessage() != null && !e.getMessage().isEmpty())
                        ? e.getMessage()
                        : getString(R.string.search_failed);
                setState(new vn.edu.usth.flickrbrowser.ui.state.PhotoState.Error(msg));
            }
        });
    }

    private void loadMore() {
        if (binding == null) return;
        if (isLoading || endReached || currentQuery.isEmpty()) return;

        isLoading = true;
        adapter.addLoadingFooter();
        FlickrRepo.search(currentQuery, page + 1, perPage, new FlickrRepo.CB() {
            @Override
            public void ok(List<PhotoItem> items) {
                isLoading = false;
                adapter.removeLoadingFooter();
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
                adapter.removeLoadingFooter();
                String msg = (e != null && e.getMessage() != null && !e.getMessage().isEmpty())
                        ? e.getMessage()
                        : getString(R.string.load_more_failed);
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startShimmers(View root){
        if (root instanceof com.facebook.shimmer.ShimmerFrameLayout){
            ((com.facebook.shimmer.ShimmerFrameLayout)root).startShimmer();
        }
        if (root instanceof ViewGroup){
            ViewGroup vg = (ViewGroup) root;
            for (int i = 0; i < vg.getChildCount(); i++){
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
            for (int i = 0; i < vg.getChildCount(); i++){
                stopShimmers(vg.getChildAt(i));
            }
        }
    }
}
