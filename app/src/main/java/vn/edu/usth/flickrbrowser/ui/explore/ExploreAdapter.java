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

    /** Callback click item để Fragment mở DetailActivity */
    public interface OnPhotoClickListener { void onPhotoClick(PhotoItem photo, int position); }
    private OnPhotoClickListener listener;

    public void setOnPhotoClickListener(OnPhotoClickListener l) { this.listener = l; }

    /** Dữ liệu */
    private final List<PhotoItem> data = new ArrayList<>();
    public List<PhotoItem> getCurrentData() { return new ArrayList<>(data); }

    /** Thay toàn bộ danh sách */
    public void setData(List<PhotoItem> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    /** Thêm trang mới (paging) */
    public void addPhotos(List<PhotoItem> newPhotos) {
        if (newPhotos == null || newPhotos.isEmpty()) return;
        int start = data.size();
        data.addAll(newPhotos);
        notifyItemRangeInserted(start, newPhotos.size());
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        final PhotoItem p = data.get(pos);

        // Chọn URL an toàn cho Pexels/Flickr
        String url = (p.getThumbUrl() != null && !p.getThumbUrl().isEmpty())
                ? p.getThumbUrl() : p.getFullUrl();

        try {
            Glide.with(h.img.getContext())
                    .load(url)
                    .placeholder(R.drawable.placeholder_grey)
                    .error(R.drawable.placeholder_grey)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(h.img);
        } catch (Throwable t) {
            // tránh crash do context/activity đã destroy
            h.img.setImageResource(R.drawable.placeholder_grey);
        }

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPhotoClick(p, h.getBindingAdapterPosition());
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView img;
        VH(@NonNull View v) {
            super(v);
            // YÊU CẦU: item_photo.xml phải có ImageView id=imgPhoto
            img = v.findViewById(R.id.imgPhoto);
        }
    }
}
