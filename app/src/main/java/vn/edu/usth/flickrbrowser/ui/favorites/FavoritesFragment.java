package vn.edu.usth.flickrbrowser.ui.favorites;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import vn.edu.usth.flickrbrowser.ui.detail.DetailActivity;

public class FavoritesFragment extends Fragment {

    private FavoritesViewModel vm;
    private FavoritesAdapter adapter;

    public FavoritesFragment() { }

    // Launcher để mở DetailActivity và nhận result quay về
    private final ActivityResultLauncher<Intent> detailLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    PhotoItem returned = (PhotoItem) result.getData().getSerializableExtra("PHOTO_ITEM");
                    boolean isFav = result.getData().getBooleanExtra("is_favorite", false);
                    if (returned != null) {
                        if (isFav) {
                            vm.addFavorite(returned);    // đảm bảo có trong favorites
                        } else {
                            vm.removeFavorite(returned); // nếu đã bỏ tim trong Detail → xoá khỏi danh sách
                        }
                    }
                }
            });

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

        // ViewModel (scope Activity để chia sẻ với Explore/Search)
        vm = new ViewModelProvider(requireActivity()).get(FavoritesViewModel.class);

        // Adapter:
        //  - click vào ♥: remove khỏi favorites (như cũ)
        //  - click vào item: mở DetailActivity (mới thêm)
        adapter = new FavoritesAdapter(
                new ArrayList<>(),
                item -> vm.removeFavorite(item),                      // click ♥
                item -> {                                             // click item mở Detail
                    Intent i = new Intent(requireContext(), DetailActivity.class);
                    i.putExtra("PHOTO_ITEM", item);
                    i.putExtra("is_favorite", true); // vì đang ở Favorites, mặc định đang ♥
                    detailLauncher.launch(i);
                }
        );
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

    // Adapter — giữ nguyên cấu trúc, chỉ thêm callback onItemClick để mở Detail
    public static class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.VH> {

        public interface OnFavoriteClick { void onClick(PhotoItem item); }
        public interface OnItemClick     { void onClick(PhotoItem item); }

        private List<PhotoItem> items;
        private final OnFavoriteClick favCallback;
        private final OnItemClick itemClick; // mới thêm để mở Detail

        public FavoritesAdapter(List<PhotoItem> items,
                                OnFavoriteClick favCallback,
                                OnItemClick itemClick) {
            this.items = (items != null) ? new ArrayList<>(items) : new ArrayList<>();
            this.favCallback = favCallback;
            this.itemClick = itemClick;
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

            // Hiển thị ảnh (nếu layout có imgPhoto)
            if (holder.img != null) {
                String url = item.getThumbUrl();
                if (url == null || url.isEmpty()) url = item.getFullUrl();

                Glide.with(holder.itemView.getContext())
                        .load(url)
                        .placeholder(R.drawable.bg_skeleton_rounded)
                        .centerCrop()
                        .into(holder.img);
            }

            if (holder.btnFavorite != null) {
                holder.btnFavorite.setImageResource(R.drawable.baseline_favorite_24);
                holder.btnFavorite.setContentDescription(
                        holder.itemView.getContext().getString(R.string.cd_unfavorite)
                );
                holder.btnFavorite.setOnClickListener(v -> {
                    if (favCallback != null) favCallback.onClick(item);
                });
            }

            // Click item → mở DetailActivity (dùng chung layout Detail)
            holder.itemView.setOnClickListener(v -> {
                if (itemClick != null) itemClick.onClick(item);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            ImageView img;
            ImageView btnFavorite;
            VH(@NonNull View itemView) {
                super(itemView);
                img = itemView.findViewById(R.id.imgPhoto);
                btnFavorite = itemView.findViewById(R.id.btnFavorite);
            }
        }
    }
}
