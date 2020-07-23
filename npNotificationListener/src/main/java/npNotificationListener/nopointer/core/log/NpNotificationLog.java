package npNotificationListener.nopointer.core.log;

import android.text.TextUtils;
import android.util.Log;

public class NpNotificationLog {


    /**
     * 是否允许log
     */
    public static boolean enableLog = true;

    public static void log(String message) {
        if (!enableLog) return;
        if (mLogPrinter != null) {
            log(mLogPrinter.initTag(), message);
        } else {
            log("NpNotificationLog", message);
        }

    }

    public static void log(String tag, String message) {
        if (!enableLog) return;
        if (TextUtils.isEmpty(tag)) {
            tag = "NpNotificationLog";
        }
        if (mLogPrinter == null) {
            Log.e(tag, message);
        } else {
            mLogPrinter.onLogPrint(tag, message);
        }
    }

    private static NpNotificationmLogPrinter mLogPrinter;

    public static void setNpBleLogPrinter(NpNotificationmLogPrinter logPrinter) {
        mLogPrinter = logPrinter;
    }

    public static interface NpNotificationmLogPrinter {
        void onLogPrint(String message);

        void onLogPrint(String tag, String message);

        String initTag();
    }


}
