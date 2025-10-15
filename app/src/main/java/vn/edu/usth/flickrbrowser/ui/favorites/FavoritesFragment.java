package vn.edu.usth.flickrbrowser.ui.favorites;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;

import vn.edu.usth.flickrbrowser.R;
import vn.edu.usth.flickrbrowser.core.model.PhotoItem;
import vn.edu.usth.flickrbrowser.ui.detail.DetailActivity;

public class FavoritesFragment extends Fragment {

    // ---- Keys/Action lấy từ DetailActivity ----
    private static final String ACTION_FAV_CHANGED = DetailActivity.ACTION_FAV_CHANGED;
    private static final String EXTRA_PHOTOS       = DetailActivity.EXTRA_PHOTOS;
    private static final String EXTRA_START_INDEX  = DetailActivity.EXTRA_START_INDEX;
    private static final String RESULT_PHOTO       = DetailActivity.RESULT_PHOTO;
    private static final String RESULT_IS_FAVORITE = DetailActivity.RESULT_IS_FAVORITE;

    private FavoritesViewModel vm;
    private FavoritesAdapter adapter;

    // Mở DetailActivity và nhận result quay về
    private final ActivityResultLauncher<Intent> detailLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                try {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        PhotoItem returned = (PhotoItem) result.getData().getSerializableExtra(RESULT_PHOTO);
                        boolean isFav = result.getData().getBooleanExtra(RESULT_IS_FAVORITE, false);
                        if (returned != null) {
                            if (isFav) vm.addFavorite(returned);
                            else vm.removeFavorite(returned);
                        }
                    }
                } catch (Exception e) {
                    android.widget.Toast.makeText(requireContext(),
                            "Error when updating favorites", android.widget.Toast.LENGTH_SHORT).show();
                }
            });

    // Receiver nhận sự kiện tim/un-tim từ DetailActivity
    private final BroadcastReceiver favReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (intent == null || !ACTION_FAV_CHANGED.equals(intent.getAction())) return;
            try {
                PhotoItem item = (PhotoItem) intent.getSerializableExtra(RESULT_PHOTO);
                boolean isFav = intent.getBooleanExtra(RESULT_IS_FAVORITE, false);
                if (item == null || item.id == null) return;

                if (isFav) vm.addFavorite(item);
                else vm.removeFavorite(item);
            } catch (Exception ignored) {
                android.widget.Toast.makeText(requireContext(),
                        "Error when updating favorites", android.widget.Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Views
        SwipeRefreshLayout swipe = view.findViewById(R.id.swipeFavorites);
        RecyclerView rv = view.findViewById(R.id.rvFavorites);
        View empty = view.findViewById(R.id.emptyView);

        // Grid 2 cột + padding theo insets
        rv.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        final int space = getResources().getDimensionPixelSize(R.dimen.spacing_xs);
        rv.setClipToPadding(false);
        rv.setPadding(rv.getPaddingLeft(), rv.getPaddingTop(),
                rv.getPaddingRight(), rv.getPaddingBottom() + space);

        final int[] sysBottomHolder = new int[]{0};
        ViewCompat.setOnApplyWindowInsetsListener(rv, (v, insets) -> {
            int sysBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            sysBottomHolder[0] = sysBottom;
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(),
                    v.getPaddingRight(), space + sysBottom);
            rv.invalidateItemDecorations();
            return insets;
        });

        // Spacing cho grid + đệm hàng cuối
        rv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View v,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.set(space, space, space, space);

                int position = parent.getChildAdapterPosition(v);
                if (position == RecyclerView.NO_POSITION) return;

                RecyclerView.Adapter<?> ad = parent.getAdapter();
                if (ad == null) return;

                GridLayoutManager glm = (GridLayoutManager) parent.getLayoutManager();
                if (glm == null) return;

                int itemCount = ad.getItemCount();
                int span = glm.getSpanCount();
                if (itemCount == 0 || span <= 0) return;

                int rem = itemCount % span;
                int lastRowStart = Math.max(0, itemCount - (rem == 0 ? span : rem));
                if (position >= lastRowStart) {
                    int extraTail = space * 14; // kéo giãn phần cuối để không bị kẹt sau nav bar
                    outRect.bottom = space + sysBottomHolder[0] + extraTail;
                }
            }
        });

        // ViewModel dùng scope Activity để chia sẻ giữa các tab
        vm = new ViewModelProvider(requireActivity()).get(FavoritesViewModel.class);

        // Adapter: click ♥ → remove; click item → mở DetailActivity
        adapter = new FavoritesAdapter(
                new ArrayList<>(),
                item -> vm.removeFavorite(item),
                (item, position) -> {
                    try {
                        Intent i = new Intent(requireContext(), DetailActivity.class);
                        i.putExtra(EXTRA_PHOTOS, new ArrayList<>(adapter.getCurrentData()));
                        i.putExtra(EXTRA_START_INDEX, position);
                        i.putExtra("is_favorite", true);
                        detailLauncher.launch(i);
                    } catch (Exception e) {
                        android.widget.Toast.makeText(requireContext(),
                                "Error opening detail", android.widget.Toast.LENGTH_SHORT).show();
                    }
                }
        );
        rv.setAdapter(adapter);

        // Pull-to-refresh: reload từ ViewModel storage
        swipe.setOnRefreshListener(() -> {
            try {
                vm.refresh();
                rv.post(rv::invalidateItemDecorations);
            } finally {
                swipe.setRefreshing(false);
            }
        });

        // Quan sát dữ liệu yêu thích
        vm.getFavorites().observe(getViewLifecycleOwner(), list -> {
            try {
                if (list == null || list.isEmpty()) {
                    empty.setVisibility(View.VISIBLE);
                    adapter.update(new ArrayList<>());
                    rv.post(rv::invalidateItemDecorations);
                } else {
                    empty.setVisibility(View.GONE);
                    adapter.update(list);
                }
            } catch (Exception e) {
                android.widget.Toast.makeText(requireContext(),
                        "Error displaying favorites", android.widget.Toast.LENGTH_SHORT).show();
            } finally {
                swipe.setRefreshing(false);
            }
        });
    }

    // Khuyến nghị: đăng ký/unregister Receiver theo vòng đời hiển thị (an toàn rò rỉ)
    @Override
    public void onStart() {
        super.onStart();
        try {
            IntentFilter f = new IntentFilter(ACTION_FAV_CHANGED);
            Context app = requireContext().getApplicationContext();
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                app.registerReceiver(favReceiver, f, Context.RECEIVER_NOT_EXPORTED);
            } else {
                // AndroidX Core: ContextCompat.registerReceiver(Context, BroadcastReceiver, IntentFilter, int flags)
                ContextCompat.registerReceiver(app, favReceiver, f, ContextCompat.RECEIVER_NOT_EXPORTED);
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            requireContext().getApplicationContext().unregisterReceiver(favReceiver);
        } catch (Exception ignored) {}
    }
}
