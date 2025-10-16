package vn.edu.usth.flickrbrowser.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import vn.edu.usth.flickrbrowser.R;
import vn.edu.usth.flickrbrowser.core.model.PhotoItem;

public class HomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Interfaces for callbacks
    public interface OnPhotoInteractionListener {
        void onPhotoClick(PhotoItem photo, int position);
        void onFavoriteClick(PhotoItem photo);
        void onShareClick(PhotoItem photo);
        void onOwnerClick(PhotoItem photo);
    }

    private final List<PhotoItem> photoList = new ArrayList<>();
    private final Set<String> favoriteIds = new HashSet<>();
    private OnPhotoInteractionListener listener;

    private static final int TYPE_PHOTO = 1;
    private static final int TYPE_LOADING = 2;
    private boolean isLoadingFooter = false;

    public void setOnPhotoInteractionListener(OnPhotoInteractionListener listener) {
        this.listener = listener;
    }

    public ArrayList<PhotoItem> getCurrentData() {
        return new ArrayList<>(photoList);
    }

    public void setData(List<PhotoItem> newPhotos, List<PhotoItem> favorites) {
        photoList.clear();
        if (newPhotos != null) {
            photoList.addAll(newPhotos);
        }
        updateFavoriteSet(favorites);
        notifyDataSetChanged();
    }

    public void addMore(List<PhotoItem> morePhotos) {
        if (morePhotos != null && !morePhotos.isEmpty()) {
            int startPosition = photoList.size();
            photoList.addAll(morePhotos);
            notifyItemRangeInserted(startPosition, morePhotos.size());
        }
    }

    public void updateFavoriteStatus(PhotoItem photo, boolean isFavorite) {
        int index = -1;
        for (int i = 0; i < photoList.size(); i++) {
            if (photoList.get(i).id.equals(photo.id)) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            if (isFavorite) {
                favoriteIds.add(photo.id);
            } else {
                favoriteIds.remove(photo.id);
            }
            notifyItemChanged(index);
        }
    }

    private void updateFavoriteSet(List<PhotoItem> favorites) {
        favoriteIds.clear();
        if (favorites != null) {
            for (PhotoItem p : favorites) {
                favoriteIds.add(p.id);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (position == photoList.size() && isLoadingFooter) ? TYPE_LOADING : TYPE_PHOTO;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_LOADING) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_photo, parent, false);
            return new PhotoViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PhotoViewHolder) {
            PhotoViewHolder photoHolder = (PhotoViewHolder) holder;
            PhotoItem photo = photoList.get(position);

            photoHolder.ownerName.setText(photo.owner);

            // ============================================================
            // THAY ĐỔI LOGIC Ở ĐÂY
            // ============================================================

            if (photo.title != null && !photo.title.trim().isEmpty()) {
                photoHolder.photoTitle.setText(photo.title);
                photoHolder.photoTitle.setVisibility(View.VISIBLE); // Hiển thị TextView
            } else {
                photoHolder.photoTitle.setVisibility(View.GONE); // Ẩn TextView đi
            }

            try {
                Glide.with(holder.itemView.getContext())
                        .load(photo.getFullUrl())
                        .placeholder(R.drawable.placeholder_grey)
                        .error(R.drawable.placeholder_grey)
                        .into(photoHolder.photoImage);
            } catch (Exception e) {
                photoHolder.photoImage.setImageResource(R.drawable.placeholder_grey);
            }

            if (favoriteIds.contains(photo.id)) {
                photoHolder.btnFavorite.setIconResource(R.drawable.baseline_favorite_24);
            } else {
                photoHolder.btnFavorite.setIconResource(R.drawable.outline_favorite_24);
            }

            photoHolder.photoImage.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPhotoClick(photo, photoHolder.getBindingAdapterPosition());
                }
            });

            photoHolder.btnFavorite.setOnClickListener(v -> {
                if (listener != null) listener.onFavoriteClick(photo);
            });
            photoHolder.btnShare.setOnClickListener(v -> {
                if (listener != null) listener.onShareClick(photo);
            });
            View.OnClickListener ownerClickListener = v -> {
                if (listener != null) listener.onOwnerClick(photo);
            };
            photoHolder.ownerName.setOnClickListener(ownerClickListener);
            photoHolder.ownerAvatar.setOnClickListener(ownerClickListener);
        }
    }

    @Override
    public int getItemCount() {
        return photoList.size() + (isLoadingFooter ? 1 : 0);
    }

    public void addLoadingFooter() {
        if (!isLoadingFooter) {
            isLoadingFooter = true;
            notifyItemInserted(photoList.size());
        }
    }

    public void removeLoadingFooter() {
        if (isLoadingFooter) {
            isLoadingFooter = false;
            int position = photoList.size();
            notifyItemRemoved(position);
        }
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView ownerAvatar, photoImage;
        TextView ownerName, photoTitle;
        MaterialButton btnFavorite, btnComment, btnShare;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ownerAvatar = itemView.findViewById(R.id.ownerAvatar);
            photoImage = itemView.findViewById(R.id.photoImage);
            ownerName = itemView.findViewById(R.id.ownerName);
            photoTitle = itemView.findViewById(R.id.photoTitle);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            btnComment = itemView.findViewById(R.id.btnComment);
            btnShare = itemView.findViewById(R.id.btnShare);
        }
    }

    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;
        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}

