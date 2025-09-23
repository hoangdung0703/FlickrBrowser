package vn.edu.usth.flickrbrowser.ui.detail;
import android.content.Context; import android.content.Intent; import android.os.Bundle; import android.widget.ImageView;
import androidx.annotation.Nullable; import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide; import vn.edu.usth.flickrbrowser.R;
public class DetailActivity extends AppCompatActivity {
    public static void open(Context ctx, String url){ Intent i=new Intent(ctx, DetailActivity.class); i.putExtra("url", url); ctx.startActivity(i); }
    @Override protected void onCreate(@Nullable Bundle b){ super.onCreate(b); setContentView(R.layout.activity_detail);
        String url=getIntent().getStringExtra("url"); ImageView iv=findViewById(R.id.image); Glide.with(this).load(url).into(iv); }
}
