package vn.edu.usth.flickrbrowser.ui.detail;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;

import vn.edu.usth.flickrbrowser.R;
import vn.edu.usth.flickrbrowser.core.model.PhotoItem;

public class DetailActivity extends AppCompatActivity {

    private PhotoItem photoItem;
    private ImageView btnFavorite;
    private boolean currentFav = false; // trạng thái tim hiện tại

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Nhận PhotoItem từ Explore/Search
        photoItem = (PhotoItem) getIntent().getSerializableExtra("PHOTO_ITEM");
        currentFav = getIntent().getBooleanExtra("is_favorite", false);

        if (!isValidPhotoItem(photoItem)) {
            Toast.makeText(this, "Ảnh không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        loadImageWithGlide();
        setupFavoriteButton();

        // Back gesture / system back → trả result
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                setResultAndFinish();
            }
        });
    }

    private void updateHeartIcon() {
        if (btnFavorite == null) return;
        btnFavorite.setImageResource(
                currentFav ? R.drawable.baseline_favorite_24
                        : R.drawable.outline_favorite_24
        );
    }

    private void setupFavoriteButton() {
        btnFavorite = findViewById(R.id.btnFavorite);
        updateHeartIcon();

        btnFavorite.setOnClickListener(v -> {
            currentFav = !currentFav; // toggle
            updateHeartIcon();
            Toast.makeText(this,
                    currentFav ? "Added to Favorites" : "Removed from Favorites",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private boolean isValidPhotoItem(PhotoItem item) {
        if (item == null) return false;
        return item.getFullUrl() != null && !item.getFullUrl().isEmpty()
                && item.id != null && !item.id.isEmpty();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(photoItem.title != null ? photoItem.title : "");
        }
    }

    private void loadImageWithGlide() {
        ImageView imageView = findViewById(R.id.photoView);
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
                .centerCrop()
                .into(imageView);

        imageView.setContentDescription(photoItem.title != null ? photoItem.title : "Flickr photo");
    }

    private void setResultAndFinish() {
        Intent data = new Intent();
        data.putExtra("PHOTO_ITEM", photoItem);      // gửi lại full object để add/remove dễ dàng
        data.putExtra("photo_id", photoItem.id);
        data.putExtra("is_favorite", currentFav);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        setResultAndFinish();
        return true;
    }
}
