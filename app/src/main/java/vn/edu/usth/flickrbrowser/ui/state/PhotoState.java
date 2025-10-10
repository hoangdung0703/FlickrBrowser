package vn.edu.usth.flickrbrowser.ui.state;

import java.util.List;
import vn.edu.usth.flickrbrowser.core.model.PhotoItem; // import model ảnh mà nhóm đã define

public abstract class PhotoState {

    // Loading state
    public static class Loading extends PhotoState {}

    // Success state, có dữ liệu
    public static class Success extends PhotoState {
        private final List<PhotoItem> items;
        public Success(List<PhotoItem> items) {
            this.items = items;
        }
        public List<PhotoItem> getItems() {
            return items;
        }
    }

    // Empty state (không có dữ liệu)
    public static class Empty extends PhotoState {}

    // Error state, có message
    public static class Error extends PhotoState {
        private final String message;
        public Error(String message) {
            this.message = message;
        }
        public String getMessage() {
            return message;
        }
    }
}
