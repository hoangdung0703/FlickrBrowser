package vn.edu.usth.flickrbrowser.core.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface FlickrApi {

    // Lấy ảnh mới nhất (Pexels: curated)
    @GET("curated")
    Call<ResponseBody> getRecent(
            @Query("page") int page,
            @Query("per_page") int perPage
    );

    // Tìm kiếm ảnh (Pexels: search)
    @GET("search")
    Call<ResponseBody> search(
            @Query("query") String query,
            @Query("page") int page,
            @Query("per_page") int perPage
    );
}
