package vn.edu.usth.flickrbrowser.core.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import java.util.List;

/**
 * Utility class for safely checking whether an Intent
 * can be handled by at least one installed app.
 *
 * Used in ActionUtils for Share / Open in Browser.
 */
public final class IntentUtils {
    private static final String TAG = "IntentUtils";

    private IntentUtils() {}

    /**
     * Kiểm tra xem có app nào xử lý được Intent này không.
     *
     * @param ctx    Context hiện tại
     * @param intent Intent cần kiểm tra
     * @return true nếu có app phù hợp, false nếu không
     */
    public static boolean canHandle(Context ctx, Intent intent) {
        if (ctx == null || intent == null) {
            Log.w(TAG, "Context or Intent is null");
            return false;
        }

        try {
            PackageManager pm = ctx.getPackageManager();
            if (pm == null) {
                Log.w(TAG, "PackageManager is null");
                return false;
            }

            // ✅ Cách chính xác nhất: queryIntentActivities
            List<ResolveInfo> handlers =
                    pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

            boolean hasHandler = handlers != null && !handlers.isEmpty();

            Log.d(TAG, "canHandle(" + intent.getAction() + ") → " + hasHandler);
            return hasHandler;

        } catch (SecurityException se) {
            // Một số ROM custom có thể chặn queryIntentActivities
            Log.e(TAG, "SecurityException while resolving intent", se);
            return false;
        } catch (Throwable t) {
            Log.e(TAG, "Error checking intent handler", t);
            return false;
        }
    }
}
