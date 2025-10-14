package vn.edu.usth.flickrbrowser.core.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.util.Log;

/**
 * Utility class for checking active network connection.
 * Used by ActionUtils (Share, Download, Open Info).
 *
 * Handles both modern (API 23+) and legacy devices gracefully.
 */
public final class NetUtils {
    private static final String TAG = "NetUtils";

    private NetUtils() {}

    /**
     * Kiểm tra có kết nối mạng hay không (Wi-Fi / 4G / Ethernet).
     *
     * @param ctx Context của Activity / Application.
     * @return true nếu có Internet khả dụng, false nếu không.
     */
    public static boolean hasNetwork(Context ctx) {
        if (ctx == null) return false;

        try {
            ConnectivityManager cm =
                    (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) {
                Log.w(TAG, "ConnectivityManager is null");
                return false;
            }

            // Android 6.0 (API 23) trở lên
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network active = cm.getActiveNetwork();
                if (active == null) {
                    Log.d(TAG, "No active network");
                    return false;
                }

                NetworkCapabilities caps = cm.getNetworkCapabilities(active);
                boolean connected = caps != null && (
                        caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));

                Log.d(TAG, "Network available (API 23+): " + connected);
                return connected;
            }

            // API thấp hơn (Deprecated nhưng vẫn cần cho emulator cũ)
            else {
                android.net.NetworkInfo ni = cm.getActiveNetworkInfo();
                boolean connected = ni != null && ni.isConnected();
                Log.d(TAG, "Network available (Legacy): " + connected);
                return connected;
            }

        } catch (SecurityException se) {
            // Một số ROM yêu cầu quyền ACCESS_NETWORK_STATE
            Log.e(TAG, "Missing ACCESS_NETWORK_STATE permission", se);
            return false;
        } catch (Throwable t) {
            Log.e(TAG, "Error checking network state", t);
            return false;
        }
    }
}
