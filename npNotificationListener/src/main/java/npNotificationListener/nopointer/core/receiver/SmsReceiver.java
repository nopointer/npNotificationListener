package npNotificationListener.nopointer.core.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import npNotificationListener.nopointer.core.MsgNotifyHelper;
import npNotificationListener.nopointer.core.log.NpNotificationLog;
import npNotificationListener.nopointer.core.phone.NpContactsUtil;

import static android.telephony.TelephonyManager.EXTRA_STATE_IDLE;
import static android.telephony.TelephonyManager.EXTRA_STATE_OFFHOOK;
import static android.telephony.TelephonyManager.EXTRA_STATE_RINGING;

/**
 * 来电或者短信广播接收器，用于监听来电或者短信
 * <p>
 * 对于国内的贱商，真的是fuck啊，普通短信可以收到没问题，问题是一个通知类的短信来了，就他娘的拦截了，真恶心
 * * 这个广播，部分手机还有可能会回调2次
 */
public class SmsReceiver extends BroadcastReceiver {

    /**
     * 最后一次状态和来电号码，为了去除重复的数据
     */
    private String lastStateAndNumber = "";


    private static String strLastContent = null;
    //短信没有内容的时候，应该就是没有权限了，这里提示没有相关的权限
    public static String messageWithNoPermissionText = "请授予app读取短信权限,否则无法显示短信内容";

    /**
     * 手机状态
     */
//    public static final String PHONE_STATE_ACTION = "android.intent.action.PHONE_STATE";

    /**
     * 去电
     */
    public static final String NEW_OUTGOING_CALL_ACTION = "android.intent.action.NEW_OUTGOING_CALL";
    /**
     * 短信广播
     */
    public static final String SMS_RECEIVE_ACTION = "android.provider.Telephony.SMS_RECEIVED";


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
//        if (action.equalsIgnoreCase(PHONE_STATE_ACTION)) {
//            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
//            String extraIncomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
//            onCallStateChanged(context, state, extraIncomingNumber);
//        } else if (action.equalsIgnoreCase(Intent.ACTION_NEW_OUTGOING_CALL)) {
//            NpNotificationLog.log("NPPhoneStateListener==>拨打电话出去");
//        } else
        if (action.equalsIgnoreCase(SMS_RECEIVE_ACTION)) {
            NpNotificationLog.log("接收到短信");
            //操蛋的方法 每个地方最好都判断一次吧 金立的烂手机就会空指针异常
            handWithSms(context, intent);
        }
    }


    public static IntentFilter createIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        intentFilter.addAction("android.intent.action.NEW_OUTGOING_CALL");
        return intentFilter;
    }


    //来电状态监听
    public void onCallStateChanged(Context context, String state, String incomingNumber) {

        NpNotificationLog.log("state:" + state + ";incomingNumber:" + incomingNumber);
        if (TextUtils.isEmpty(incomingNumber)) {
            NpNotificationLog.log("来电号码为空，没有读取通话记录权限，不往下执行");
            return;
        }

        String currentState = state + "/" + incomingNumber;
        if (!currentState.equalsIgnoreCase(lastStateAndNumber)) {
            lastStateAndNumber = currentState;
            String name = NpContactsUtil.queryContactName(context, incomingNumber);
            if (state.equalsIgnoreCase(EXTRA_STATE_RINGING)) {
                NpNotificationLog.log("NPPhoneStateListener==>手机铃声响了，来电人:" + name);
                MsgNotifyHelper.getMsgNotifyHelper().onPhoneCallIng(incomingNumber, name, 0);
            } else if (state.equalsIgnoreCase(EXTRA_STATE_IDLE)) {
                NpNotificationLog.log("NPPhoneStateListener==>非通话状态" + name);
                MsgNotifyHelper.getMsgNotifyHelper().onPhoneCallIng(incomingNumber, name, 2);
            } else if (state.equalsIgnoreCase(EXTRA_STATE_OFFHOOK)) {
                NpNotificationLog.log("NPPhoneStateListener==>电话被接通了,可能是打出去的，也可能是接听的" + incomingNumber);
                MsgNotifyHelper.getMsgNotifyHelper().onPhoneCallIng(incomingNumber, name, 1);
            }
        }
    }

    /**
     * 处理短信接收
     *
     * @param context
     * @param intent
     */
    private static void handWithSms(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        String number = null;//发送方号码
        StringBuilder messageContentBuilder = new StringBuilder();
        if (null != bundle) {
            Object[] smsObj = (Object[]) bundle.get("pdus");
            if (null != smsObj) {
                for (Object object : smsObj) {
                    SmsMessage msg = SmsMessage.createFromPdu((byte[]) object);
                    if (null != msg) {
                        number = msg.getOriginatingAddress();
                        String messageContent = msg.getDisplayMessageBody();
                        NpNotificationLog.log("短信 -> " + number + ":" + messageContent);
                        if (!TextUtils.isEmpty(messageContent)) {
                            messageContentBuilder.append(messageContent);
                        }
                    }
                }
                if (TextUtils.isEmpty(messageContentBuilder.toString())) {
                    messageContentBuilder.append(messageWithNoPermissionText);
                }

                MsgNotifyHelper.getMsgNotifyHelper().onMessageReceive(number, NpContactsUtil.queryContactName(context, number), messageContentBuilder.toString());
                strLastContent = messageContentBuilder.toString();
            }
        }
    }


}
