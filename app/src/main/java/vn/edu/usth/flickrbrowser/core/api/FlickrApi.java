package vn.edu.usth.flickrbrowser.core.api;
import java.util.Map;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
public interface FlickrApi {
    @FormUrlEncoded @POST("getRecentPhotos") Call<ResponseBody> getRecent(@FieldMap Map<String, String> p);
    @FormUrlEncoded @POST("searchPhotos") Call<ResponseBody> search(@FieldMap Map<String, String> p);
}
