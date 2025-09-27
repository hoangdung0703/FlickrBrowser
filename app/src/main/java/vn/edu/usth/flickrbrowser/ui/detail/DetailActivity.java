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

        // Nhận PhotoItem từ Intent (từ Explore/Search)
        photoItem = getIntent().getParcelableExtra("PHOTO_ITEM");

        // VALIDATION
        if (!isValidPhotoItem(photoItem)) {
            Toast.makeText(this, "Invalid Image", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        loadImageWithGlide();
    }

    /**
     *VALIDATION TOÀN DIỆN
     * Vì PhotoItem từ Explore/Search có thể có nhiều lỗi khác nhau
     */
    private boolean isValidPhotoItem(PhotoItem item) {
        // Check if item is null
        if (item == null) {
            return false;
        }

        // PhotoItem cần id, server, secret để tạo URL
        if (item.id == null || item.id.isEmpty()) {
            return false;
        }
        if (item.server == null || item.server.isEmpty()) {
            return false;
        }
        if (item.secret == null || item.secret.isEmpty()) {
            return false;
        }

        //Check URL được tạo ra
        String url = item.getFullUrl();
        return url != null && !url.isEmpty();
    }

    /**
     * Setup Toolbar
     */
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null && photoItem.title != null) {
            getSupportActionBar().setTitle(photoItem.title);
        }
    }
     // GLIDE PLACEHOLDER/ERROR
    private void loadImageWithGlide() {
        ImageView imageView = findViewById(R.id.imageViewDetail);
        String imageUrl = photoItem.getFullUrl();

        // DEFENSE IN DEPTH: Check lần cuối trước khi glide
        if (imageUrl == null || imageUrl.isEmpty()) {
            Toast.makeText(this, "Invalid Image", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // GLIDE VỚI PLACEHOLDER/ERROR HOÀN CHỈNH
        Glide.with(this)
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)  // Ảnh tạm khi loading
                .error(android.R.drawable.ic_dialog_alert)        // Ảnh lỗi khi fail
                .into(imageView);
    }
}