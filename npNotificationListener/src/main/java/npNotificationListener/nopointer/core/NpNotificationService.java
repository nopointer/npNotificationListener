package npNotificationListener.nopointer.core;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;

import npLog.nopointer.core.NpLog;


/**
 * Created by nopointer on 2018/7/26.
 * 通知栏消息获取
 */

public final class NpNotificationService extends NotificationListenerService {


    /**
     * 通知栏监听是否能收到通知的标志，有的手机收不到，开了辅助帮助后就可以收到，
     * 就怕能收到通知同时又开了辅助的情况，所以加了这个标志位，能收到通知 辅助里面就不处理了
     */
    public static boolean NPNotificationServiceCanReceive = false;

    /**
     * 最后一次数据，为了避免部分手机推送消息的时候，通知栏会收到2次回调
     */
    private static String lastMsgStr = null;

    @Override
    public void onCreate() {
        super.onCreate();
        NpLog.e("NPNotificationService===>onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NpLog.e("通知栏onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        NpLog.e("onListenerConnected====>通知栏服务正常，可以获取到通知信息");
    }


    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        NpLog.e("onListenerConnected====>通知栏服务不正常，不可以获取到通知信息");
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn, RankingMap rankingMap) {
        super.onNotificationRemoved(sbn, rankingMap);
        NpLog.e("onNotificationRemoved====>");
        if (!NpNotificationUtilHelper.isServiceExisted(this, NpNotificationService.class)) {
//            Intent intent = new Intent(this, NpNotificationService.class);
//            startService(intent);
            NpNotificationUtilHelper.getInstance().startListeningForNotification(getApplicationContext());
        }
    }

    //接收到通知消息的回调
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        NpLog.e("通知栏===>onNotificationPosted");
        NPNotificationServiceCanReceive = true;

        MsgNotifyHelper.getMsgNotifyHelper().onNotificationPost(sbn);
        if (sbn == null) return;
        //应用包名
        String pckName = sbn.getPackageName();
        if (TextUtils.isEmpty(pckName)) {
            return;
        }

        if (android.os.Build.VERSION.SDK_INT < 18) {
            NpLog.e("Android platform version is lower than 18.");
            return;
        }

        Notification notification = sbn.getNotification();

        Bundle extras = notification.extras;
        if (extras == null) return;

        //消息发送方,QQ 来电或者语音是没有发送方的，为空
        String from = "";

        String notificationTitle = extras.getString(Notification.EXTRA_TITLE);
        if (!TextUtils.isEmpty(notificationTitle)) {
            from = notificationTitle;
        }

        //消息内容
        String msgStr;
        try {
            msgStr = extras.getCharSequence(Notification.EXTRA_TEXT).toString();
        } catch (NullPointerException e) {
            msgStr = "";
        }

        NpLog.e("通知栏获取到消息==>{" + msgStr + "}===>pckName:" + pckName);

        handMsg(pckName, from, msgStr);
    }

    //通知消息被移除的回调
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
    }

//    @Override
//    public void onNotificationPosted(StatusBarNotification sbn, RankingMap rankingMap) {
//        super.onNotificationPosted(sbn, rankingMap);
//        NpLog.e("onNotificationPosted(StatusBarNotification sbn,RankingMap rankingMap)");
//    }

    //处理消息，判断消息类型和来源
    public void handMsg(String pkhName, String from, String msgContent) {
        String tmpStr = pkhName + " , from:" + from + " , msgContent:" + msgContent;

        if (pkhName.equalsIgnoreCase(NpMsgTypeConstants.pckg_wechat)) {
            if (!TextUtils.isEmpty(from) && !TextUtils.isEmpty(msgContent) && msgContent.length() > 4) {
                int startIndex = msgContent.indexOf(from);
                if (startIndex != -1) {
                    msgContent = msgContent.substring(startIndex + from.length());
                }
            }
        } else if (pkhName.equalsIgnoreCase(NpMsgTypeConstants.pckg_instagram)) {
            if (!TextUtils.isEmpty(from) && !TextUtils.isEmpty(msgContent) && msgContent.length() > from.length()) {
                int startIndex = msgContent.indexOf(from);
                if (startIndex != -1) {
                    msgContent = msgContent.substring(startIndex + from.length());
                }
            }
        }


        NpLog.e(tmpStr);
//        if (TextUtils.isEmpty(lastMsgStr) || !tmpStr.equals(lastMsgStr)) {
        MsgNotifyHelper.getMsgNotifyHelper().onAppMsgReceiver(pkhName, from, msgContent);
        lastMsgStr = tmpStr;
//        }
    }

    /**
     * 清空上一次的消息内容
     */
    @Deprecated
    public static void clearLastMessage() {
        if (lastMsgStr != null && !TextUtils.isEmpty(lastMsgStr)) {
            lastMsgStr = null;
        }
    }


}
