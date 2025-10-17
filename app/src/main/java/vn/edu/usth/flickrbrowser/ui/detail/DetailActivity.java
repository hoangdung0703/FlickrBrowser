package vn.edu.usth.flickrbrowser.ui.detail;

import android.app.DownloadManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import vn.edu.usth.flickrbrowser.R;
import vn.edu.usth.flickrbrowser.core.model.PhotoItem;
import vn.edu.usth.flickrbrowser.core.util.HapticUtil;
import vn.edu.usth.flickrbrowser.ui.favorites.FavoritesViewModel;

public class DetailActivity extends AppCompatActivity {

    // ===== Keys nhận dữ liệu từ Explore/Search
    public static final String EXTRA_PHOTOS = "PHOTOS";
    public static final String EXTRA_START_INDEX = "START_INDEX";

    // ===== Keys trả kết quả cho màn trước
    public static final String RESULT_PHOTO = "PHOTO_ITEM";
    public static final String RESULT_IS_FAVORITE = "is_favorite";
    public static final String ACTION_FAV_CHANGED = "vn.edu.usth.flickrbrowser.FAVORITE_CHANGED";

    private ViewPager2 viewPager;
    private MaterialButton btnFavorite, btnShare, btnDownload, btnInfo;
    private TextView photoTitle, photoOwner;

    private ArrayList<PhotoItem> photos = new ArrayList<>();
    private int startIndex = 0;

    // ViewModel làm nguồn dữ liệu chung
    private FavoritesViewModel favVM;

    // Receiver để nhận thay đổi từ Home/Favorites khi activity đang mở
    private android.content.BroadcastReceiver favChangedReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Toolbar
        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tb.setNavigationOnClickListener(v -> setResultAndFinish());

        // ViewModel (single source of truth)
        favVM = new androidx.lifecycle.ViewModelProvider(this)
                .get(FavoritesViewModel.class);

        // Bind views
        viewPager   = findViewById(R.id.viewPager);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnShare    = findViewById(R.id.btnShare);
        btnDownload = findViewById(R.id.btnDownload);
        btnInfo     = findViewById(R.id.btnInfo);
        photoTitle  = findViewById(R.id.photoTitle);
        photoOwner  = findViewById(R.id.photoOwner);

        // Nhận dữ liệu
        try {
            Intent it = getIntent();
            ArrayList<PhotoItem> in = (ArrayList<PhotoItem>) it.getSerializableExtra(EXTRA_PHOTOS);
            if (in != null) photos = in;
            startIndex = it.getIntExtra(EXTRA_START_INDEX, 0);
            if (photos == null || photos.isEmpty()) {
                PhotoItem single = (PhotoItem) it.getSerializableExtra("PHOTO_ITEM");
                if (single != null) {
                    photos = new ArrayList<>();
                    photos.add(single);
                    startIndex = 0;
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Invalid Data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (photos == null || photos.isEmpty()) {
            Toast.makeText(this, "No photos to display", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        startIndex = Math.max(0, Math.min(startIndex, photos.size() - 1));

        // Adapter cho ViewPager2
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
            HapticUtil.medium(v);
            toggleFavorite(cur);
        });

        btnShare.setOnClickListener(v -> {
            PhotoItem cur = getCurrent();
            try {
                if (cur == null || cur.getFullUrl() == null || cur.getFullUrl().isEmpty()) {
                    Toast.makeText(this, "No URL to share", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent share = new Intent(Intent.ACTION_SEND);
                share.putExtra(Intent.EXTRA_TEXT, cur.getFullUrl());
                share.setType("text/plain");
                startActivity(Intent.createChooser(share, "Share via"));
            } catch (Exception e) {
                Toast.makeText(this, "Cannot be shared", Toast.LENGTH_SHORT).show();
            }
        });

        btnDownload.setOnClickListener(v -> {
            HapticUtil.medium(v);
            handleDownload();
        });
        btnInfo.setOnClickListener(v -> {
            HapticUtil.light(v);
            showInfoSheet();
        });

        // Back hệ thống → trả result
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { setResultAndFinish(); }
        });
    }

    // === DOWNLOAD ===
    private void handleDownload() {
        try {
            PhotoItem p = getCurrent();
            if (p == null) { Toast.makeText(this, "No images to load", Toast.LENGTH_SHORT).show(); return; }

            String url = p.getFullUrl();
            if (url == null || url.isEmpty()) { Toast.makeText(this, "Invalid image URL", Toast.LENGTH_SHORT).show(); return; }

            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            Uri uri = Uri.parse(url);
            DownloadManager.Request req = new DownloadManager.Request(uri);
            String cleanTitle = (p.title == null || p.title.trim().isEmpty()) ? p.id : p.title.trim();
            cleanTitle = cleanTitle.replaceAll("[^a-zA-Z0-9-_ ]", "-");
            String filename = cleanTitle + "_" + p.id + ".jpg";

            req.setTitle(filename);
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            req.setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, filename);

            dm.enqueue(req);
            HapticUtil.success(btnDownload);
            Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Download failed", Toast.LENGTH_SHORT).show();
        }
    }

    // === INFO ===
    private void showInfoSheet() {
        try {
            PhotoItem p = getCurrent();
            if (p == null) { Toast.makeText(this, "No image data available", Toast.LENGTH_SHORT).show(); return; }

            String title = (p.title == null) ? "" : p.title;
            String owner = (p.owner == null) ? "" : p.owner;

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
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(pageUrl)));
                        } else {
                            Toast.makeText(this, "Invalid URL", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Image information display error", Toast.LENGTH_SHORT).show();
        }
    }

