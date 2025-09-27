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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

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

        //spacing
        final int space = getResources().getDimensionPixelSize(R.dimen.spacing_m);
        rv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View v,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.set(space, space, space, space);
            }
        });

        //Mock list of items
        List<String> mockList = generateMockList(20);

        if (mockList.isEmpty()) {
            empty.setVisibility(View.VISIBLE);
        } else {
            empty.setVisibility(View.GONE);
            rv.setAdapter(new MockAdapter(mockList));
        }

    }

    /**
     * Helper method to generate a mock list of items
     * @param count number of items to create
     */
    private List<String> generateMockList(int count) {
        List<String> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            list.add("Mock item " + i);

        }
        return list;
    }

    //Adapter
    public static class MockAdapter extends RecyclerView.Adapter<MockAdapter.MockVH> {
        private final List<String> items;
        private final boolean[] favorites;

        public MockAdapter(List<String> items) {
            this.items = items;
            this.favorites = new boolean[items.size()];
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
            holder.tvTitle.setText(items.get(position));
            holder.btnFavorite.setImageResource(favorites[position] ? R.drawable.baseline_favorite_24 : R.drawable.outline_favorite_24);

            holder.btnFavorite.setOnClickListener(v -> {
                favorites[position] = !favorites[position];
                notifyItemChanged(position);
            });
        }
        @Override
        public int getItemCount() {
            return items.size();
        }
        public static class MockVH extends RecyclerView.ViewHolder {
            TextView tvTitle;
            ImageView btnFavorite;

            public MockVH(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                btnFavorite = itemView.findViewById(R.id.btnFavorite);
            }
        }
    }
}

