package vn.edu.usth.flickrbrowser.ui.favorites;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import vn.edu.usth.flickrbrowser.R;

public class FavoritesFragment extends Fragment {

    public FavoritesFragment() { /* Required empty public constructor */ }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView rv = view.findViewById(R.id.rvFavorites);
        final View empty = view.findViewById(R.id.emptyView);

        // 2-column grid
        rv.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        // (Day 1) no adapter yet — we just show EmptyView mock.
        // If you already have a PhotoAdapter from Explore/Search, you can set it here.

        // Optional: spacing between items (8–12dp)
        final int space = getResources().getDimensionPixelSize(R.dimen.spacing_m);
        rv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View v,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.set(space, space, space, space);
            }
        });

        // Day 1 requirement: force show EmptyView (mock)
        empty.setVisibility(View.VISIBLE);
        // When you wire real data later:
        // empty.setVisibility(listIsEmpty ? View.VISIBLE : View.GONE);
    }
}
