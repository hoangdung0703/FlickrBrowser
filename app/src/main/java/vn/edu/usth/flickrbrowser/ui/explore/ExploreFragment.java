package vn.edu.usth.flickrbrowser.ui.explore;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.*;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import org.json.*;
import java.util.*;
import vn.edu.usth.flickrbrowser.R;
import vn.edu.usth.flickrbrowser.core.api.FlickrRepo;
import vn.edu.usth.flickrbrowser.core.model.PhotoItem;
import vn.edu.usth.flickrbrowser.ui.detail.DetailActivity;
import vn.edu.usth.flickrbrowser.ui.favorites.FavoritesViewModel;
import vn.edu.usth.flickrbrowser.ui.state.PhotoState;

public class ExploreFragment extends Fragment {
    private SwipeRefreshLayout swipe;
    private RecyclerView rv;
    private ExploreAdapter adapter;
    private ViewGroup shimmerGrid;
    private View emptyRoot;
    private TextView emptyText;


    private int currentPage = 1;
    private boolean isLoading = false;

    private FavoritesViewModel favVM;

    private final ActivityResultLauncher<Intent> detailLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    PhotoItem returned = (PhotoItem) result.getData().getSerializableExtra("PHOTO_ITEM");
                    boolean isFav = result.getData().getBooleanExtra("is_favorite", false);
                    if (returned != null) {
                        if (isFav) favVM.addFavorite(returned);
                        else favVM.removeFavorite(returned);
                    }
                }
            });


    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf,@Nullable ViewGroup parent,@Nullable Bundle b){
        View v=inf.inflate(R.layout.fragment_explore,parent,false);
        swipe=v.findViewById(R.id.swipe); rv=v.findViewById(R.id.recyclerViewExplore);

        shimmerGrid = v.findViewById(R.id.shimmerGrid);
        emptyRoot= v.findViewById(R.id.emptyView);
        emptyText = emptyRoot.findViewById(R.id.emptyText);

        rv.setLayoutManager(new GridLayoutManager(requireContext(),2));
        adapter=new ExploreAdapter();
        rv.setAdapter(adapter);
        swipe.setOnRefreshListener(this::load);
        return v;
    }
    @Override
    public void onViewCreated(@NonNull View v,@Nullable Bundle b){
        super.onViewCreated(v,b); load();
        favVM = new ViewModelProvider(requireActivity()).get(FavoritesViewModel.class);
        load();
        adapter.setOnPhotoClickListener(p -> {
            // Create an Intent to open DetailActivity
            Intent intent = new Intent(requireContext(), DetailActivity.class);

            // Pass the clicked photo's information with the correct key
            intent.putExtra("PHOTO_ITEM", p);

            intent.putExtra("is_favorite", favVM.isFavorite(p.id));
            detailLauncher.launch(intent);
        });


        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == adapter.getItemCount() - 1) {
                    if (!isLoading) {
                        loadMorePhotos();
                    }
                }
            }
        });

    }

    private void load(){

        currentPage = 1; // Reset lại trang khi làm mới

        swipe.setRefreshing(true);
        // tránh shimmer vĩnh viễn
        setState(new PhotoState.Loading());
        swipe.setRefreshing(true);

        FlickrRepo.getRecent(1,12,new FlickrRepo.CB(){
            @Override
            public void ok(List<PhotoItem> items){ swipe.setRefreshing(false);

                if (items == null || items.isEmpty()) {
                    setState(new PhotoState.Empty());
                } else {
                    setState(new PhotoState.Success(items));
                }
            }
            @Override
            public void err(Throwable t){
                swipe.setRefreshing(false);
                Toast.makeText(requireContext(),"Load error",Toast.LENGTH_SHORT).show();
            }
        });
    }
    private List<PhotoItem> parse(String j){
        List<PhotoItem> out=new ArrayList<>();
        try{
            JSONObject root=new JSONObject(j);
            JSONObject photos=root.optJSONObject("photos");
            JSONArray arr= photos!=null? photos.optJSONArray("photo"):null;
            if(arr==null)
                return out;
            for(int i=0;i<arr.length();i++){
                JSONObject o=arr.getJSONObject(i);
                PhotoItem p=new PhotoItem();
                p.id=o.optString("id");
                p.server=o.optString("server");
                p.secret=o.optString("secret");
                p.title=o.optString("title");
                p.owner=o.optString("owner");
                out.add(p);
            }
        }catch(Exception ignore){}return out; }



    private void loadMorePhotos() {
        isLoading = true;
        currentPage++;

        FlickrRepo.getRecent(currentPage, 12, new FlickrRepo.CB() {
            @Override
            public void ok(List<PhotoItem> items) {
                if (getView() != null && items != null && !items.isEmpty()) {
                    adapter.addPhotos(items); // Chỉ thêm ảnh mới, không thay thế
                }
                isLoading = false;
            }

            @Override
            public void err(Throwable t) {
                if (getView() != null) {
                    Toast.makeText(requireContext(), "Load more error", Toast.LENGTH_SHORT).show();
                }
                isLoading = false;
                currentPage--; // Giảm số trang để thử lại lần sau
            }
        });
    }



    private void setState(@NonNull PhotoState state) {
        if (state instanceof PhotoState.Loading){
            shimmerGrid.setVisibility(View.VISIBLE);
            startShimmers(shimmerGrid);

            rv.setVisibility(View.GONE);
            emptyRoot.setVisibility(View.GONE);
        }
        else if (state instanceof PhotoState.Success){
            List<PhotoItem> items = ((PhotoState.Success) state).getItems();
            stopShimmers(shimmerGrid);
            shimmerGrid.setVisibility(View.GONE);
            emptyRoot.setVisibility(View.GONE);

            rv.setVisibility(View.VISIBLE);
            adapter.setData(items);
        }
        else if (state instanceof PhotoState.Empty) {
            stopShimmers(shimmerGrid);
            shimmerGrid.setVisibility(View.GONE);

            rv.setVisibility(View.GONE);
            emptyRoot.setVisibility(View.VISIBLE);
            emptyText.setText(R.string.empty_default);
        }
        else if ( state instanceof PhotoState.Error){
            stopShimmers(shimmerGrid);
            shimmerGrid.setVisibility(View.GONE);

            rv.setVisibility(View.GONE);
            emptyRoot.setVisibility(View.GONE);

            String msg = ((PhotoState.Error) state).getMessage();
            Toast.makeText(requireContext(),msg,Toast.LENGTH_SHORT).show();
        }
    }

    private void startShimmers(View root){
        if (root instanceof com.facebook.shimmer.ShimmerFrameLayout){
            ((com.facebook.shimmer.ShimmerFrameLayout)root).startShimmer();
        }

        if (root instanceof ViewGroup){
            ViewGroup vg = (ViewGroup) root;
            for (int i = 0; i <vg.getChildCount(); i++){
                startShimmers(vg.getChildAt(i));
            }
        }
    }

    private void stopShimmers(View root){
        if (root instanceof com.facebook.shimmer.ShimmerFrameLayout){
            ((com.facebook.shimmer.ShimmerFrameLayout) root).stopShimmer();
        }

        if (root instanceof ViewGroup){
            ViewGroup vg = (ViewGroup) root;
            for (int i = 0; i <vg.getChildCount(); i++){
                stopShimmers(vg.getChildAt(i));
            }
        }
    }
}