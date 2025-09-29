package vn.edu.usth.flickrbrowser.ui.common;

import android.graphics.Rect;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
public class GridSpacingDecoration extends RecyclerView.ItemDecoration {
    private final int spanCount; // số cột trong grid
    private final int spacingPx; // khoảng cách giữa các item(pixel)
    private final boolean includeEdge;  // có thêm khoảng cách ở biên không?

    public GridSpacingDecoration(int spanCount, int spacingPx, boolean includeEdge) {
        this.spanCount = spanCount;
        this.spacingPx = spacingPx;
        this.includeEdge = includeEdge;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect,@NonNull View view,@NonNull RecyclerView parent,@NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int column = position % spanCount;

        if (includeEdge){
            outRect.left = spacingPx - column * spacingPx / spanCount;
            outRect.right= (column + 1) * spacingPx / spanCount;
            if (position < spanCount) outRect.top = spacingPx;
            outRect.bottom = spacingPx;
        } else {
            outRect.left = column * spacingPx / spanCount;
            outRect.right= spacingPx - (column + 1) * spacingPx / spanCount;
            if (position >= spanCount) outRect.top = spacingPx;
        }
    }
}
// giữ khoảng cách đều