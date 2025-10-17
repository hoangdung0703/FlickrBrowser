package vn.edu.usth.flickrbrowser.core.util;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeUtil {
    
    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_DARK_MODE = "dark_mode";
    
    /**
     * Check if dark mode is enabled
     */
    public static boolean isDarkMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }
    
    /**
     * Set dark mode preference and apply immediately
     */
    public static void setDarkMode(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply();
        
        // Apply theme immediately
        applyTheme(enabled);
    }
    
    /**
     * Apply theme based on preference
     */
    public static void applyTheme(boolean darkMode) {
        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
    
    /**
     * Initialize theme on app startup
     */
    public static void initTheme(Context context) {
        boolean isDark = isDarkMode(context);
        applyTheme(isDark);
    }
}
