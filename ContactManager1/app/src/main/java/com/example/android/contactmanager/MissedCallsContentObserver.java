package com.example.android.contactmanager;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.provider.CallLog;

/**
 * Created by SONY on 2015/4/8.
 *
 * http://stackoverflow.com/questions/3665183/broadcast-receiver-for-missed-call-in-android
 */
public class MissedCallsContentObserver extends ContentObserver
{
    // variable to hold context
    private Context m_context;

    //save the context recievied via constructor in a local variable
    public MissedCallsContentObserver(Context context){
        super(null);
        this.m_context = context;
    }

    public MissedCallsContentObserver()
    {
        super(null);
    }

    @Override
    public void onChange(boolean selfChange)
    {
        Cursor cursor = m_context.getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                null,
                CallLog.Calls.TYPE +  " = ? AND " + CallLog.Calls.NEW + " = ?",
                new String[] { Integer.toString(CallLog.Calls.MISSED_TYPE), "1" },
                CallLog.Calls.DATE + " DESC ");

        //this is the number of missed calls
        //for your case you may need to track this number
        //so that you can figure out when it changes
        cursor.getCount();

        cursor.close();
    }
}