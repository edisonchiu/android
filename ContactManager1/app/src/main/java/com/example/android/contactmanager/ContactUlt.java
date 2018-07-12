package com.example.android.contactmanager;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Environment;
import android.provider.CallLog;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *      class ContactUlt
 *
 *      Created by SONY on 2015/4/3.
 *
 *      public List<ContactInfo> getAllContacts()                           Get All Contacts 获取所有联系人姓名及电话
 *      public List<CallLogInfo> getCallList()
 *      public static List<SmsInfo> getAllSms(Context context)      获取用户所有短信
 *      public static boolean checkSDcard()             检测SDcard是否正常
 *
 */
public class ContactUlt {

    Context m_context=null;

    // constructor CallItem()
    ContactUlt(Context context){
        this.m_context = context;
    }


    /*
     *******************************************************
     *                                                                                                          *
     *          Contacts Service                                                                      *
     *                                                                                                          *
     *******************************************************
     */
    /*
     *      Get All Contacts 获取所有联系人姓名及电话
     */
    //public List<CallItem> getAllContacts(Context context) {
    public List<ContactInfo> getAllContacts() {
        List<ContactInfo> resultList = new ArrayList<ContactInfo>();

        ContentResolver cr = m_context.getContentResolver();

        //----- All Numbers -----
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
                null, null, null);

        //----- Favorite Numbers -----
        //  int	STARRED	read/write	An indicator for favorite contacts: '1' if favorite, '0' otherwise.
        //  http://developer.android.com/reference/android/provider/ContactsContract.Contacts.html
        //        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI,
        //                null, "starred=?", new String[] {"1"}, null);

        while (cursor.moveToNext()) {
            // 得到名字
            String name = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            // 得到电话号码
            String contactId = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.Contacts._ID)); // 获取联系人的ID号，在SQLite中的数据库ID
            Cursor phone = cr.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = "
                            + contactId, null, null);
            while (phone.moveToNext()) {
                String strPhoneNumber = phone
                        .getString(phone
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)); // 手机号码字段联系人可能不止一个
                resultList.add(new ContactInfo(name, strPhoneNumber));
            }
        }

        return resultList;
    }


    /**
         * getCallList()
         * [整理] 操作通话记录（一） http://erwin-king.iteye.com/blog/1058000
         */
    public List<CallLogInfo> getCallList() {
        //public List<CallData> getCallList() {
        List<CallLogInfo> list = new ArrayList<CallLogInfo>();
        int type;
        Date date; //日期
        String time = "";//通话时间
        String telName = "";//姓名
        String telNo = "";//电话号码
        String callsID = "";
        ContentResolver cr = m_context.getContentResolver();

        // 取得通話紀錄
        final Cursor cursor = cr.query(CallLog.Calls.CONTENT_URI, new String[] {
                        CallLog.Calls.NUMBER,
                        CallLog.Calls.CACHED_NAME,
                        CallLog.Calls.TYPE,
                        CallLog.Calls.DATE,
                        CallLog.Calls.DURATION,
                        CallLog.Calls._ID
                }, null, null,
                CallLog.Calls.DEFAULT_SORT_ORDER);

        //        // 获取所有联系人姓名及电话
        //        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
        //                null, null, null);


        //listItems1= new String[cursor.getCount()];//test
        m_context.getResources().getString(R.string.unknown_telno);
        for (int i = 0; i < cursor.getCount(); i++) {
            CallLogInfo call = new CallLogInfo();
            cursor.moveToPosition(i);
            //listItems1[i] = cursor.getString(1);   //test

            telNo = cursor.getString(0);
            telName = cursor.getString(1);
            callsID = cursor.getString(5);

            // 不明來電 或 未登記通訊錄號碼
            if(telName==null) {
                if(telNo==null) {// 不明來電
                    telName =  m_context.getResources().getString(R.string.unknown_telno);
                } else {
                    // 未登記通訊錄號碼
                    telName =  cursor.getString(0);
                    telNo   =  "";
                }}
//            if(telName.length()<=0) {
//                //telName = telNo;
//                //telNo = "";
//            }
            //    telName = getString(R.string.unknown_telno);
            type = cursor.getInt(2);
            SimpleDateFormat sfd = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat sfd2 = new SimpleDateFormat("yyyy-MM-dd");
            date = new Date(Long.parseLong(cursor.getString(3)));
            time = sfd.format(date);

            call.setLongTime(formatDuring(Long.valueOf(cursor.getString(4))));
            String callDate = getDays(Long.parseLong(cursor.getString(3)));
//            String x = getContactIDFromPhoneNum(telNo);
//            byte[] kk = getPhoto(x);//联系人头像
//            if (kk != null) {
//                img = BitmapFactory.decodeByteArray(kk, 0, kk.length);
//                call.setImg(img);
//            }
//            Manager.addList(img, telName, telNo, callDate, time,
//                    sfd2.format(date), type);

            call.setTelName(telName);
            call.setNumber(telNo);
            call.setType(type);
            call.setDate(callDate);
            call.setTelTime(time);
            call.setCallsID(callsID);
            if (CallLog.Calls.INCOMING_TYPE == type) {//已接来电
                call.setType(0);
            } else if (CallLog.Calls.OUTGOING_TYPE == type) {//已拨
                call.setType(1);
            } else if (CallLog.Calls.MISSED_TYPE == type) {//未接来电
                call.setType(-1);
            }
            list.add(call);
        }
        return list;
    }


