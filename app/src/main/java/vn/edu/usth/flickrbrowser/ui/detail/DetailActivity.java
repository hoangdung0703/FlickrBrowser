package vn.edu.usth.flickrbrowser.ui.detail;

import android.app.DownloadManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import vn.edu.usth.flickrbrowser.R;
import vn.edu.usth.flickrbrowser.core.model.PhotoItem;

public class DetailActivity extends AppCompatActivity {

    // ===== Keys nhận dữ liệu từ Explore/Search
    public static final String EXTRA_PHOTOS = "PHOTOS";
    public static final String EXTRA_START_INDEX = "START_INDEX";

    // ===== Keys trả kết quả cho màn trước (giống bản cũ)
    public static final String RESULT_PHOTO = "PHOTO_ITEM";
    public static final String RESULT_IS_FAVORITE = "is_favorite";

    // ===== Lưu local “đã tim” để không mất khi quay lại
    private static final String PREFS = "favorites_prefs";
    private static final String PREF_FAV_IDS = "fav_ids";

    private ViewPager2 viewPager;
    private MaterialButton btnFavorite, btnShare, btnDownload, btnInfo;
    private TextView photoTitle, photoOwner;
    private PhotoItem photoItem;

    private ArrayList<PhotoItem> photos = new ArrayList<>();
    private int startIndex = 0;

    // Trạng thái tim: id đã được tim
    private final HashSet<String> favIds = new HashSet<>();

