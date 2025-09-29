package vn.edu.usth.flickrbrowser.ui.search;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import vn.edu.usth.flickrbrowser.databinding.ItemPhotoBinding;
public class MockPhotoAdapter extends RecyclerView.Adapter<MockPhotoAdapter.ViewHolder> {
    private final List<Integer> items; // resID ảnh trong drawable

    public MockPhotoAdapter(List<Integer> items) {
        this.items = items;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemPhotoBinding binding;
        ViewHolder(ItemPhotoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemPhotoBinding binding = ItemPhotoBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Integer resID = items.get(position);
        Glide.with(holder.itemView)
                .load(resID)             //hnay load drawable, sau đổi sang URL .load(urlString)
                .into(holder.binding.imgPhoto);
    }

    @Override
    public int getItemCount() {
        return items.size();

    }
}
