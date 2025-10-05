package vn.edu.usth.flickrbrowser.ui.explore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

import vn.edu.usth.flickrbrowser.R;
import vn.edu.usth.flickrbrowser.core.model.PhotoItem;

public class ExploreAdapter extends RecyclerView.Adapter<ExploreAdapter.VH> {

    //  START: ADDITIONS FOR CLICK HANDLING
    public interface OnPhotoClickListener {
        void onPhotoClick(PhotoItem photo);
    }

    private OnPhotoClickListener listener;

    public void setOnPhotoClickListener(OnPhotoClickListener listener) {
        this.listener = listener;
    }
    //  END: ADDITIONS FOR CLICK HANDLING

    private final List<PhotoItem> data = new ArrayList<>();

    // Hàm cập nhật danh sách ảnh
    public void setData(List<PhotoItem> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    // Hàm thêm ảnh vào danh sách (cho infinite scroll)
    public void addPhotos(List<PhotoItem> newPhotos) {
        if (newPhotos == null || newPhotos.isEmpty()) {
            return;
        }
        int currentSize = data.size();
        data.addAll(newPhotos);
        notifyItemRangeInserted(currentSize, newPhotos.size());
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        PhotoItem p = data.get(pos);

        // Lấy URL ảnh thumbnail (Pexels trả về medium/large)
        String url = p.getThumbUrl();
        if (url == null || url.isEmpty()) {
            url = p.getFullUrl();
        }

        // Load ảnh với Glide
        Glide.with(h.img.getContext())
                .load(url)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(h.img);

        //  START: ADDITION FOR CLICK HANDLING
        h.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPhotoClick(p);
            }
        });
        //  END: ADDITION FOR CLICK HANDLING
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        VH(@NonNull View v) {
            super(v);
            img = v.findViewById(R.id.imgPhoto);
        }
    }
}
