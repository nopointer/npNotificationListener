package npNotificationListener.nopointer.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import npNotificationListener.nopointer.core.log.NpNotificationLog;


/**
 * Created by nopointer on 2018/7/26.
 * 重新启动通知栏的辅助广播接收器
 */

public final class ReStartNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NpNotificationLog.log("ReStartNotificationReceiver==>" + intent.getAction());
        boolean result = NpNotificationUtilHelper.isServiceExisted(context, NpNotificationService.class);
        if (!result) {
            NpNotificationLog.log("通知没有打开,尝试去打开");
            NpNotificationUtilHelper.getInstance().startListeningForNotification(context);
        }
    }

    /**
     * 创建系统常用的广播接收器
     *
     * @return
     */
    public static IntentFilter createIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        //时间变化
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        //亮屏
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        //黑屏
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        return intentFilter;
    }

}
