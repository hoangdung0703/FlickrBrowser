package vn.edu.usth.flickrbrowser.ui.explore;
import android.os.Bundle; import android.view.*; import android.widget.Toast;
import androidx.annotation.*; import androidx.fragment.app.Fragment; import androidx.recyclerview.widget.*; import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.json.*; import java.util.*;
import vn.edu.usth.flickrbrowser.R; import vn.edu.usth.flickrbrowser.core.api.FlickrRepo; import vn.edu.usth.flickrbrowser.core.model.PhotoItem;
public class ExploreFragment extends Fragment {
    private SwipeRefreshLayout swipe; private RecyclerView rv; private ExploreAdapter adapter;
    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inf,@Nullable ViewGroup parent,@Nullable Bundle b){
        View v=inf.inflate(R.layout.fragment_explore,parent,false);
        swipe=v.findViewById(R.id.swipe); rv=v.findViewById(R.id.recycler);
        rv.setLayoutManager(new GridLayoutManager(requireContext(),2)); adapter=new ExploreAdapter(); rv.setAdapter(adapter);
        swipe.setOnRefreshListener(this::load); return v;
    }
    @Override public void onViewCreated(@NonNull View v,@Nullable Bundle b){ super.onViewCreated(v,b); load(); }
    private void load(){ swipe.setRefreshing(true);
        FlickrRepo.getRecent(1,12,new FlickrRepo.CB(){ @Override public void ok(String json){ swipe.setRefreshing(false); adapter.setData(parse(json)); }
            @Override public void err(Throwable t){ swipe.setRefreshing(false); Toast.makeText(requireContext(),"Load error",Toast.LENGTH_SHORT).show(); } });
    }
    private List<PhotoItem> parse(String j){ List<PhotoItem> out=new ArrayList<>(); try{
        JSONObject root=new JSONObject(j); JSONObject photos=root.optJSONObject("photos"); JSONArray arr= photos!=null? photos.optJSONArray("photo"):null;
        if(arr==null) return out; for(int i=0;i<arr.length();i++){ JSONObject o=arr.getJSONObject(i); PhotoItem p=new PhotoItem();
            p.id=o.optString("id"); p.server=o.optString("server"); p.secret=o.optString("secret"); p.title=o.optString("title"); p.owner=o.optString("owner"); out.add(p);} }catch(Exception ignore){} return out; }
}
