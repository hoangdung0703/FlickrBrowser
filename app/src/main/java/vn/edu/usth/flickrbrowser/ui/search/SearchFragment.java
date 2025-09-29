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
