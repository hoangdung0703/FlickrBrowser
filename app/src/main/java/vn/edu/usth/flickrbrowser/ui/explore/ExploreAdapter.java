package vn.edu.usth.flickrbrowser.ui.explore;
import android.view.LayoutInflater; import android.view.View; import android.view.ViewGroup; import android.widget.ImageView;
import androidx.annotation.NonNull; import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList; import java.util.List;
import vn.edu.usth.flickrbrowser.R; import vn.edu.usth.flickrbrowser.core.model.PhotoItem;
import vn.edu.usth.flickrbrowser.ui.favorites.FavoritesViewModel;

public class ExploreAdapter extends RecyclerView.Adapter<ExploreAdapter.VH>{
    private final List<PhotoItem> data=new ArrayList<>();
    private final FavoritesViewModel favVM;

    public ExploreAdapter(FavoritesViewModel favVM) {
        this.favVM = favVM;
    }

    public void setData(List<PhotoItem> list){
        data.clear();
        if(list!=null) data.addAll(list);
        notifyDataSetChanged();
    }
    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent,int viewType){
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo,parent,false);
        return new VH(v);
    }
    @Override public void onBindViewHolder(@NonNull VH h,int pos){
        PhotoItem p=data.get(pos);
        Glide.with(h.img.getContext()).load(p.getThumbUrl()).placeholder(R.drawable.bg_skeleton_rounded).centerCrop().into(h.img);
        // setting the heart icon
        boolean isFav = favVM.isFavorite(p.id);
        h.heart.setImageResource(isFav ? R.drawable.baseline_favorite_24 : R.drawable.outline_favorite_24); // using id as a key

        // click ♥ -> toggle in ViewModel
        h.heart.setOnClickListener(v -> {
            int adapterPos = h.getBindingAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) return;

            PhotoItem item = data.get(adapterPos);
            favVM.toggleFavorite(item.id);   // update viewmodel
            notifyItemChanged(adapterPos);   // change icon
        });
    }
    @Override public int getItemCount(){ return data.size(); }
    static class VH extends RecyclerView.ViewHolder{
        ImageView img;
        ImageView heart; // add heart icon
        VH(@NonNull View v){
            super(v);
            img=v.findViewById(R.id.imgPhoto);
            heart = itemView.findViewById(R.id.btnFavorite);
        }
    }
}