//    public List<CallItem> getCallList1() {
//        List<CallItem> list = new ArrayList<CallItem>();
//        CallItem call = new CallItem();
//        call.setTelName("setTelName");
//        call.setTelNo("setTelNo");
//        list.add(call);
//        return list;
//    }


//    /**
//         * Obtains the contact list for the currently selected account.
//         *
//         * @return A cursor for for accessing the contact list.
//         */
//    private Cursor getContacts()
//    {
//        // Run query
//        Uri uri = ContactsContract.Contacts.CONTENT_URI;
//        String[] projection = new String[] {
//                ContactsContract.Contacts._ID,
//                ContactsContract.Contacts.DISPLAY_NAME
//        };
//        //String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '" +
//        //        (mShowInvisible ? "0" : "1") + "'";
//        String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '" + "0" + "'";
//        String[] selectionArgs = null;
//        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
//
//        return managedQuery(uri, projection, selection, selectionArgs, sortOrder);
//    }


    /*
     * 检测SDcard是否正常
     */
    public static boolean checkSDcard() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    /*
     * 读取文件从SDcard
     */
    public static String readFileFromSDcard(String path, String filename)
            throws IOException {
        if (!checkSDcard()) {
            return null;
        }
        path = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/" + path;
        File dir = new File(path);
        if (!dir.exists())
            dir.mkdirs();

        FileInputStream inStream = new FileInputStream(new File(dir, filename));
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        byte[] data = outStream.toByteArray();
        outStream.close();
        inStream.close();
        return new String(data);
    }

    /*
     * 写入文件到SDcard
     */
    public static boolean writeFileToSDcard(String path, String filename,
                                            String content, boolean append) throws IOException {
        byte[] buffer = content.getBytes();
        if (!checkSDcard()) {
            return false;
        }
        path = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/" + path;
        File dir = new File(path);
        if (!dir.exists())
            dir.mkdirs();
        FileOutputStream out = new FileOutputStream(new File(dir, filename),
                append);
        out.write(buffer);
        out.flush();
        out.close();
        return true;
    }


    /**
         *      拨打电话
         */
    public static void callPhone(String number, Context context) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + number));
        context.startActivity(intent);
    }


    /**
     *      getMissedCallNumber
     */
    static public int getMissedCallNumber(Context context) {
        int count= 0;
        Cursor cursor = context.getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                null,
                CallLog.Calls.TYPE +  " = ? AND " + CallLog.Calls.NEW + " = ?",
                new String[] { Integer.toString(CallLog.Calls.MISSED_TYPE), "1" },
                CallLog.Calls.DATE + " DESC ");

        //this is the number of missed calls
        //for your case you may need to track this number
        //so that you can figure out when it changes
        //Log.v(TAG, ".getCount() = " + cursor.getCount());
        count = cursor.getCount();
        cursor.close();
        return count;
    }

    /*
    *   deleteRecordByPhoneNumber()
    *   删除记录
    */
    public void deleteRecordByPhoneNumber(String phoneNumber){
        String strUriInbox = "content://call_log/calls";
        Uri uriCall;
        uriCall = Uri.parse(strUriInbox);
        Cursor c = m_context.getContentResolver().query(uriCall,
                new String[] { "_id", "number", "date" }, null, null, null);
        if (c != null && c.moveToFirst()) {
            String num = c.getString(1);
            String id = c.getString(0);
            if (num != null &&phoneNumber!=null&& num.equals(phoneNumber)) {
                m_context.getContentResolver().delete(uriCall, "_id=" + id, null);
            }
            c.close();   }
    }


    /**
        *   deleteCallLogByID()
        *   Delete one Call  record  删除一筆紀錄
        *   @return int : number of data deleted, error return 0
        */
    public int deleteCallLogByID(String strID){
        int ret;
        try {
            ret= m_context.getContentResolver().delete(
                CallLog.Calls.CONTENT_URI,
                Contacts.Phones._ID + "=" + strID, null);
            if(ret>0)
                return 1;
            else
                return 0;
        }
        catch(Exception e) {
            System.out.println(e.getStackTrace());
            return 0;
        }
    }


    /**
     *   deleteSmsByID()
     *   Delete one Call  record  删除一筆紀錄
     *   @return int : number of data deleted, error return 0
     */
    public int deleteSmsByID(Context context, String str_id) {
        int count=0;
        try {
            Uri uriSms = Uri.parse("content://sms/" + str_id);
            Cursor c = context.getContentResolver().query(
                    uriSms,
                    //null,
                    new String[]{"_id", "thread_id", "address", "person",
                            "date", "body"},
                    //"_id=" + str_id,
                    null,
                    null, null); //"read=0"
            count = c.getCount();
            if (c != null && c.moveToFirst()) {
                do {
                    long id = c.getLong(0);
                    String sid = c.getString(0);
                    long threadId = c.getLong(1);
                    String address = c.getString(2);
                    String body = c.getString(5);
                    String date = c.getString(3);
                    Log.e("log>>>",
                            "0>" + c.getString(0) + " 1>" + c.getString(1)
                                    + " 2>" + c.getString(2) + " 3>"
                                    + c.getString(3) + " 4>" + c.getString(4)
                                    + " 5>" + c.getString(5));
                    Log.e("log>>>", "date" + c.getString(0));

                    //if (message.equals(body) && address.equals(number))
                    if (c.getString(0).equals(str_id)) {
                        // mLogger.logInfo("Deleting SMS with id: " + threadId);
//                        count= context.getContentResolver().delete(
//                                Uri.parse("content://sms/" + id), "date=?",
//                                new String[]{c.getString(4)});

                    count= context.getContentResolver().delete(
                            Uri.parse("content://sms/" + c.getString(1)), null,null);

//                        count= context.getContentResolver().delete(
//                                Uri.parse("content://sms/" +  c.getLong(0)), null,null);

//                        count= context.getContentResolver().delete(
//                                Uri.parse("content://sms/"), "_id=?",
//                                new String[]{c.getString(0)});
                        Log.e("log>>>", "Delete success.........");
                    }
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.e("log>>>", e.toString());
        }
        return count;
    }
/*
    public int deleteRecordByID(String strID){
        //        String strUriInbox = "content://call_log/calls";
        //        Uri uriCall;
        //        uriCall = Uri.parse(strUriInbox);
        int ret;

        ret= m_context.getContentResolver().delete(
                CallLog.Calls.CONTENT_URI,
                Contacts.Phones._ID + "=" + strID, null);

        // query
        Cursor cur = m_context.getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                new String[] { "_id", "number",
                CallLog.Calls.CACHED_NAME }, null, null, null);

        // delete one
        while (cur.moveToNext()) {
            try {
                String id = cur.getString(0);
                if (id.equals(strID)) {
                    ret= m_context.getContentResolver().delete(
                            CallLog.Calls.CONTENT_URI,
                            Contacts.Phones._ID + "=" + strID, null);
                    Toast.makeText(m_context.getApplicationContext(),
                            "Delete Success", Toast.LENGTH_SHORT).show();
                    cur.close();
                    return 1;
                }
            }
            catch(Exception e) {
                System.out.println(e.getStackTrace());
                cur.close();
                return 0;
            }
        }
        cur.close();
        return 0;
    }
*/

    /*
        *   deleteAllRecords()
        *   Delete all Call records  删除全部紀錄
        */
    private void deleteAllRecords() {
        Uri uri = Contacts.People.CONTENT_URI;
        m_context.getContentResolver().delete(uri, null, null);
    }


    /*
        *   获取联系人头像
        */
    public byte[] getPhoto(String people_id) {
        Cursor cur1 = null;
        String photo_id = null;
        String[] projection1 = new String[] {
                ContactsContract.Contacts.PHOTO_ID
        };
        if (people_id != null && !"".equals(people_id)) {
            String selection1 = ContactsContract.Contacts._ID + " = "
                    + people_id;
            try {
                cur1 = m_context.getContentResolver().query(
                        ContactsContract.Contacts.CONTENT_URI, projection1,
                        selection1, null, null);
            } catch (Exception e) {
            }
            if (cur1.getCount() > 0) {
                cur1.moveToFirst();
                photo_id = cur1.getString(0);
            }
            String[] projection = new String[] {
                    ContactsContract.Data.DATA15
            };
            String selection = "ContactsContract.Data._ID = " + photo_id;
            Cursor cur = m_context.getContentResolver().query(
                    ContactsContract.Data.CONTENT_URI, projection, selection,
                    null, null);
            cur.moveToFirst();
            if (cur.getCount() < 0 || cur.getCount() == 0) {
                return null;
            }
            byte[] contactIcon = cur.getBlob(0);
            if (contactIcon == null) {
                return null;
            } else {
                return contactIcon;
            }
        } else {
            return null;
        }
    }

    /**
         *      formatDuring    处理时间
         */
    static public String formatDuring(long mss) {
        long hours = mss / (60 * 60);
        long minutes = (mss % (1000 * 60 * 60)) / 60;
        long seconds = (mss % (1000 * 60));
        return hours + ":" + minutes+ ":" + seconds;
    }

    /**
         *      getDays
         */
    static final int DAY = 60*24;
    static public String getDays(long callTime) {
        String value = "";
        long newTime = new Date().getTime();
        long duration = (newTime - callTime) / (1000 * 60);
        if (duration < 60) {
            value = duration + "分鐘前";
        } else if (duration >= 60 && duration < DAY) {
            value = (duration / 60) + "小時前";
        } else if (duration >= DAY && duration < DAY * 2) {
            value = "昨天";
        } else if (duration >= DAY * 2 && duration < DAY * 3) {
            value = "前天";
        } else if (duration >= DAY * 7) {
            SimpleDateFormat sdf = new SimpleDateFormat("M月dd日");
            //SimpleDateFormat sdf = new SimpleDateFormat(SimpleDateFormat.MEDIUM, Locale.getDefault());
            //DateFormat format = SimpleDateFormat.getTimeInstance(SimpleDateFormat.MEDIUM, Locale.getDefault());
            value = sdf.format(new Date(callTime));
        } else {
            value = (duration / DAY) + "天前";
        }
        return value;
    }


    /**
     *      insertContacts  插入联系人姓名及电话
     */
    public static void insertContacts(String name, String number,
                                      Context context) {
        if (name == null || number == null)
            return;
        ContentResolver cr = context.getContentResolver();
        // 首先向RawContacts.CONTENT_URI执行一个空值插入，目的是获取系统返回的rawContactId
        ContentValues values = new ContentValues();
        Uri rawContactUri = cr.insert(ContactsContract.RawContacts.CONTENT_URI, values);
        long rawContactId = ContentUris.parseId(rawContactUri);
        values.clear();
        // 往data表入姓名数据
        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name);
        cr.insert(ContactsContract.Data.CONTENT_URI, values);

        // 往data表入电话数据
        values.clear();
        values.put(ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, number);
        values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
        cr.insert(ContactsContract.Data.CONTENT_URI, values);

    }

    /*
     * 批量插入联系人姓名及电话
     */
    public static void insertContactsList(List<ContactInfo> listData,
                                          Context context) throws Exception {
        // 文档位置：reference\android\provider\ContactsContract.RawContacts.html
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ContentResolver cr = context.getContentResolver();

        // 插入第几个人的信息
        int rawContactInsertIndex = 0;

        for (int i = 0; i < listData.size(); i++) {
            ContactInfo info = listData.get(i);
            // 插入当前编号人的空行
            ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());
            // 插入当前编号人的姓名
            ops.add(ContentProviderOperation
                    .newInsert(
                            android.provider.ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,
                            rawContactInsertIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, info.getName())
                    .build());
            // 插入当前编号人的电话
            ops.add(ContentProviderOperation
                    .newInsert(
                            android.provider.ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,
                            rawContactInsertIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, info.getNumber())
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build());
            // rawContactInsertIndex++;
            cr.applyBatch(ContactsContract.AUTHORITY, ops);
            ops.clear();
        }
    }

    
    /*
     * 插入通话记录
     */
    public static boolean insertCallLog(List<CallLogInfo> listDate,Context context){

        for(int i=0;i<listDate.size();i++){
            CallLogInfo info = listDate.get(i);
            ContentValues values = new ContentValues();
            values.put(CallLog.Calls.NUMBER, info.getNumber());
            values.put(CallLog.Calls.DATE, info.getDate());
            values.put(CallLog.Calls.DURATION, info.getDuration());
            values.put(CallLog.Calls.TYPE,info.getType());
            values.put(CallLog.Calls.NEW, info.getRend());//0已看1未看
            context.getContentResolver().insert(CallLog.Calls.CONTENT_URI, values);
        }
        return true;
    }


    /*
     *******************************************************
     *                                                                                                          *
     *          SMS Service                                                                             *
     *                                                                                                          *
     *******************************************************
     */
    /**
    *       getAllSms   获取用户所有短信
    */
    public static List<SmsInfo> getAllSms(Context context) {
        List<SmsInfo> list = new ArrayList<SmsInfo>();
        final String SMS_URI_ALL = "content://sms/";
        try {
            ContentResolver cr = context.getContentResolver();
            String[] projection = new String[] {
                    "_id", "address", "person",
                    "body", "date", "type" };
            Uri uri = Uri.parse(SMS_URI_ALL);
            Cursor cur = cr.query(uri, projection, null, null, "date desc");

            while (cur.moveToNext()) {
                String id;
                String name;
                String phoneNumber;
                String smsbody;
                long date;
                int type;

                id = cur.getString(cur.getColumnIndex("_id"));
                name = cur.getString(cur.getColumnIndex("person"));
                phoneNumber = cur.getString(cur.getColumnIndex("address"));
                smsbody = cur.getString(cur.getColumnIndex("body"));
                date = Long.parseLong(cur.getString(cur
                        .getColumnIndex("date")));
                type = cur.getInt(cur.getColumnIndex("type"));

                // Uri personUri = Uri.withAppendedPath(
                // ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                // phoneNumber);
                // Cursor localCursor = cr.query(personUri, new String[] {
                // PhoneLookup.DISPLAY_NAME, PhoneLookup.PHOTO_ID,
                // PhoneLookup._ID }, null, null, null);
                //
                // if (localCursor.getCount() != 0) {
                // localCursor.moveToFirst();
                // name = localCursor.getString(localCursor
                // .getColumnIndex(PhoneLookup.DISPLAY_NAME));
                // }
                if (smsbody == null)
                    smsbody = "";
                list.add(new SmsInfo(id, name, phoneNumber, smsbody, date, type));
            }
        } catch (SQLiteException ex) {

        }

        return list;
    }

    /*
     * 批量插入短信
     */
    public static boolean insertSmsList(List<SmsInfo> listData, Context context) {
        Uri mSmsUri = Uri.parse("content://sms/inbox");
        for (int i = 0; i < listData.size(); i++) {
            SmsInfo info = listData.get(i);
            ContentValues values = new ContentValues();
            values.put("address", info.getNumber());
            values.put("body", info.getContent());
            values.put("date", info.getDate());
            values.put("read", info.getRead());
            values.put("type", info.getType());
            //values.put("service_center", "+8613010776500");
            context.getContentResolver().insert(mSmsUri, values);
        }
        return true;
    }
    public static String getMD5(String instr) {
        String s = null;
        char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f' };
        try {
            java.security.MessageDigest md = java.security.MessageDigest
                    .getInstance("MD5");
            md.update(instr.getBytes());
            byte tmp[] = md.digest();
            char str[] = new char[16 * 2];
            int k = 0;
            for (int i = 0; i < 16; i++) {
                byte byte0 = tmp[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            s = new String(str).toUpperCase();
        } catch (Exception e) {
        }
        return s;
    }
}
