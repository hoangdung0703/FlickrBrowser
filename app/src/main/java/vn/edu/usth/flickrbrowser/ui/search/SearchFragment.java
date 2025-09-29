package vn.edu.usth.flickrbrowser.ui.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import java.util.Arrays;
import java.util.List;

import vn.edu.usth.flickrbrowser.R;
import vn.edu.usth.flickrbrowser.core.model.PhotoItem;
import vn.edu.usth.flickrbrowser.databinding.FragmentSearchBinding;
import vn.edu.usth.flickrbrowser.ui.common.GridSpacingDecoration;
import vn.edu.usth.flickrbrowser.ui.common.PhotosAdapter;
import vn.edu.usth.flickrbrowser.ui.state.PhotoState;

public class SearchFragment extends Fragment {
    private FragmentSearchBinding binding;
    private PhotosAdapter adapter;
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

        adapter = new PhotosAdapter();
        binding.rvPhotos.setAdapter(adapter);

        // 1) AppBar title
        binding.topAppBar.setTitle("search");

        // 2) RecyclerView grid 2 cột
        int span = 2;
        binding.rvPhotos.setLayoutManager(new GridLayoutManager(getContext(), span));

        // 3) Spacing theo token
        int spacingPx = getResources().getDimensionPixelSize(R.dimen.spacing_m);
        binding.rvPhotos.addItemDecoration(new GridSpacingDecoration(span, spacingPx, true));

        // 4) Mock data (danh sách drawable id)
        List<Integer> mockIds = Arrays.asList(
                R.drawable.placeholder_grey, R.drawable.placeholder_grey, R.drawable.placeholder_grey,
                R.drawable.placeholder_grey, R.drawable.placeholder_grey, R.drawable.placeholder_grey,
                R.drawable.placeholder_grey, R.drawable.placeholder_grey, R.drawable.placeholder_grey
                );

        // 5) Gắn adapter mock
        binding.rvPhotos.setAdapter(new MockPhotoAdapter(mockIds));

        // 6) EmptyView: ngày 1 để GONE, ngày 2 sẽ show/hide theo dữ liệu
        binding.txtEmpty.setVisibility(View.GONE);
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
            binding.emptyView.getRoot().setVisibility(View.GONE);

        } else if (state instanceof PhotoState.Success) {
            List<PhotoItem> items = ((PhotoState.Success) state).getItems();
            stopShimmers(binding.shimmerGrid.getRoot());
            binding.shimmerGrid.getRoot().setVisibility(View.GONE);

            binding.emptyView.getRoot().setVisibility(View.GONE);
            binding.rvPhotos.setVisibility(View.VISIBLE);
            adapter.submitList(items);

        } else if (state instanceof PhotoState.Empty) {
            stopShimmers(binding.shimmerGrid.getRoot());
            binding.shimmerGrid.getRoot().setVisibility(View.GONE);

            binding.rvPhotos.setVisibility(View.GONE);
            binding.emptyView.getRoot().setVisibility(View.VISIBLE);
            binding.emptyView.emptyText.setText(R.string.empty_default);

        } else if (state instanceof PhotoState.Error) {
            stopShimmers(binding.shimmerGrid.getRoot());
            binding.shimmerGrid.getRoot().setVisibility(View.GONE);

            binding.rvPhotos.setVisibility(View.GONE);
            binding.emptyView.getRoot().setVisibility(View.GONE);

            String msg = ((PhotoState.Error) state).getMessage();
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }

    private void startShimmers(View root) {
        if (root instanceof com.facebook.shimmer.ShimmerFrameLayout) {
            ((com.facebook.shimmer.ShimmerFrameLayout) root).startShimmer();
        }
        if (root instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) root;
            for (int i = 0; i < vg.getChildCount(); i++) startShimmers(vg.getChildAt(i));
        }
    }

    private void stopShimmers(View root) {
        if (root instanceof com.facebook.shimmer.ShimmerFrameLayout) {
            ((com.facebook.shimmer.ShimmerFrameLayout) root).stopShimmer();
        }
        if (root instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) root;
            for (int i = 0; i < vg.getChildCount(); i++) stopShimmers(vg.getChildAt(i));
        }
    }

}

//Biding sinh từ fragment_search.xml
//GridLayoutManager(2) tạo lưới 2 cột
//GridSpacingDecoration dùng @dimen/spacing_m -> px
//mockIds lặp lại vài lần để nhìn đủ grid
//txtEmpty để sẵn cho ngày 2(ẩn)