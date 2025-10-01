package vn.edu.usth.flickrbrowser.ui.explore;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.List;

import vn.edu.usth.flickrbrowser.R;
import vn.edu.usth.flickrbrowser.core.api.FlickrRepo;
import vn.edu.usth.flickrbrowser.core.model.PhotoItem;
import vn.edu.usth.flickrbrowser.ui.state.PhotoState;

public class ExploreFragment extends Fragment {
    private SwipeRefreshLayout swipe;
    private RecyclerView rv;
    private ExploreAdapter adapter;
    private ShimmerFrameLayout shimmerGrid; // Changed to ShimmerFrameLayout type
    private View emptyRoot;
    private TextView emptyText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup parent, @Nullable Bundle b) {
        View v = inf.inflate(R.layout.fragment_explore, parent, false);
        swipe = v.findViewById(R.id.swipe);
        rv = v.findViewById(R.id.recyclerViewExplore);

        // Ensure in fragment_explore.xml ShimmerFrameLayout has id "shimmerGrid"
        shimmerGrid = v.findViewById(R.id.shimmerGrid); 
        // Ensure in fragment_explore.xml the include tag for empty view has id "emptyView"
        emptyRoot = v.findViewById(R.id.emptyView); 
        emptyText = emptyRoot.findViewById(R.id.emptyText); // Assumes emptyText is within emptyView

        rv.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        adapter = new ExploreAdapter(); // Consider passing a click listener here if needed
        rv.setAdapter(adapter);
        swipe.setOnRefreshListener(this::load); // Call load when user swipes
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        super.onViewCreated(v, b);
        load(); // Initial load
    }

    private void load() {
        setState(new PhotoState.Loading()); // Set loading state immediately

        FlickrRepo.getRecent(1, 12, new FlickrRepo.CB() {
            @Override
            public void ok(List<PhotoItem> items) {
                if (items == null || items.isEmpty()) {
                    setState(new PhotoState.Empty());
                } else {
                    setState(new PhotoState.Success(items));
                }
            }

            @Override// Handle error
            public void err(Throwable t) {
                // Check if context is still available
                if (getContext() != null) {
                     setState(new PhotoState.Error(t.getMessage() != null ? t.getMessage() : "Unknown error"));
                }
            }
        });
    }

    private void setState(@NonNull PhotoState state) {
        // Stop swipe refresh animation for all final states
        if (!(state instanceof PhotoState.Loading)) {
            swipe.setRefreshing(false);
        }

        if (state instanceof PhotoState.Loading) {
            shimmerGrid.setVisibility(View.VISIBLE);
            shimmerGrid.startShimmer(); // Start shimmer directly

            rv.setVisibility(View.GONE);
            emptyRoot.setVisibility(View.GONE);
            // swipe.setRefreshing(true); // Optional: if you want spinner + shimmer
        } else if (state instanceof PhotoState.Success) {
            List<PhotoItem> items = ((PhotoState.Success) state).getItems();
            
            shimmerGrid.stopShimmer(); // Stop shimmer directly
            shimmerGrid.setVisibility(View.GONE);
            emptyRoot.setVisibility(View.GONE);

            rv.setVisibility(View.VISIBLE);
            adapter.setData(items);
        } else if (state instanceof PhotoState.Empty) {
            shimmerGrid.stopShimmer(); // Stop shimmer directly
            shimmerGrid.setVisibility(View.GONE);

            rv.setVisibility(View.GONE);
            emptyRoot.setVisibility(View.VISIBLE);
            emptyText.setText(R.string.empty_default); // Ensure this string exists
        } else if (state instanceof PhotoState.Error) {
            shimmerGrid.stopShimmer(); // Stop shimmer directly
            shimmerGrid.setVisibility(View.GONE);

            rv.setVisibility(View.GONE);
            // Optionally show empty view with error or just a toast
            emptyRoot.setVisibility(View.GONE); // Or View.VISIBLE if you want to show error text in emptyText
            // emptyText.setText(((PhotoState.Error) state).getMessage());


            String msg = ((PhotoState.Error) state).getMessage();
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }


}
