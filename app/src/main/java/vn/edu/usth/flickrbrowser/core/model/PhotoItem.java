package vn.edu.usth.flickrbrowser.core.model;
import java.io.Serializable;
public class PhotoItem implements Serializable {
    public String id = "";
    public String server = "";
    public String secret = "";
    public String title = "";
    public String owner = "";
    public String thumbUrl = "";
    public String fullUrl = "";

    public String getThumbUrl() {
        if (thumbUrl != null && !thumbUrl.isEmpty()) {
            return thumbUrl;
        }
        if (okFlickr()) {
            return "https://live.staticflickr.com/" + server + "/" + id + "_" + secret + "_w.jpg";
        }
        return "";
    }

    public String getFullUrl() {
        if (fullUrl != null && !fullUrl.isEmpty()) {
            return fullUrl;
        }
        if (okFlickr()) {
            return "https://live.staticflickr.com/" + server + "/" + id + "_" + secret + "_b.jpg";
        }
        return getThumbUrl();
    }

    private boolean okFlickr() {
        return notEmpty(server) && notEmpty(id) && notEmpty(secret);
    }

    private static boolean notEmpty(String s) {
        return s != null && !s.isEmpty();
    }
}
