package vn.edu.usth.flickrbrowser.ui.detail;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import vn.edu.usth.flickrbrowser.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;

import vn.edu.usth.flickrbrowser.core.model.PhotoItem;


public class DetailActivity extends AppCompatActivity {

    private PhotoItem photoItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set layout XML for this Activity
        setContentView(R.layout.activity_detail);

        // Receive PhotoItem from Intent (from Explore/Search)
        photoItem = getIntent().getParcelableExtra("PHOTO_ITEM");

        // Validate received data
        if (photoItem == null) {
            // If no data from Explore/Search, use mock data for testing
            photoItem = createMockPhotoItem();
            Toast.makeText(this, "Using test data", Toast.LENGTH_SHORT).show();
        }

        // Setup basic toolbar (NO BACK BUTTON YET)
        setupBasicToolbar();

        // Load image from team's PhotoItem
        loadImageFromPhotoItem();
    }

    /**
     * Setup basic toolbar
     */
    private void setupBasicToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Set title if available from PhotoItem
        if (getSupportActionBar() != null && photoItem != null && photoItem.title != null) {
            getSupportActionBar().setTitle(photoItem.title);
        }
    }

    /**
     * Create mock PhotoItem for testing
     * Follows team's PhotoItem structure
     */
    private PhotoItem createMockPhotoItem() {
        PhotoItem mockItem = new PhotoItem();
        mockItem.id = "53134234547";
        mockItem.server = "65535";
        mockItem.secret = "abc123def4";
        mockItem.title = "Test Image";

        return mockItem;
    }

    /**
     * Load high-quality image from team's PhotoItem
     * Uses getFullUrl() for best image quality
     */
    private void loadImageFromPhotoItem() {
        ImageView imageView = findViewById(R.id.imageViewDetail);

        // Get high-quality image URL from team's PhotoItem
        String imageUrl = photoItem.getFullUrl();

        // Validate URL
        if (imageUrl == null || imageUrl.isEmpty()) {
            Toast.makeText(this, "Invalid image URL", Toast.LENGTH_SHORT).show();
            return;
        }

        // Load image using Glide
        Glide.with(this)
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)  // Loading placeholder
                .error(android.R.drawable.ic_dialog_alert)        // Error placeholder
                .into(imageView);

        // Debug log for testing
        System.out.println("Loading image from: " + imageUrl);
    }

}