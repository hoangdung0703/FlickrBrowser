package vn.edu.usth.flickrbrowser.ui.explore;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.json.*;
import java.util.*;
import vn.edu.usth.flickrbrowser.R;
import vn.edu.usth.flickrbrowser.core.api.FlickrRepo;
import vn.edu.usth.flickrbrowser.core.model.PhotoItem;
import vn.edu.usth.flickrbrowser.ui.state.PhotoState;

public class ExploreFragment extends Fragment {
    private SwipeRefreshLayout swipe;
    private RecyclerView rv;
    private ExploreAdapter adapter;
    private ViewGroup shimmerGrid;
    private View emptyRoot;
    private TextView emptyText;
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
    }
    private void load(){ swipe.setRefreshing(true);
        FlickrRepo.getRecent(1,12,new FlickrRepo.CB(){
            @Override
            public void ok(String json){ swipe.setRefreshing(false);
                adapter.setData(parse(json));
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