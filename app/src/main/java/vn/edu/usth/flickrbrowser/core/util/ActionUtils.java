package vn.edu.usth.flickrbrowser.core.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import vn.edu.usth.flickrbrowser.core.model.PhotoItem;

public final class ActionUtils {
    private ActionUtils() {}

    // -------- messages (EN) --------
    private static final String MSG_INVALID_URL = "Invalid link.";
    private static final String MSG_NO_NETWORK = "No internet connection. Please try again.";
    private static final String MSG_NO_HANDLER_OPEN = "No suitable app found to open this link.";
    private static final String MSG_NO_HANDLER_SHARE = "No app available to share this content.";
    private static final String MSG_OPEN_FAILED = "Unable to open the link.";
    private static final String MSG_SHARE_FAILED = "Unable to share at the moment.";

    // -------- debounce --------
    private static final long MIN_INTERVAL_MS = 800; // chống bấm liên tục
    private static long lastOpenTs = 0L;
    private static long lastShareTs = 0L;

    private static boolean debounced(long lastTs) {
        long now = System.currentTimeMillis();
        return now - lastTs < MIN_INTERVAL_MS;
    }

    private static void toast(Context c, String msg) {
        // dùng app context để tránh leak
        Toast.makeText(c.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private static String firstNonEmpty(String a, String b) {
        if (a != null && !a.trim().isEmpty()) return a;
        return (b != null && !b.trim().isEmpty()) ? b : "";
    }

    /**
     * Open a photo in external browser.
     * Flow: pick url -> validate -> check network -> check handler -> ACTION_VIEW
     */
    public static void openPhoto(Context c, PhotoItem p, View anchor) {
        // debounce
        if (debounced(lastOpenTs)) return;
        lastOpenTs = System.currentTimeMillis();

        String url = firstNonEmpty(
                p != null ? p.getFullUrl() : null,
                p != null ? p.getThumbUrl() : null
        );

        // Validate URL
        if (!UrlUtils.isValidHttpUrl(url)) {
            toast(c, MSG_INVALID_URL);
            return;
        }

        // Check network
        if (!NetUtils.hasNetwork(c)) {
            toast(c, MSG_NO_NETWORK);
            return;
        }

        // Build & check handler
        Intent view = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        if (!IntentUtils.canHandle(c, view)) {
            toast(c, MSG_NO_HANDLER_OPEN);
            return;
        }

        try {
            c.startActivity(view);
        } catch (Exception e) {
            toast(c, MSG_OPEN_FAILED);
        }
    }

    /**
     * Share a photo link with other apps.
     * Flow: pick url -> validate -> check network -> check handler -> ACTION_SEND
     * (Có debounce để tránh spam.)
     */
    public static void sharePhoto(Context c, PhotoItem p, View anchor) {
        // debounce
        if (debounced(lastShareTs)) return;
        lastShareTs = System.currentTimeMillis();

        String url = firstNonEmpty(
                p != null ? p.getFullUrl() : null,
                p != null ? p.getThumbUrl() : null
        );

        // Validate URL
        if (!UrlUtils.isValidHttpUrl(url)) {
            toast(c, MSG_INVALID_URL);
            return;
        }

        // Theo TC01: share khi mất mạng phải báo lỗi mạng
        if (!NetUtils.hasNetwork(c)) {
            toast(c, MSG_NO_NETWORK);
            return;
        }

        // Build share intent
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        String title = (p != null && p.title != null && !p.title.isEmpty()) ? p.title : "Photo";
        send.putExtra(Intent.EXTRA_SUBJECT, title);
        send.putExtra(Intent.EXTRA_TEXT, title + " - " + url);

        // Check handler
        if (!IntentUtils.canHandle(c, send)) {
            toast(c, MSG_NO_HANDLER_SHARE);
            return;
        }

        try {
            c.startActivity(Intent.createChooser(send, "Share photo via"));
        } catch (Exception e) {
            toast(c, MSG_SHARE_FAILED);
        }
    }
}
