package com.example.android.contactmanager;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

/**
 * Created by SONY on 2015/4/3.
 */
/**
 * CallLogInfo
 *      type
 *          CallLog.Calls.INCOMING_TYPE == type: 0 ->已接来电
 *          CallLog.Calls.OUTGOING_TYPE == type: 1-> 已拨
 *          CallLog.Calls.MISSED_TYPE == type:-1 -> 未接来电
 */
public class CallLogInfo {

    //Map<String, String> callStatus = new HashMap<>();
    //callStatus.Put("","");

    //private Context m_context = null;
    private Bitmap img;
    private String callsID;
    private String telName;
    private String telNo;
    private String telDate;
    private String telTime;
    private String simpleTime;
    private int type;       //1来电 2已拨 3未接
    private int rend;       //0已看1未看
    private String longTime;


    // constructor
    CallLogInfo() { }
    CallLogInfo(String telName, String telNo) {
        this.telName = telName;
        this.telNo = telNo;
    }

//    CallItem(Context c){
//        if(m_context!= null)
//            m_context = c;
//    }
//    public CallLogModel(){}
//    public CallLogModel(String number, long date, long duration, int type,
//                        int rend) {
//        this.number = number;
//        this.date = date;
//        this.duration = duration;
//        this.type = type;
//        this.rend = rend;
//    }

    //get、set......
    public void setCallsID(String str) {
        this.callsID = str;
    }
    public String getCallsID() { return callsID; }

    public void setLongTime(String time) {
        this.longTime = time;
    }

    public String getDuration() { return this.telDate; }

    public String getdays() {
        //long newTime = new Date().getTime();
        // return getDays(telDate.getTime());
        return telDate;
    }

    public int getStatus() {
        return type;
    }

    public void setTelName(String telName) {
        this.telName = telName;
    }
    public String getName() {
//        if(telName.length()==0)
//            //return  m_context.getString(R.string.unknown_telno);
//            String s= new String() {"2222"};
//            return  "333333333333";//"不明電話號碼";
//        else
        return this.telName;
    }

    public void setNumber(String telNo) {
        this.telNo = telNo;
    }
    public String getNumber() { return this.telNo; }

    public void setRend(int rend) {
        this.rend = rend;
    }
    public int getRend() {
        return this.rend;
    }

    public void setType(int type) {
        this.type = type;
    }
    public int getType() {
        return this.type;
    }


    public void setDate(String callDate) {
        this.telDate = callDate;
    }
    public String getDate() {
        return this.telDate;
    }

    public void setTelTime(String callTime) {
        this.telTime = callTime;
    }
    public void setImg(Bitmap img) {
        this.img = img;
    }

    public byte[] getPhoto(int x) { //联系人头像
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        img.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }
    public String getContactIDFromPhoneNum(String telNo) {
        return "";
    }


}
