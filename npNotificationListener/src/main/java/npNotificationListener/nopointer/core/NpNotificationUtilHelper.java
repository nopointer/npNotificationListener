package npNotificationListener.nopointer.core;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import androidx.core.app.NotificationManagerCompat;

import java.util.List;
import java.util.Set;

import npNotificationListener.nopointer.core.callback.MsgCallback;
import npNotificationListener.nopointer.core.log.NpNotificationLog;
import npNotificationListener.nopointer.core.receiver.SmsReceiver;

/**
 * Created by nopointer on 2018/7/26.
 * 通知栏监听工具
 */

public final class NpNotificationUtilHelper {


    /**
     * 来电和短信广播接收器
     */
    private SmsReceiver smsReceiver = null;


    //接收一些常用的广播接收器，用来激活通知栏，不让他挂掉
    private ReStartNotificationReceiver reStartNotificationReceiver = null;

    private CustomPhoneStateListener customPhoneStateListener = null;

    private NpNotificationUtilHelper() {
        if (smsReceiver == null) {
            smsReceiver = new SmsReceiver();
        }
        if (reStartNotificationReceiver == null) {
            reStartNotificationReceiver = new ReStartNotificationReceiver();
        }
    }

    private static NpNotificationUtilHelper instance = new NpNotificationUtilHelper();

    public static NpNotificationUtilHelper getInstance() {
        return instance;
    }

    /**
     * 开始监听通知栏消息, 没有办法停止，唯一的停止就是去关闭通知栏的使用权限
     *
     * @param context
     */
    public void startListeningForNotification(final Context context) {
        if (!isServiceExisted(context, NpNotificationService.class)) {
            NpNotificationLog.log("通知栏监听服务，没有开启");
            startNotifyListenService(context);
        } else {
            NpNotificationLog.log("通知栏监听服务，已经开启");
        }

    }


    /**
     * 是否允许通知监听栏运行久一点
     *
     * @param context
     * @param enable
     */
    public void enableNpNotificationAlive(Context context, boolean enable) {
        if (context == null) return;
        //常用的系统广播
        if (enable) {
            if (reStartNotificationReceiver == null) {
                reStartNotificationReceiver = new ReStartNotificationReceiver();
                context.registerReceiver(reStartNotificationReceiver, ReStartNotificationReceiver.createIntentFilter());
            }
        } else {
            if (reStartNotificationReceiver == null) return;
            try {
                context.unregisterReceiver(reStartNotificationReceiver);
            } catch (IllegalArgumentException e) {
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 设置消息回调通知
     *
     * @param msgReceiveCakkback
     */
    public void setMsgReceiveCallback(MsgCallback msgReceiveCakkback) {
        MsgNotifyHelper.getMsgNotifyHelper().setMsgCallback(msgReceiveCakkback);
    }


    /**
     * 注册来电或者短信广播接收器
     *
     * @param context
     * @param enablePhone
     * @param enableSms
     */
    public void registerPhoneOrSmsReceiver(Context context, boolean enablePhone, boolean enableSms) {
        if (context == null) {
            NpNotificationLog.log("registerBroadcastReceiver failure! context=null!!!");
            return;
        }

        stopListenSMS(context);

        stopListenPhone(context);

        if (enablePhone) {
            startListenPhone(context);
//            filter.addAction(SmsReceiver.PHONE_STATE_ACTION);
        }

        if (enableSms) {
            startListenSMS(context);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///             静态方法            /////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * 前往设置通知栏权限
     *
     * @param context
     */
    public static void goToSettingNotificationAccess(Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 前往设置辅助功能
     *
     * @param context
     */
    public static void goToSettingAccessibility(Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


//    //开启监听通知栏的服务
//    private static void startNotifyListenService(Context context) {
//        NpNotificationLog.log("reStartNotifyListenService==>NPNotificationService");
//        ComponentName thisComponent = new ComponentName(context, NpNotificationService.class);
//        PackageManager pm = context.getPackageManager();
//        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
//        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
//    }

    /**
     * 开启监听通知栏的服务
     *
     * @param context
     */
    public static void startNotifyListenService(Context context) {
        NpNotificationLog.log("startNotifyListenService==>NPNotificationService， 开启通知栏监听服务");
        ComponentName thisComponent = new ComponentName(context, NpNotificationService.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }


    /**
     * 判断某个服务是否正在运行的方法
     *
     * @param context
     * @param clazz   是包名+服务的类名（例如：net.loonggg.testbackstage.TestService）
     * @return true代表正在运行，false代表服务没有正在运行
     */
    public static boolean isServiceExisted(Context context, Class clazz) {

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(Integer.MAX_VALUE);
        if (!(serviceList.size() > 0)) {
            return false;
        }

        String className = clazz.getName();
        String packageName = context.getPackageName();

        for (int i = 0; i < serviceList.size(); i++) {
            ActivityManager.RunningServiceInfo serviceInfo = serviceList.get(i);
            ComponentName serviceName = serviceInfo.service;
            if (serviceName.getPackageName().equalsIgnoreCase(packageName) && serviceName.getClassName().equals(className)) {
//                ycBleLog.e(" serviceInfo.getPackageName===>" + serviceName.getPackageName());
                return true;
            }
        }
        return false;
    }

    /**
     * 判断消息栏通知权限是否授权
     *
     * @param context
     * @return
     */
    public static boolean isNotifyEnable(Context context) {
        Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages(context);
        NpNotificationLog.log("获取了通知栏监听权限的应用包名:" + packageNames);
        return packageNames.contains(context.getPackageName());
    }


    /**
     * 开始监听来电
     */
    public void startListenPhone(Context context) {
        try {
            if (customPhoneStateListener == null) {
                customPhoneStateListener = new CustomPhoneStateListener(context);
            }
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                telephonyManager.listen(customPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止监听来电
     */
    public void stopListenPhone(Context context) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                if (customPhoneStateListener != null) {
                    telephonyManager.listen(customPhoneStateListener, PhoneStateListener.LISTEN_NONE);
                } else {
                    telephonyManager.listen(null, PhoneStateListener.LISTEN_NONE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始监听短信
     */
    public void startListenSMS(Context context) {
        try {
            if (smsReceiver == null) {
                smsReceiver = new SmsReceiver();
            }
            IntentFilter filter = new IntentFilter();
            filter.addAction(SmsReceiver.SMS_RECEIVE_ACTION);
            context.registerReceiver(smsReceiver, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止监听短信
     */
    public void stopListenSMS(Context context) {
        try {
            if (smsReceiver == null) {
                return;
            }
            context.unregisterReceiver(smsReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}