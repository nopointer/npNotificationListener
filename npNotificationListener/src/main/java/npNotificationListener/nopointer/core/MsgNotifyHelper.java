package npNotificationListener.nopointer.core;

import android.service.notification.StatusBarNotification;

import java.util.ArrayList;
import java.util.List;

import npLog.nopointer.core.NpLog;
import npNotificationListener.nopointer.core.callback.MsgCallback;

/**
 * Created by nopointer on 2018/7/26.
 * 消息推送的管理者，并不是具体实现，只是起桥接作用，方便管理
 */

public final class MsgNotifyHelper {

    /**
     * 通知栏来电提醒
     */
    private List<TmpCallOrSmsObj> tmpCallObjList = new ArrayList<>();

    /**
     * 通知栏短信提醒
     */
    private List<TmpCallOrSmsObj> tmpSmsObjList = new ArrayList<>();


    private MsgNotifyHelper() {
        initData();
    }

    private static MsgNotifyHelper msgNotifyHelper = new MsgNotifyHelper();

    public static MsgNotifyHelper getMsgNotifyHelper() {
        return msgNotifyHelper;
    }

    //设置消息回调
    private MsgCallback msgCallback;

    public void setMsgCallback(MsgCallback msgCallback) {
        this.msgCallback = msgCallback;
    }

    public void onAppMsgReceiver(String packName, String from, String msgContent) {
        //来电会在系统通知栏里面
        if (packName.equalsIgnoreCase("com.android.incallui")) {
            onNotificationInCall(from, msgContent);
            return;
        }

        //短信会在系统通知栏里面
        if (packName.equalsIgnoreCase("com.android.mms")) {
            onNotificationSMS(from, msgContent);
            return;
        }

        if (msgCallback != null) {
            msgCallback.onAppMsgReceive(packName, from, msgContent);
        }
    }

    public void onNotificationPost(StatusBarNotification sbn) {
        if (msgCallback != null) {
            msgCallback.onNotificationPost(sbn);
        }
    }

    public void onPhoneCallIng(String phoneNumber, String contactName, int userHandResult) {
        if (msgCallback != null) {
            msgCallback.onPhoneInComing(phoneNumber, contactName, userHandResult);
        }
    }

    public void onMessageReceive(String phoneNumber, String contactName, String messageContent) {
        if (msgCallback != null) {
            msgCallback.onMessageReceive(phoneNumber, contactName, messageContent);
        }
    }


    /**
     * 通知栏来电提醒
     */
    synchronized void onNotificationInCall(String from, String content) {
        NpLog.e("来电通知栏:" + from + ":" + content);
        from = filterPhoneNumber(from);
        initData();
        tmpCallObjList.add(new TmpCallOrSmsObj(from, content + from));
        if (tmpCallObjList.size() >= 2) {
            TmpCallOrSmsObj first = tmpCallObjList.get(0);
            TmpCallOrSmsObj second = tmpCallObjList.get(1);
            if (second.content.contains(first.phoneOrName)) {
                if (msgCallback != null) {
                    msgCallback.onNotificationInCall(from, from, 3);
                }
                tmpCallObjList.clear();
            }
        }
    }


    synchronized void onNotificationSMS(String from, String content) {
        NpLog.e("短信通知栏:" + from + ":" + content);
        from = filterPhoneNumber(from);
        initData();
        tmpSmsObjList.add(new TmpCallOrSmsObj(from, content));
        if (tmpSmsObjList.size() >= 2) {
            TmpCallOrSmsObj first = tmpSmsObjList.get(0);
            TmpCallOrSmsObj second = tmpSmsObjList.get(1);
            if (second.phoneOrName.contains(first.phoneOrName)) {
                if (msgCallback != null) {
                    msgCallback.onNotificationReceiveSms(first.phoneOrName, second.phoneOrName, second.content);
                }
                tmpSmsObjList.clear();
            }
        }
    }


    /**
     * 特殊过滤处理一下 来电号码
     *
     * @param phone
     * @return
     */
    public static String filterPhoneNumber(String phone) {
        phone = phone.replace(" ", "");
        if (phone.startsWith("+86")) {
            phone = phone.substring(3);
        }
        return phone;
    }


    /**
     * 临时的来电或者短信对象
     */
    public static class TmpCallOrSmsObj {
        private String phoneOrName;
        private String content;

        public TmpCallOrSmsObj(String phoneOrName, String content) {
            this.phoneOrName = phoneOrName;
            this.content = content;
        }
    }

    private void initData() {
        if (tmpCallObjList == null) {
            tmpCallObjList = new ArrayList<>();
        }
        if (tmpSmsObjList == null) {
            tmpSmsObjList = new ArrayList<>();
        }
    }


}