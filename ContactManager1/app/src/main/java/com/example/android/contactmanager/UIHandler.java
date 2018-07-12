package com.example.android.contactmanager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

/**
 * Created by SONY on 2015/4/9.
 */
public class UIHandler extends Handler {

    Context m_context;
    private MainActivity mActivity;
    public static final String MSG = "msg";
    public String finalReaminTime = "00:00:00" ;


    //    public UIHandler(MainActivity activity) {
    //        super();
    //        this.mActivity = activity;
    //    }
    public UIHandler(Context context) {
        super();
        this.m_context = context;
    }

    @Override
    public void handleMessage(Message msg) {
//        String text = msg.getData().getString(MSG);
//        this.mActivity.setRemainTimeText(text);
//
//        if (text.equals(finalReaminTime)){
//            this.mActivity.changeButtonState();
//            this.mActivity.makeFinishToast();
//        }
    }
}
