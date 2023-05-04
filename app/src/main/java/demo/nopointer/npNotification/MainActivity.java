package demo.nopointer.npNotification;

import android.Manifest;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import npLog.nopointer.core.NpLog;
import npNotificationListener.nopointer.core.NpNotificationUtilHelper;
import npNotificationListener.nopointer.core.phone.NpContactsUtil;


public class MainActivity extends Activity {

    private TextView textBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textBtn = findViewById(R.id.textBtn);
        textBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NpNotificationUtilHelper.goToSettingNotificationAccess(MainActivity.this);
//                startActivity(new Intent(MainActivity.this,BleActivity.class));
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_SMS
            }, 100);
        }

        NpNotificationUtilHelper.getInstance().startListeningForNotification(this);

        NpNotificationUtilHelper.getInstance().registerPhoneOrSmsReceiver(this, true, true);
//

//        String name = NpContactsUtil.getContactName(this, "075583693832");
//        textBtn.setText(name);

    }


    @Override
    protected void onResume() {
        super.onResume();

//        NpLog.e("onResume     ，");
//        if (NpNotificationUtilHelper.isNotifyEnable(this)) {
//            NpNotificationUtilHelper.getInstance().startListeningForNotification(this);
//            textBtn.setText("已开启");
//        } else {
//            textBtn.setText("未开启");
//        }

    }

    //    adb shell dumpsys activity | grep -i run
//    plugin.voip.ui.VideoActivity
//    plugin.voip.ui.VideoActivity

}
