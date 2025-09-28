package vn.edu.usth.flickrbrowser.ui.favorites;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.edu.usth.flickrbrowser.R;

public class FavoritesFragment extends Fragment {

    private FavoritesViewModel vm;
    private MockAdapter adapter;

    public FavoritesFragment() { }

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

        // Grid 2 columns
        rv.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        // Spacing according to design system
        final int space = getResources().getDimensionPixelSize(R.dimen.spacing_m);
        rv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View v,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.set(space, space, space, space);
            }
        });

        // ViewModel (scope Activity để có thể share với Explore/Search sau này)
        vm = new ViewModelProvider(requireActivity()).get(FavoritesViewModel.class);

        // Adapter:  callback → ViewModel remove (unfavorite)
        adapter = new MockAdapter(new ArrayList<>(), item -> vm.removeFavorite(item));
        rv.setAdapter(adapter);

        // Observe LiveData → update list & EmptyView
        vm.getFavorites().observe(getViewLifecycleOwner(), list -> {
            if (list == null || list.isEmpty()) {
                empty.setVisibility(View.VISIBLE);
                adapter.update(new ArrayList<>()); // clear UI
            } else {
                empty.setVisibility(View.GONE);
                adapter.update(list);
            }
        });

    }


    // Adapter
    public static class MockAdapter extends RecyclerView.Adapter<MockAdapter.MockVH> {

        public interface OnFavoriteClick { void onClick(String item); }

        private List<String> items;
        private final OnFavoriteClick callback;

        public MockAdapter(List<String> items, OnFavoriteClick callback) {
            this.items = items;
            this.callback = callback;
        }

        /** Update list */
        public void update(List<String> newItems) {
            this.items = (newItems != null) ? new ArrayList<>(newItems) : new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MockVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_favorite, parent, false);
            return new MockVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull MockVH holder, int position) {
            String item = items.get(position);
            holder.tvTitle.setText(item);

            // Màn Favorites: show heart icon
            holder.btnFavorite.setImageResource(R.drawable.baseline_favorite_24);
            holder.btnFavorite.setContentDescription(
                    holder.itemView.getContext().getString(R.string.cd_unfavorite)
            );

            // Click heart icon → remove item
            holder.btnFavorite.setOnClickListener(v -> {
                if (callback != null) callback.onClick(item);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class MockVH extends RecyclerView.ViewHolder {
            TextView tvTitle;
            ImageView btnFavorite;

            MockVH(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                btnFavorite = itemView.findViewById(R.id.btnFavorite);
            }
        }
    }
}
