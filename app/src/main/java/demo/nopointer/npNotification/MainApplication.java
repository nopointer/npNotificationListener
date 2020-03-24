package demo.nopointer.npNotification;

import android.app.Application;


public class MainApplication extends Application {



    public static MainApplication mainApplication = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mainApplication = this;
//        PushAiderHelper.getAiderHelper().setMsgReceiveCallback(new MsgCallback() {
//            @Override
//            public void onAppMsgReceive(String packName, NpMsgType msgType, String from, String msgContent) {
//            }
//
//            @Override
//            public void onPhoneInComing(String phoneNumber, String contactName, int userHandResult) {
//
//            }
//
//            @Override
//            public void onMessageReceive(String phoneNumber, String contactName, String messageContent) {
//            }
//
//            @Override
//            public void onNotificationPost(StatusBarNotification sbn) {
//                super.onNotificationPost(sbn);
//
//            }
//        });
    }

    public static MainApplication getMainApplication() {
        return mainApplication;
    }





}
