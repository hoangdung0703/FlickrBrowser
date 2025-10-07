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

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import vn.edu.usth.flickrbrowser.R;
import vn.edu.usth.flickrbrowser.core.model.PhotoItem;

public class FavoritesFragment extends Fragment {

    private FavoritesViewModel vm;
    private FavoritesAdapter adapter;

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

        // Spacing
        final int space = getResources().getDimensionPixelSize(R.dimen.spacing_m);
        rv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View v,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.set(space, space, space, space);
            }
        });

        // ViewModel (scope activity để chia sẻ với Explore/Search)
        vm = new ViewModelProvider(requireActivity()).get(FavoritesViewModel.class);

        // Adapter (vẫn giữ tên và cách gọi cũ)
        adapter = new FavoritesAdapter(new ArrayList<>(), item -> vm.removeFavorite(item));
        rv.setAdapter(adapter);

        // Observe LiveData → update list & EmptyView
        vm.getFavorites().observe(getViewLifecycleOwner(), list -> {
            if (list == null || list.isEmpty()) {
                empty.setVisibility(View.VISIBLE);
                adapter.update(new ArrayList<>());
            } else {
                empty.setVisibility(View.GONE);
                adapter.update(list);
            }
        });
    }

    // Giữ nguyên cấu trúc adapter như bạn có, chỉ đổi String → PhotoItem
    public static class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.VH> {

        public interface OnFavoriteClick { void onClick(PhotoItem item); }

        private List<PhotoItem> items;
        private final OnFavoriteClick callback;

        public FavoritesAdapter(List<PhotoItem> items, OnFavoriteClick callback) {
            this.items = items;
            this.callback = callback;
        }

        public void update(List<PhotoItem> newItems) {
            this.items = (newItems != null) ? new ArrayList<>(newItems) : new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_favorite, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            PhotoItem item = items.get(position);

            // Hiển thị ảnh thay vì text
            String url = item.getThumbUrl();
            if (url == null || url.isEmpty()) url = item.getFullUrl();

            Glide.with(holder.itemView.getContext())
                    .load(url)
                    .placeholder(R.drawable.bg_skeleton_rounded)
                    .centerCrop()
                    .into(holder.img);

            holder.tvTitle.setText(item.title != null ? item.title : "(No title)");
            holder.btnFavorite.setImageResource(R.drawable.baseline_favorite_24);
            holder.btnFavorite.setContentDescription(
                    holder.itemView.getContext().getString(R.string.cd_unfavorite)
            );

            // Click ♥ → remove khỏi favorites
            holder.btnFavorite.setOnClickListener(v -> {
                if (callback != null) callback.onClick(item);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            ImageView img;
            TextView tvTitle;
            ImageView btnFavorite;

            VH(@NonNull View itemView) {
                super(itemView);
                img = itemView.findViewById(R.id.imgPhoto);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                btnFavorite = itemView.findViewById(R.id.btnFavorite);
            }
        }
    }
}