    // === Helper: Lấy ảnh hiện tại an toàn ===
    private @androidx.annotation.Nullable PhotoItem getCurrentPhoto() {
        if (photoItem != null) return photoItem; // nếu chỉ có 1 ảnh (mở từ Explore/Fav)

        if (photos != null && !photos.isEmpty()) {
            if (startIndex < 0 || startIndex >= photos.size()) return null;
            return photos.get(startIndex);
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Toolbar
        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tb.setNavigationOnClickListener(v -> setResultAndFinish());

        // Bind views
        viewPager   = findViewById(R.id.viewPager);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnShare    = findViewById(R.id.btnShare);
        btnDownload = findViewById(R.id.btnDownload);
        btnInfo     = findViewById(R.id.btnInfo);
        photoTitle  = findViewById(R.id.photoTitle);
        photoOwner  = findViewById(R.id.photoOwner);

        // Đọc danh sách “đã tim” từ SharedPreferences
        restoreFavIds();

        // Nhận PhotoItem từ Explore/Search
        try {
            Intent it = getIntent();
            ArrayList<PhotoItem> in = (ArrayList<PhotoItem>) it.getSerializableExtra(EXTRA_PHOTOS);
            if (in != null) photos = in;
            startIndex = it.getIntExtra(EXTRA_START_INDEX, 0);
            // Sử dụng fallback để đọc photoitem
            if (photos == null || photos.isEmpty()) {
                PhotoItem single = (PhotoItem) it.getSerializableExtra("PHOTO_ITEM");
                if (single != null) {
                    photos = new ArrayList<>();
                    photos.add(single);
                    startIndex = 0;
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Dữ liệu không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Validate
        if (photos == null || photos.isEmpty()) {
            Toast.makeText(this, "Không có ảnh để hiển thị", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        startIndex = Math.max(0, Math.min(startIndex, photos.size() - 1));

        // Adapter nội bộ cho ViewPager2
        PhotoPagerAdapter adapter = new PhotoPagerAdapter(photos);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setCurrentItem(startIndex, false);

        // Cập nhật tiêu đề + icon tim cho trang hiện tại
        updateMetaAndFavorite(getCurrent());

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int position) {
                updateMetaAndFavorite(getCurrent());
            }
        });

        // ====== Click handlers
        btnFavorite.setOnClickListener(v -> {
            PhotoItem cur = getCurrent();
            if (cur == null) return;
            toggleFavorite(cur);
        });

        btnShare.setOnClickListener(v -> {
            PhotoItem cur = getCurrent();
            try {
                if (cur == null || cur.getFullUrl() == null || cur.getFullUrl().isEmpty()) {
                    Toast.makeText(this, "Không có URL để chia sẻ", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent share = new Intent(Intent.ACTION_SEND);
                share.putExtra(Intent.EXTRA_TEXT, cur.getFullUrl());
                share.setType("text/plain");
                startActivity(Intent.createChooser(share, "Share via"));
            } catch (Exception e) {
                Toast.makeText(this, "Không thể chia sẻ", Toast.LENGTH_SHORT).show();
            }
        });

        btnDownload.setOnClickListener(v -> handleDownload());
        btnInfo.setOnClickListener(v -> showInfoSheet());



        // Back hệ thống → trả result
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { setResultAndFinish(); }
        });
    }

    // === DOWNLOAD ===
    private void handleDownload() {
        try {
            PhotoItem p = getCurrentPhoto();
            if (p == null) { Toast.makeText(this, "Không có ảnh để tải", Toast.LENGTH_SHORT).show(); return; }

            String url = p.getFullUrl();
            if (url == null || url.isEmpty()) { Toast.makeText(this, "URL ảnh không hợp lệ", Toast.LENGTH_SHORT).show(); return; }

            android.app.DownloadManager dm = (android.app.DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            android.net.Uri uri = android.net.Uri.parse(url);
            android.app.DownloadManager.Request req = new android.app.DownloadManager.Request(uri);
            String cleanTitle = (p.title == null || p.title.trim().isEmpty()) ? p.id : p.title.trim();
            cleanTitle = cleanTitle.replaceAll("[^a-zA-Z0-9-_ ]", "-");
            String filename = cleanTitle + "_" + p.id + ".jpg";

            req.setTitle(filename);
            req.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            // Thư mục Download công khai
            req.setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, filename);

            dm.enqueue(req);
            Toast.makeText(this, "Đang tải xuống…", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Tải xuống thất bại", Toast.LENGTH_SHORT).show();
        }
    }

    // === INFO ===
    private void showInfoSheet() {
        try {
            PhotoItem p = getCurrentPhoto();
            if (p == null) { Toast.makeText(this, "Không có dữ liệu ảnh", Toast.LENGTH_SHORT).show(); return; }

            String title = (p.title == null) ? "" : p.title;
            String owner = (p.owner == null) ? "" : p.owner;

            // Ưu tiên “trang ảnh” nếu có, nếu không dùng fullUrl
            final String pageUrl = (p.getPageUrl() != null && !p.getPageUrl().isEmpty())
                    ? p.getPageUrl()
                    : p.getFullUrl();

            String message = "Tiêu đề: " + title +
                    "\nTác giả: " + owner +
                    "\n\nURL ảnh: " + p.getFullUrl();

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Thông tin ảnh")
                    .setMessage(message)
                    .setPositiveButton("Mở trang nguồn", (d, i) -> {
                        if (pageUrl != null && !pageUrl.isEmpty()) {
                            startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(pageUrl)));
                        } else {
                            Toast.makeText(this, "Không có URL hợp lệ", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNeutralButton("Chia sẻ", (d, i) -> {
                        Intent share = new Intent(Intent.ACTION_SEND);
                        share.setType("text/plain");
                        share.putExtra(Intent.EXTRA_TEXT, pageUrl);
                        startActivity(Intent.createChooser(share, "Share via"));
                    })
                    .setNegativeButton("Đóng", null)
                    .show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi hiển thị thông tin ảnh", Toast.LENGTH_SHORT).show();
        }
    }



    // ====== Favorite logic (update + exception)
    private void toggleFavorite(@NonNull PhotoItem p) {
        boolean nowFav;
        if (favIds.contains(p.id)) {
            favIds.remove(p.id);
            nowFav = false;
        } else {
            favIds.add(p.id);
            nowFav = true;
        }
        persistFavIds();              // lưu ngay để không “mất tim” khi quay lại
        updateFavIcon(p);             // đổi icon
        Toast.makeText(this, nowFav ? "Added to Favorites" : "Removed from Favorites", Toast.LENGTH_SHORT).show();
    }

    private void updateMetaAndFavorite(PhotoItem cur) {
        if (cur == null) return;
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(cur.title != null ? cur.title : "");

        if (photoTitle != null) photoTitle.setText(cur.title != null ? cur.title : "");
        if (photoOwner != null) photoOwner.setText(cur.owner != null ? ("by " + cur.owner) : "");

        updateFavIcon(cur);
    }

    private void updateFavIcon(@NonNull PhotoItem p) {
        btnFavorite.setIconResource(
                favIds.contains(p.id) ? R.drawable.baseline_favorite_24
                        : R.drawable.outline_favorite_24
        );
    }

    private PhotoItem getCurrent() {
        int pos = viewPager.getCurrentItem();
        return (pos >= 0 && pos < photos.size()) ? photos.get(pos) : null;
    }

    // ====== SharedPreferences: lưu/khôi phục danh sách id đã tim
    private void persistFavIds() {
        try {
            SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
            sp.edit().putStringSet(PREF_FAV_IDS, new HashSet<>(favIds)).apply();
        } catch (Exception ignored) { /* tránh crash nếu có lỗi I/O */ }
    }

    private void restoreFavIds() {
        try {
            SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
            Set<String> saved = sp.getStringSet(PREF_FAV_IDS, null);
            if (saved != null) favIds.addAll(saved);
        } catch (Exception ignored) { }
    }

    // ====== Trả kết quả giống file cũ (để màn trước có thể cập nhật ngay)
    private void setResultAndFinish() {
        try {
            PhotoItem current = getCurrent();
            if (current != null) {
                Intent data = new Intent();
                data.putExtra(RESULT_PHOTO, current);
                data.putExtra(RESULT_IS_FAVORITE, favIds.contains(current.id));
                setResult(RESULT_OK, data);
            }
        } catch (Exception ignored) { }
        finish();
    }

    @Override public boolean onSupportNavigateUp() {
        setResultAndFinish();
        return true;
    }

    // ====== Adapter nội bộ cho ViewPager2
    private static class PhotoPagerAdapter extends RecyclerView.Adapter<PhotoVH> {
        private final List<PhotoItem> items;
        PhotoPagerAdapter(List<PhotoItem> items) { this.items = items; }

        @NonNull @Override
        public PhotoVH onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            android.widget.FrameLayout root = new android.widget.FrameLayout(parent.getContext());
            root.setLayoutParams(new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.MATCH_PARENT
            ));
            android.widget.ImageView iv = new android.widget.ImageView(parent.getContext());
            iv.setAdjustViewBounds(true);
            iv.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
            root.addView(iv, new android.widget.FrameLayout.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
            ));
            return new PhotoVH(root, iv);
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoVH holder, int position) {
            PhotoItem item = items.get(position);
            String url = (item != null) ? item.getFullUrl() : null;
            Glide.with(holder.image.getContext())
                    .load(url)
                    .placeholder(R.drawable.placeholder_grey)
                    .error(R.drawable.placeholder_grey)
                    .into(holder.image);
            holder.image.setContentDescription(item != null && item.title != null ? item.title : "photo");
        }

        @Override public int getItemCount() { return items != null ? items.size() : 0; }
    }

    private static class PhotoVH extends RecyclerView.ViewHolder {
        final android.widget.ImageView image;
        PhotoVH(@NonNull android.view.View itemView, @NonNull android.widget.ImageView image) {
            super(itemView);
            this.image = image;
        }
    }
}