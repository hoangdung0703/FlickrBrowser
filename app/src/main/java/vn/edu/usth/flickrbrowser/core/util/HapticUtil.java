package vn.edu.usth.flickrbrowser.core.util;

import android.view.HapticFeedbackConstants;
import android.view.View;

/**
 * Utility class for haptic feedback
 */
public class HapticUtil {
    
    /**
     * Light haptic feedback for standard interactions
     */
    public static void light(View view) {
        if (view != null) {
            view.performHapticFeedback(
                HapticFeedbackConstants.CLOCK_TICK,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            );
        }
    }
    
    /**
     * Medium haptic feedback for important actions
     */
    public static void medium(View view) {
        if (view != null) {
            view.performHapticFeedback(
                HapticFeedbackConstants.CONTEXT_CLICK,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            );
        }
    }
    
    /**
     * Strong haptic feedback for critical actions
     */
    public static void strong(View view) {
        if (view != null) {
            view.performHapticFeedback(
                HapticFeedbackConstants.LONG_PRESS,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            );
        }
    }
    
    /**
     * Success haptic - double tap pattern
     */
    public static void success(View view) {
        if (view != null) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        }
    }
    
    /**
     * Error/reject haptic
     */
    public static void error(View view) {
        if (view != null) {
            view.performHapticFeedback(HapticFeedbackConstants.REJECT);
        }
    }
}
