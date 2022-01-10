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
        StackTraceElement caller = getCallerStackTraceElement();
        message = "[" + getCallPathAndLineNumber(caller) + "]：" + message;
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


    /**
     * 获取调用路径和行号
     *
     * @return
     */
    private static String getCallPathAndLineNumber(StackTraceElement caller) {
        String result = "%s.%s(L:%d)";
        String callerClazzName = caller.getClassName();
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
        result = String.format(result, callerClazzName, caller.getMethodName(), caller.getLineNumber());
        return result;
    }


    public static StackTraceElement getCallerStackTraceElement() {
        return Thread.currentThread().getStackTrace()[5];
    }

}
