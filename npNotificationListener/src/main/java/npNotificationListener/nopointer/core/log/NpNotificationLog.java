package npNotificationListener.nopointer.core.log;

import android.text.TextUtils;
import android.util.Log;

public class NpNotificationLog {


    public static void log(String message) {
        if (mNpBleLogPrinter != null) {
            log(mNpBleLogPrinter.initTag(), message);
        } else {
            log("NpNotificationLog", message);
        }

    }

    public static void log(String tag, String message) {
        if (TextUtils.isEmpty(tag)) {
            tag = "NpNotificationLog";
        }
        if (mNpBleLogPrinter == null) {
            Log.e(tag, message);
        } else {
            mNpBleLogPrinter.onLogPrint(tag, message);
        }
    }

    private static NpBleLogPrinter mNpBleLogPrinter;

    public static void setNpBleLogPrinter(NpBleLogPrinter npBleLogPrinter) {
        mNpBleLogPrinter = npBleLogPrinter;
    }

    public static interface NpBleLogPrinter {
        void onLogPrint(String message);

        void onLogPrint(String tag, String message);

        String initTag();
    }


}
