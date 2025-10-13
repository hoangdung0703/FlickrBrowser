package vn.edu.usth.flickrbrowser.core.util;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Utility class for validating URLs.
 * Used in ActionUtils for Share / Download / Open actions.
 */
public final class UrlUtils {
    private static final String TAG = "UrlUtils";

    private UrlUtils() {}

    /**
     * Kiểm tra URL hợp lệ và phải là HTTP hoặc HTTPS.
     *
     * @param url chuỗi URL cần kiểm tra
     * @return true nếu hợp lệ, false nếu không
     */
    public static boolean isValidHttpUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            Log.d(TAG, "URL is empty or null");
            return false;
        }

        try {
            Uri uri = Uri.parse(url);
            if (uri == null) {
                Log.d(TAG, "Failed to parse URI: " + url);
                return false;
            }

            String scheme = uri.getScheme();
            String host = uri.getHost();

            boolean validScheme = "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
            boolean validHost = !TextUtils.isEmpty(host);

            if (!validScheme || !validHost) {
                Log.d(TAG, "Invalid scheme or host: " + url);
                return false;
            }

            // Kiểm tra sâu hơn bằng java.net.URL
            new URL(url);
            return true;

        } catch (MalformedURLException e) {
            Log.w(TAG, "Malformed URL: " + url, e);
            return false;
        } catch (Throwable t) {
            Log.e(TAG, "Error validating URL: " + url, t);
            return false;
        }
    }
}
