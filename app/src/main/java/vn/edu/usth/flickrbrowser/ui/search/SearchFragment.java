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

import java.util.List;

import vn.edu.usth.flickrbrowser.R;
import vn.edu.usth.flickrbrowser.core.api.FlickrRepo;
import vn.edu.usth.flickrbrowser.core.model.PhotoItem;
import vn.edu.usth.flickrbrowser.databinding.FragmentSearchBinding;
import vn.edu.usth.flickrbrowser.ui.common.GridSpacingDecoration;
import vn.edu.usth.flickrbrowser.ui.search.PhotosAdapter;
import vn.edu.usth.flickrbrowser.ui.detail.DetailActivity;
import vn.edu.usth.flickrbrowser.ui.favorites.FavoritesViewModel;
import vn.edu.usth.flickrbrowser.ui.state.PhotoState;

public class SearchFragment extends Fragment {
    private FragmentSearchBinding binding;
    private PhotosAdapter adapter;
    private int page = 1;
    private final int perPage = 24;
    private FavoritesViewModel favVM;

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
        favVM = new ViewModelProvider(requireActivity()).get(FavoritesViewModel.class);
        adapter = new PhotosAdapter((item,position) -> {
            android.content.Intent i = new android.content.Intent(requireContext(), vn.edu.usth.flickrbrowser.ui.detail.DetailActivity.class);
            i.putExtra("PHOTO_ITEM", item);
            i.putExtra("is_favorite", favVM.isFavorite(item.id));
            detailLauncher.launch(i);
        });
        binding.rvPhotos.setAdapter(adapter);

        // 1) AppBar title
        binding.topAppBar.setTitle(R.string.search_hint);

        // 2) RecyclerView grid 2 cột
        int span = 2;
        binding.rvPhotos.setLayoutManager(new GridLayoutManager(getContext(), span));

        // 3) Spacing theo token
        int spacingPx = getResources().getDimensionPixelSize(R.dimen.spacing_m);
        binding.rvPhotos.addItemDecoration(new GridSpacingDecoration(span, spacingPx, true));

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

    private void doSearch(String query) {
        // Cancel any in-flight before starting
        FlickrRepo.cancelSearch();
        setState(new PhotoState.Loading());
        FlickrRepo.search(query, page, perPage, new FlickrRepo.CB() {
            @Override
            public void ok(List<PhotoItem> items) {
                if (items == null || items.isEmpty()) {
                    setState(new PhotoState.Empty());
                } else {
                    setState(new PhotoState.Success(items));
                }
            }

            @Override
            public void err(Throwable e) {
                setState(new PhotoState.Error("Search failed"));
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