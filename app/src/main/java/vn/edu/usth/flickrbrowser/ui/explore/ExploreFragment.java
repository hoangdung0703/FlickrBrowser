package vn.edu.usth.flickrbrowser.ui.explore;

import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.*;
import vn.edu.usth.flickrbrowser.R;
import vn.edu.usth.flickrbrowser.core.api.FlickrRepo;
import vn.edu.usth.flickrbrowser.core.model.PhotoItem;
import vn.edu.usth.flickrbrowser.ui.state.PhotoState;

public class ExploreFragment extends Fragment {

    private SwipeRefreshLayout swipe;
    private RecyclerView rv;
    private ExploreAdapter adapter;
    private ViewGroup shimmerGrid;
    private View emptyRoot;
    private TextView emptyText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup parent, @Nullable Bundle b) {
        View v = inf.inflate(R.layout.fragment_explore, parent, false);

        swipe = v.findViewById(R.id.swipe);
        rv = v.findViewById(R.id.recyclerViewExplore);
        shimmerGrid = v.findViewById(R.id.shimmerGrid);
        emptyRoot = v.findViewById(R.id.emptyView);
        emptyText = emptyRoot.findViewById(R.id.emptyText);

        rv.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        adapter = new ExploreAdapter();
        rv.setAdapter(adapter);

        swipe.setOnRefreshListener(this::load);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        super.onViewCreated(v, b);
        load();
    }

    private void load() {
        if (!isAdded()) return;
        swipe.setRefreshing(true);
        setState(new PhotoState.Loading());

        // Gọi API (dùng Pexels hoặc fallback Flickr feed)
        FlickrRepo.getRecent(1, 12, new FlickrRepo.CB() {
            @Override
            public void ok(List<PhotoItem> items) {
                if (!isAdded()) return;
                swipe.setRefreshing(false);

                if (items == null || items.isEmpty()) {
                    setState(new PhotoState.Empty());
                } else {
                    setState(new PhotoState.Success(items));
                }
            }

            @Override
            public void err(Throwable t) {
                if (!isAdded()) return;
                swipe.setRefreshing(false);
                setState(new PhotoState.Error(t.getMessage() != null ? t.getMessage() : "Lỗi tải dữ liệu"));
                Toast.makeText(requireContext(), "Load error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setState(@NonNull PhotoState state) {
        if (state instanceof PhotoState.Loading) {
            shimmerGrid.setVisibility(View.VISIBLE);
            startShimmers(shimmerGrid);
            rv.setVisibility(View.GONE);
            emptyRoot.setVisibility(View.GONE);
        }
        else if (state instanceof PhotoState.Success) {
            List<PhotoItem> items = ((PhotoState.Success) state).getItems();
            stopShimmers(shimmerGrid);
            shimmerGrid.setVisibility(View.GONE);
            emptyRoot.setVisibility(View.GONE);

            rv.setVisibility(View.VISIBLE);
            adapter.setData(items);
        }
        else if (state instanceof PhotoState.Empty) {
            stopShimmers(shimmerGrid);
            shimmerGrid.setVisibility(View.GONE);
            rv.setVisibility(View.GONE);
            emptyRoot.setVisibility(View.VISIBLE);
            emptyText.setText(R.string.empty_default);
        }
        else if (state instanceof PhotoState.Error) {
            stopShimmers(shimmerGrid);
            shimmerGrid.setVisibility(View.GONE);
            rv.setVisibility(View.GONE);
            emptyRoot.setVisibility(View.VISIBLE);
            emptyText.setText(((PhotoState.Error) state).getMessage());
        }
    }

    private void startShimmers(View root) {
        if (root instanceof com.facebook.shimmer.ShimmerFrameLayout)
            ((com.facebook.shimmer.ShimmerFrameLayout) root).startShimmer();

        if (root instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) root;
            for (int i = 0; i < vg.getChildCount(); i++) startShimmers(vg.getChildAt(i));
        }
    }

    private void stopShimmers(View root) {
        if (root instanceof com.facebook.shimmer.ShimmerFrameLayout)
            ((com.facebook.shimmer.ShimmerFrameLayout) root).stopShimmer();

        if (root instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) root;
            for (int i = 0; i < vg.getChildCount(); i++) stopShimmers(vg.getChildAt(i));
        }
    }
}