    // ====== Favorite logic (ghi vào ViewModel + broadcast)
    private void toggleFavorite(@NonNull PhotoItem p) {
        boolean nowFav = !(favVM != null && favVM.isFavorite(p.id));

        // 1) Cập nhật nguồn chung
        if (favVM != null) {
            if (nowFav) favVM.addFavorite(p);
            else        favVM.removeFavorite(p);
        }

        // 2) Cập nhật icon ngay
        btnFavorite.setIconResource(
                nowFav ? R.drawable.baseline_favorite_24
                        : R.drawable.outline_favorite_24
        );

        // 3) Toast
        try {
            android.view.LayoutInflater inflater = getLayoutInflater();
            android.view.View layout = inflater.inflate(R.layout.toast_favorites, null);

            android.widget.ImageView imgIcon = layout.findViewById(R.id.imgIcon);
            android.widget.TextView tvMessage = layout.findViewById(R.id.tvMessage);

            if (nowFav) {
                imgIcon.setImageResource(R.drawable.baseline_favorite_24);
                tvMessage.setText("Added to Favorites");
            } else {
                imgIcon.setImageResource(R.drawable.outline_favorite_24);
                tvMessage.setText("Removed from Favorites");
            }

            android.widget.Toast toast = new android.widget.Toast(getApplicationContext());
            toast.setDuration(android.widget.Toast.LENGTH_SHORT);
            toast.setView(layout);
            toast.show();
        } catch (Exception e) {
            Toast.makeText(
                    this,
                    nowFav ? "Added to Favorites" : "Removed from Favorites",
                    Toast.LENGTH_SHORT
            ).show();
        }

        // 4) PHÁT BROADCAST để Home/Favorites nhận ngay
        try {
            Intent bc = new Intent(ACTION_FAV_CHANGED);
            bc.setPackage(getPackageName());
            bc.putExtra(RESULT_PHOTO, p);
            bc.putExtra(RESULT_IS_FAVORITE, nowFav);
            sendBroadcast(bc);
        } catch (Exception ignored) {}
    }

    private void updateMetaAndFavorite(PhotoItem cur) {
        if (cur == null) return;
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(cur.title != null ? cur.title : "");

        if (photoTitle != null) photoTitle.setText(cur.title != null ? cur.title : "");
        if (photoOwner != null) photoOwner.setText(cur.owner != null ? ("by " + cur.owner) : "");

        updateFavIcon(cur);
    }

    private void updateFavIcon(@NonNull PhotoItem p) {
        boolean isFavNow = favVM != null && favVM.isFavorite(p.id);
        btnFavorite.setIconResource(
                isFavNow ? R.drawable.baseline_favorite_24
                        : R.drawable.outline_favorite_24
        );
    }

    private PhotoItem getCurrent() {
        int pos = viewPager.getCurrentItem();
        return (pos >= 0 && pos < photos.size()) ? photos.get(pos) : null;
    }

    // ====== Trả kết quả cho màn trước
    private void setResultAndFinish() {
        try {
            PhotoItem current = getCurrent();
            if (current != null) {
                boolean isFavNow = favVM != null && favVM.isFavorite(current.id);
                Intent data = new Intent();
                data.putExtra(RESULT_PHOTO, current);
                data.putExtra(RESULT_IS_FAVORITE, isFavNow);
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
            iv.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
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

    // ====== Receiver: nghe thay đổi khi đang mở Detail
    @Override
    protected void onStart() {
        super.onStart();

        favChangedReceiver = new android.content.BroadcastReceiver() {
            @Override public void onReceive(android.content.Context ctx, Intent intent) {
                if (!ACTION_FAV_CHANGED.equals(intent.getAction())) return;

                PhotoItem p = (PhotoItem) intent.getSerializableExtra(RESULT_PHOTO);
                boolean isFav = intent.getBooleanExtra(RESULT_IS_FAVORITE, false);
                PhotoItem cur = getCurrent();
                if (p == null || cur == null) return;

                if (p.id != null && p.id.equals(cur.id)) {
                    btnFavorite.setIconResource(
                            isFav ? R.drawable.baseline_favorite_24
                                    : R.drawable.outline_favorite_24
                    );
                }
            }
        };

        IntentFilter f = new IntentFilter(ACTION_FAV_CHANGED);
        ContextCompat.registerReceiver(
                this, favChangedReceiver, f,
                ContextCompat.RECEIVER_NOT_EXPORTED
        );

        // Đồng bộ icon khi trở lại foreground (trường hợp ở nền không nghe được broadcast)
        PhotoItem cur = getCurrent();
        if (cur != null) updateFavIcon(cur);
    }

    @Override
    protected void onStop() {
        try { unregisterReceiver(favChangedReceiver); } catch (Exception ignored) {}
        super.onStop();
    }
}
