package npNotificationListener.nopointer.core;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import npNotificationListener.nopointer.core.log.NpNotificationLog;
import npNotificationListener.nopointer.core.phone.NpContactsUtil;


/**
 * 来去电监听
 */
public class CustomPhoneStateListener extends PhoneStateListener {

    private Context mContext;

    public CustomPhoneStateListener(Context context) {
        mContext = context;
    }

    /**
     * 最后一次状态和来电号码，为了去除重复的数据
     */
    private String lastStateAndNumber = "";


    @Override
    public void onServiceStateChanged(ServiceState serviceState) {
        super.onServiceStateChanged(serviceState);
        NpNotificationLog.log("CustomPhoneStateListener onServiceStateChanged: " + serviceState);
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {

        NpNotificationLog.log("state:" + state + ";incomingNumber:" + incomingNumber);
        if (TextUtils.isEmpty(incomingNumber)) {
            NpNotificationLog.log("来电号码为空，没有读取通话记录权限，不往下执行");
            return;
        }

        String currentState = state + "/" + incomingNumber;
        if (!currentState.equalsIgnoreCase(lastStateAndNumber)) {
            lastStateAndNumber = currentState;
            String name = NpContactsUtil.queryContactName(mContext, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:      // 电话挂断S
                    NpNotificationLog.log("NPPhoneStateListener==>非通话状态" + name);
                    MsgNotifyHelper.getMsgNotifyHelper().onPhoneCallIng(incomingNumber, name, 2);
                    break;
                case TelephonyManager.CALL_STATE_RINGING:   // 电话响铃
                    NpNotificationLog.log("NPPhoneStateListener==>手机铃声响了，来电人:" + name);
                    MsgNotifyHelper.getMsgNotifyHelper().onPhoneCallIng(incomingNumber, name, 0);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:   // 来电接通 或者 去电，去电接通  但是没法区分
                    NpNotificationLog.log("NPPhoneStateListener==>电话被接通了,可能是打出去的，也可能是接听的" + incomingNumber);
                    MsgNotifyHelper.getMsgNotifyHelper().onPhoneCallIng(incomingNumber, name, 1);
                    break;
            }
        }
    }


}