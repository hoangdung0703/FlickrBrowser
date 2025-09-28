package vn.edu.usth.flickrbrowser.ui.detail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.squareup.picasso.Picasso;

import vn.edu.usth.flickrbrowser.R;
import vn.edu.usth.flickrbrowser.core.model.PhotoItem;

public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_PHOTO = "extra_photo";

    private PhotoView photoView;
    private TextView title, owner;
    private ChipGroup chipGroupTags;
    private ImageButton btnFavorite, btnShare, btnDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        photoView = findViewById(R.id.photoView);
        title = findViewById(R.id.photoTitle);
        owner = findViewById(R.id.photoOwner);
        chipGroupTags = findViewById(R.id.chipGroupTags);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnShare = findViewById(R.id.btnShare);
        btnDownload = findViewById(R.id.btnDownload);

        PhotoItem photo = (PhotoItem) getIntent().getSerializableExtra(EXTRA_PHOTO);
        if (photo != null) {
            bindPhoto(photo);
        }
    }

    private void bindPhoto(PhotoItem photo) {
        // Load ảnh full size
        Picasso.get()
                .load(photo.getUrl_m())
                .placeholder(R.drawable.placeholder)
                .into(photoView);

        title.setText(photo.getTitle());
        owner.setText("by " + photo.getOwner());

        chipGroupTags.removeAllViews();
        if (photo.getTags() != null) {
            for (String tag : photo.getTags().split(" ")) {
                Chip chip = new Chip(this);
                chip.setText(tag);
                chip.setOnClickListener(v -> {
                    // TODO: trigger search by tag
                });
                chipGroupTags.addView(chip);
            }
        }

        btnFavorite.setOnClickListener(v -> {
            // TODO: save/remove from favorites
        });

        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, photo.getUrl_m());
            shareIntent.setType("text/plain");
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });

        btnDownload.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(photo.getUrl_m()));
            startActivity(browserIntent);
        });
    }
}