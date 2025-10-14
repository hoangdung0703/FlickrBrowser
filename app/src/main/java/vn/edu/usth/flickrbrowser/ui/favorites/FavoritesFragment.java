package vn.edu.usth.flickrbrowser.ui.favorites;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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

    private static final String RESULT_PHOTO = DetailActivity.RESULT_PHOTO;
    private static final String RESULT_IS_FAVORITE  = DetailActivity.RESULT_IS_FAVORITE ;

    private FavoritesViewModel vm;
    private FavoritesAdapter adapter;

    public FavoritesFragment() {
    }

    // Launcher để mở DetailActivity và nhận result quay về
    private final ActivityResultLauncher<Intent> detailLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                try {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        PhotoItem returned = (PhotoItem) result.getData().getSerializableExtra(RESULT_PHOTO);
                        boolean isFav = result.getData().getBooleanExtra(RESULT_IS_FAVORITE, false);
                        if (returned != null) {
                            if (isFav) {
                                vm.addFavorite(returned);    // đảm bảo có trong favorites
                            } else {
                                vm.removeFavorite(returned); // nếu đã bỏ tim trong Detail → xoá khỏi danh sách
                            }
                        }
                    }

                } catch (Exception e) {
                    android.widget.Toast.makeText(requireContext(), "Error when update Favorite", android.widget.Toast.LENGTH_SHORT).show();
                }

});

private final android.content.BroadcastReceiver favReceiver = new android.content.BroadcastReceiver() {
    @Override public void onReceive(android.content.Context context, Intent intent) {
        if (intent == null || !DetailActivity.ACTION_FAV_CHANGED.equals(intent.getAction())) return;
        try {
            PhotoItem item = (PhotoItem) intent.getSerializableExtra(DetailActivity.RESULT_PHOTO);
            boolean isFav = intent.getBooleanExtra(DetailActivity.RESULT_IS_FAVORITE, false);
            if (item == null || item.id == null) return;

            if (isFav) vm.addFavorite(item);
            else vm.removeFavorite(item);
        } catch (Exception ignored) {
            android.widget.Toast.makeText(requireContext(), "Error when update Favorite", android.widget.Toast.LENGTH_SHORT).show();
        }
    }
};

@Override
public View onCreateView(@NonNull LayoutInflater inflater,
                         @Nullable ViewGroup container,
                         @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_favorites, container, false);
}

@Override
public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    SwipeRefreshLayout swipe = view.findViewById(R.id.swipeFavorites);

    RecyclerView rv = view.findViewById(R.id.rvFavorites);
    final View empty = view.findViewById(R.id.emptyView);

    // Grid 2 columns
    rv.setLayoutManager(new GridLayoutManager(requireContext(), 2));

    // Spacing

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
                int extraTail = space * 14;
                outRect.bottom = space + sysBottomHolder[0] + extraTail;
            }
        }


    });

    // ViewModel (scope Activity để chia sẻ với Explore/Search)
    vm = new ViewModelProvider(requireActivity()).get(FavoritesViewModel.class);

    // Adapter:
    //  - click vào ♥: remove khỏi favorites (như cũ)
    //  - click vào item: mở DetailActivity (mới thêm)
    adapter = new FavoritesAdapter(
            new ArrayList<>(),
            item -> vm.removeFavorite(item),
            (item, position) -> {
                try {
                    Intent i = new Intent(requireContext(), DetailActivity.class);
                    i.putExtra(DetailActivity.EXTRA_PHOTOS, new ArrayList<>(adapter.getCurrentData()));
                    i.putExtra(DetailActivity.EXTRA_START_INDEX, position);
                    i.putExtra("is_favorite", true);
                    detailLauncher.launch(i);
                } catch (Exception e) {
                    android.widget.Toast.makeText(requireContext(), "Error opening detail", android.widget.Toast.LENGTH_SHORT).show();
                }
            }
    );
    rv.setAdapter(adapter);

    swipe.setOnRefreshListener(() -> {
        try {
            vm.refresh();
            rv.post(rv::invalidateItemDecorations);
        } finally {
            swipe.setRefreshing(false);
        }
    });
    // Observe LiveData → update list & EmptyView
    vm.getFavorites().observe(getViewLifecycleOwner(), list -> {
        try {
            if (list == null || list.isEmpty()) {
                empty.setVisibility(View.VISIBLE);
                adapter.update(new ArrayList<>()); // clear UI
                rv.post(rv::invalidateItemDecorations);
            } else {
                empty.setVisibility(View.GONE);
                adapter.update(list);
            }
            swipe.setRefreshing(false);
        } catch (Exception e) {
            android.widget.Toast.makeText(requireContext(), "Error displaying favorites", android.widget.Toast.LENGTH_SHORT).show();
        }
        swipe.setRefreshing(false);
    });

}

@Override
public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    try {
        android.content.IntentFilter f = new android.content.IntentFilter(DetailActivity.ACTION_FAV_CHANGED);
        android.content.Context appCtx = requireContext().getApplicationContext();

        if (android.os.Build.VERSION.SDK_INT >= 33) {
            appCtx.registerReceiver(
                    favReceiver,
                    f,
                    android.content.Context.RECEIVER_NOT_EXPORTED
            );
        } else {
            androidx.core.content.ContextCompat.registerReceiver(
                    appCtx, favReceiver, f,
                    androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
            );
        }
    } catch (Exception ignored) {}
}

@Override
public void onDestroy() {
    super.onDestroy();
    try {
        requireContext().getApplicationContext().unregisterReceiver(favReceiver);
    } catch (Exception ignored) {}
}

}