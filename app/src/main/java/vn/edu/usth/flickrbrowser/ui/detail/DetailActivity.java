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

        // Nhận PhotoItem từ Intent
        photoItem = getIntent().getParcelableExtra("PHOTO_ITEM");

        // Nếu không có hoặc không hợp lệ thì fallback mock
        if (!isValidPhotoItem(photoItem)) {
            photoItem = createMockPhotoItem();
            Toast.makeText(this, "Using mock data", Toast.LENGTH_SHORT).show();
        }

        // Setup toolbar
        setupToolbar();

        // Load ảnh
        loadImageWithGlide();
    }

    /** Kiểm tra PhotoItem hợp lệ */
    private boolean isValidPhotoItem(PhotoItem item) {
        if (item == null) return false;
        if (item.id == null || item.id.isEmpty()) return false;
        if (item.server == null || item.server.isEmpty()) return false;
        if (item.secret == null || item.secret.isEmpty()) return false;

        String url = item.getFullUrl();
        return url != null && !url.isEmpty();
    }

    /** Mock PhotoItem nếu dữ liệu không có */
    private PhotoItem createMockPhotoItem() {
        PhotoItem mock = new PhotoItem();
        mock.id = "53134234547";
        mock.server = "65535";
        mock.secret = "abc123def4";
        mock.title = "Beautiful Sunset - Test Image";
        return mock;
    }

    /** Setup Toolbar */
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(photoItem.title != null ? photoItem.title : "");
        }
    }

    /** Load ảnh với Glide */
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
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_dialog_alert)
                .into(imageView);

        imageView.setContentDescription(photoItem.title != null ? photoItem.title : "Flickr photo");
    }
}
