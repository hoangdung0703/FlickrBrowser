package vn.edu.usth.flickrbrowser.core.api;
import java.util.HashMap; import java.util.Map;
import okhttp3.ResponseBody;
import retrofit2.Call; import retrofit2.Callback; import retrofit2.Response;
public class FlickrRepo {
    public interface CB { void ok(String json); void err(Throwable t); }
    private static FlickrApi api;
    private static FlickrApi api(){ if(api==null) api = ApiClient.get().create(FlickrApi.class); return api; }
    public static void getRecent(int page, int per, CB cb){
        Map<String,String> p = new HashMap<>(); p.put("page", String.valueOf(page)); p.put("per_page", String.valueOf(per));
        api().getRecent(p).enqueue(new Callback<ResponseBody>(){
            @Override public void onResponse(Call<ResponseBody> c, Response<ResponseBody> r){
                try{ String body = r.body()!=null? r.body().string(): (r.errorBody()!=null? r.errorBody().string(): "");
                     if(r.isSuccessful()) cb.ok(body); else cb.err(new RuntimeException("HTTP "+r.code())); }
                catch(Exception e){ cb.err(e); }
            }
            @Override public void onFailure(Call<ResponseBody> c, Throwable t){ cb.err(t); }
        });
    }
    public static void search(String q, int page, int per, CB cb){
        Map<String,String> p = new HashMap<>(); p.put("text", q); p.put("page", String.valueOf(page)); p.put("per_page", String.valueOf(per));
        api().search(p).enqueue(new Callback<ResponseBody>(){
            @Override public void onResponse(Call<ResponseBody> c, Response<ResponseBody> r){
                try{ String body = r.body()!=null? r.body().string(): (r.errorBody()!=null? r.errorBody().string(): "");
                     if(r.isSuccessful()) cb.ok(body); else cb.err(new RuntimeException("HTTP "+r.code())); }
                catch(Exception e){ cb.err(e); }
            }
            @Override public void onFailure(Call<ResponseBody> c, Throwable t){ cb.err(t); }
        });
    }
}
