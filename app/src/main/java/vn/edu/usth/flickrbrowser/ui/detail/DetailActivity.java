
package vn.edu.usth.flickrbrowser.ui.detail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import vn.edu.usth.flickrbrowser.R;
import vn.edu.usth.flickrbrowser.core.util.NetworkUtils;

public class DetailActivity extends AppCompatActivity {

    private String photoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Nhận URL từ Intent
        photoUrl = getIntent().getStringExtra("photo_url");

        // Gắn view
        Button btnShare = findViewById(R.id.btn_share);
        Button btnOpen = findViewById(R.id.btn_open);
        Button btnBack = findViewById(R.id.btn_back);

        // Xử lý Share
        btnShare.setOnClickListener(v -> {
            if (!NetworkUtils.hasInternet(this)) {
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
                return;
            }
            if (photoUrl == null || photoUrl.isEmpty()) {
                Toast.makeText(this, "URL is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, photoUrl);
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });

        // Xử lý Open
        btnOpen.setOnClickListener(v -> {
            if (!NetworkUtils.hasInternet(this)) {
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
                return;
            }
            if (photoUrl == null || photoUrl.isEmpty()) {
                Toast.makeText(this, "URL is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent openIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(photoUrl));
            startActivity(openIntent);
        });

        // Xử lý Back
        btnBack.setOnClickListener(v -> finish());
    }
}
