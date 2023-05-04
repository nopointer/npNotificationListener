package npNotificationListener.nopointer.core.phone;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;

import androidx.core.content.ContextCompat;

import npNotificationListener.nopointer.core.log.NpNotificationLog;


/**
 * 通讯录工具
 */
public final class NpContactsUtil {

    private NpContactsUtil() {
    }

    private static final String NUMBER_RULE = "/^\\d+$/";

    /**
     * 查询联系人 姓名 ，如果没有存在手机里面，或者没有通讯录权限的话，直接返回手机号码
     *
     * @param number 手机号码 先把这个手机号码过滤一下，变成普通的手机号码，然后再变成不同厂商喜欢的号码格式
     */
    public static String queryContactName(Context context, String number) {
        String oldNumber = number;
        String afterHandNumber = number;
        String result = number;
        try {
            boolean hasPermission = hasPermissions(context, new String[]{Manifest.permission.READ_CONTACTS});
            if (!hasPermission) {
                NpNotificationLog.log("没有 Manifest.permission.READ_CONTACTS 权限！！！，返回原始号码");
                return number;
            }
            number = number.replace(" ", "").replace("-", "").replace("+", "");
            afterHandNumber = number;
//        if (number.startsWith("+86")) {
//            number = number.substring(3);
//        }
//        if (number.startsWith("86")) {
//            number = number.substring(2);
//        }
            result = number;
            NpNotificationLog.log("处理过后的号码格式是:" + result);

            String numberFormat0 = number;
            String numberFormat1 = "86" + number;
            String numberFormat2 = "+86" + number;
            String numberFormat3 = number;
            String numberFormat4 = number;
            String numberFormat5 = number;
            String numberFormat6 = number;
            String numberFormat7 = number;
            String numberFormat8 = number;
            String numberFormat9 = number;
            if (number.length() == 11) {
                numberFormat3 = number.substring(0, 3) + " " + number.substring(3, 7) + " " + number.substring(7);
                numberFormat4 = "86" + numberFormat3;
                numberFormat5 = "+86" + numberFormat3;
                numberFormat6 = "86 " + numberFormat3;
                numberFormat7 = "+86 " + numberFormat3;
                numberFormat8 = " 86 " + numberFormat3;
                numberFormat9 = " +86 " + numberFormat3;
            }

            Uri uri = Data.CONTENT_URI; // 联系人Uri；

            String selectionSql = new StringBuilder()
                    .append(Data.DATA1).append("=? or ")
                    .append(Data.DATA1).append("=? or ")
                    .append(Data.DATA1).append("=? or ")
                    .append(Data.DATA1).append("=? or ")
                    .append(Data.DATA1).append("=? or ")
                    .append(Data.DATA1).append("=? or ")
                    .append(Data.DATA1).append("=? or ")
                    .append(Data.DATA1).append("=? or ")
                    .append(Data.DATA1).append("=? or ")
                    .append(Data.DATA1).append("=? or ")
                    .append(Data.DATA4).append("=? or ")
                    .append(Data.DATA4).append("=? or ")
                    .append(Data.DATA4).append("=? or ")
                    .append(Data.DATA4).append("=? or ")
                    .append(Data.DATA4).append("=? or ")
                    .append(Data.DATA4).append("=? or ")
                    .append(Data.DATA4).append("=? or ")
                    .append(Data.DATA4).append("=? or ")
                    .append(Data.DATA4).append("=? or ")
                    .append(Data.DATA4).append("=?")
                    .toString();

            String[] selectionArgs = new String[]{
                    numberFormat0, numberFormat1, numberFormat2, numberFormat3, numberFormat4, numberFormat5, numberFormat6, numberFormat7, numberFormat8, numberFormat9,
                    numberFormat0, numberFormat1, numberFormat2, numberFormat3, numberFormat4, numberFormat5, numberFormat6, numberFormat7, numberFormat8, numberFormat9};

            Cursor cursor = context.getContentResolver().query(uri,
                    new String[]{Data.DISPLAY_NAME, Data.DISPLAY_NAME_ALTERNATIVE, Data.DATA1, Data.DATA4},
                    selectionSql, selectionArgs, Data.RAW_CONTACT_ID);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String display_name_alt = cursor.getString(cursor.getColumnIndex("display_name_alt"));
                    String display_name = cursor.getString(cursor.getColumnIndex("display_name"));
                    if (!TextUtils.isEmpty(display_name)) {
                        result = display_name;
                    } else {
                        if (!TextUtils.isEmpty(display_name_alt)) {
                            result = display_name_alt;
                        }
                    }
                    break;
                }
                cursor.close();
            }
            if (result.equalsIgnoreCase(number)) {
                result = getContactName(context, number);
            }
            NpNotificationLog.log("联系人姓名查询结果:" + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result.equals(afterHandNumber)) {
            return oldNumber;
        } else {
            return result;
        }
    }


    /**
     * 判断是否有该项或者多项权限
     *
     * @param context
     * @param perms
     * @return
     */
    public static boolean hasPermissions(Context context, String... perms) {
        // Always return true for SDK < M, let the system deal with the permissions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        for (String perm : perms) {
            boolean hasPerm = (ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED);
            if (!hasPerm) {
                return false;
            }
        }
        return true;
    }

    public static String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        if (TextUtils.isEmpty(contactName)) {
            contactName = phoneNumber;
        }

        return contactName;
    }
}
