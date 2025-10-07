package vn.edu.usth.flickrbrowser.ui.detail;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

import vn.edu.usth.flickrbrowser.R;
import vn.edu.usth.flickrbrowser.core.model.PhotoItem;
import vn.edu.usth.flickrbrowser.ui.favorites.FavoritesViewModel;

public class DetailActivity extends AppCompatActivity {

    private PhotoItem photoItem;
    private ImageView btnFavorite;
    private boolean currentFav = false; // trạng thái cục bộ trong màn Detail

    private FavoritesViewModel favVM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Nhận PhotoItem từ Explore/Search - CHANGED TO GET SERIALIZABLE
        photoItem = (PhotoItem) getIntent().getSerializableExtra("PHOTO_ITEM");
        currentFav = getIntent().getBooleanExtra("is_favorite", false);

        // Ngày 3: Validate dữ liệu ảnh
        if (!isValidPhotoItem(photoItem)) {
            Toast.makeText(this, "Ảnh không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        favVM = new ViewModelProvider(this).get(FavoritesViewModel.class);
        currentFav = favVM.isFavorite(photoItem.id);

        setupToolbar();
        loadImageWithGlide();
        setupFavoriteButton();

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
            currentFav = !currentFav; // toggle trạng thái cục bộ
            // Cập nhật trạng thái trong ViewModel
            if (currentFav) {
                favVM.addFavorite(photoItem);
            } else {
                favVM.removeFavorite(photoItem);
            }

            updateHeartIcon();
            Toast.makeText(this,
                    currentFav ? "Added to Favorites" : "Removed from Favorites",
                    Toast.LENGTH_SHORT).show();
        });
    }


    /** Validation cơ bản PhotoItem */
    private boolean isValidPhotoItem(PhotoItem item) {
        if (item == null) return false;
        // FlickrRepo provides fullUrl directly. We should validate that.
        // We can also check the ID as a basic sanity measure.
        return item.getFullUrl() != null && !item.getFullUrl().isEmpty() &&
               item.id != null && !item.id.isEmpty();
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
        ImageView imageView = findViewById(R.id.photoView);
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

    private void setResultAndFinish() {
        Intent data = new Intent();
        data.putExtra("photo_id", photoItem.id);
        data.putExtra("is_favorite", currentFav);
        setResult(RESULT_OK, data);
        finish();
    }
    /** D5: xử lý back trên toolbar */
    @Override
    public boolean onSupportNavigateUp() {
        setResultAndFinish();
        return true;
    }

}
