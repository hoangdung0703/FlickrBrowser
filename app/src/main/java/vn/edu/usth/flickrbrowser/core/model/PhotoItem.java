package vn.edu.usth.flickrbrowser.core.model;

import java.io.Serializable;

/**
 * Model đại diện cho một ảnh (dùng chung cho Flickr & Pexels).
 * Giữ logic tương thích: nếu là Flickr thì tự build link,
 * còn nếu là Pexels thì dùng sẵn URL từ JSON.
 */
public class PhotoItem implements Serializable {

    // ====== Các trường dữ liệu cơ bản ======
    public String id = "";
    public String server = "";
    public String secret = "";
    public String title = "";
    public String owner = "";
    public String thumbUrl = "";
    public String fullUrl = "";

    // ====== Lấy ảnh thumbnail ======
    public String getThumbUrl() {
        // 1️⃣ Nếu JSON đã có thumbUrl (Pexels hoặc fallback)
        if (thumbUrl != null && !thumbUrl.isEmpty()) {
            return thumbUrl;
        }

        // 2️⃣ Nếu là Flickr (tự build URL)
        if (okFlickr()) {
            return "https://live.staticflickr.com/" + server + "/" + id + "_" + secret + "_w.jpg";
        }

        // 3️⃣ Nếu không có dữ liệu
        return "";
    }

    // ====== Lấy ảnh full size ======
    public String getFullUrl() {
        // 1️⃣ Nếu JSON đã có fullUrl (Pexels)
        if (fullUrl != null && !fullUrl.isEmpty()) {
            return fullUrl;
        }

        // 2️⃣ Nếu là Flickr (tự build)
        if (okFlickr()) {
            return "https://live.staticflickr.com/" + server + "/" + id + "_" + secret + "_b.jpg";
        }

        // 3️⃣ Nếu không có gì -> fallback = thumbnail
        return getThumbUrl();
    }

    // ====== Kiểm tra xem có phải ảnh Flickr hợp lệ ======
    private boolean okFlickr() {
        return notEmpty(server) && notEmpty(id) && notEmpty(secret);
    }

    // ====== Tiện ích kiểm tra chuỗi rỗng ======
    private static boolean notEmpty(String s) {
        return s != null && !s.isEmpty();
    }

    // ====== Getter cho các thuộc tính cơ bản ======
    public String getTitle() {
        return title != null ? title : "";
    }

    public String getOwner() {
        return owner != null ? owner : "";
    }

    // Nếu API Pexels không có tags, để trống cho tương thích DetailActivity
    public String getTags() {
        return "";
    }
}

