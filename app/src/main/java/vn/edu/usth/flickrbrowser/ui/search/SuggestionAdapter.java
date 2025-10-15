package vn.edu.usth.flickrbrowser.ui.search;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import java.util.List;
import vn.edu.usth.flickrbrowser.R;

public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.SuggestionViewHolder> {

    private final List<Suggestion> suggestions;
    private final OnSuggestionClickListener listener;
    private int selectedPosition = -1; // -1 nghĩa là không có item nào được chọn

    public interface OnSuggestionClickListener {
        void onSuggestionClick(String query);
    }

    public SuggestionAdapter(List<Suggestion> suggestions, OnSuggestionClickListener listener) {
        this.suggestions = suggestions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_suggestion_chip, parent, false);
        return new SuggestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
        Suggestion suggestion = suggestions.get(position);
        holder.chip.setText(suggestion.name);

        // ============================================================
        // THAY ĐỔI LOGIC Ở ĐÂY
        // ============================================================
        if (selectedPosition == position) {
            // Nếu là item đang được chọn: đổi màu nền, bỏ viền
            holder.chip.setChipBackgroundColorResource(R.color.md_theme_secondaryContainer);
            holder.chip.setChipStrokeWidth(0); // Bỏ viền
        } else {
            // Nếu không được chọn: trả về màu nền trong suốt và thêm viền
            holder.chip.setChipBackgroundColorResource(android.R.color.transparent);
            holder.chip.setChipStrokeWidthResource(R.dimen.chip_stroke_width); // Thêm viền
            holder.chip.setChipStrokeColorResource(R.color.md_theme_outline);
        }

        holder.chip.setOnClickListener(v -> {
            // Cập nhật vị trí được chọn
            int previousSelectedPosition = selectedPosition;
            selectedPosition = holder.getBindingAdapterPosition();

            // Chỉ cập nhật 2 item thay vì toàn bộ list, tối ưu hơn
            if (previousSelectedPosition != -1) {
                notifyItemChanged(previousSelectedPosition);
            }
            notifyItemChanged(selectedPosition);

            // Gọi listener để thực hiện tìm kiếm
            listener.onSuggestionClick(suggestion.name);
        });
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    static class SuggestionViewHolder extends RecyclerView.ViewHolder {
        Chip chip;
        public SuggestionViewHolder(@NonNull View itemView) {
            super(itemView);
            chip = (Chip) itemView;
        }
    }
}