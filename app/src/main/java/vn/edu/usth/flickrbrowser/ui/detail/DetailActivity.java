package vn.edu.usth.flickrbrowser.ui.detail;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;

import vn.edu.usth.flickrbrowser.R;
import vn.edu.usth.flickrbrowser.core.model.PhotoItem;

public class DetailActivity extends AppCompatActivity {

    private PhotoItem photoItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Nhận PhotoItem từ Explore/Search
        photoItem = getIntent().getParcelableExtra("PHOTO_ITEM");

        // Ngày 3: Validate dữ liệu ảnh
        if (!isValidPhotoItem(photoItem)) {
            Toast.makeText(this, "Ảnh không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        loadImageWithGlide();
    }

    /** Validation cơ bản PhotoItem */
    private boolean isValidPhotoItem(PhotoItem item) {
        if (item == null) return false;
        if (item.id == null || item.id.isEmpty()) return false;
        if (item.server == null || item.server.isEmpty()) return false;
        if (item.secret == null || item.secret.isEmpty()) return false;

        String url = item.getFullUrl();
        return url != null && !url.isEmpty();
    }

    /** Toolbar cơ bản (D1–D2), D5 gắn nút back */
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // D5: Back
            getSupportActionBar().setTitle(photoItem.title != null ? photoItem.title : "");
        }
    }

    /** Load ảnh với Glide + placeholder/error (D3) */
    private void loadImageWithGlide() {
        ImageView imageView = findViewById(R.id.imageViewDetail);
        String imageUrl = photoItem.getFullUrl();

        if (imageUrl == null || imageUrl.isEmpty()) {
            Toast.makeText(this, "Ảnh không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Glide.with(this)
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)  // Loading
                .error(android.R.drawable.ic_dialog_alert)        // Lỗi
                .into(imageView);

        imageView.setContentDescription(photoItem.title != null ? photoItem.title : "Flickr photo");
    }

    /** D5: xử lý back trên toolbar */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
