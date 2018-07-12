package com.example.android.contactmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;


/**
 * Detect Missed Call In Android
 *  http://www.learn-android-easily.com/2013/06/detect-missed-call-in-android.html
 */


public class IncommingCallReceiver extends BroadcastReceiver {

    public static final String TAG = "ContactManager";
    static boolean bIsRinging = false;
    static boolean bIsReceived = false;
    static String callerPhoneNumber;
    //Context mContext;

    public IncommingCallReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        // TODO: This method is called when the BroadcastReceiver is receiving

        Log.v(TAG, "#IncommingCallReceiver : onReceive()");

        // Get the current Phone State
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

        if(state==null)
            return;

        // If phone state "Rininging"
        if(state.equals(TelephonyManager.EXTRA_STATE_RINGING))
        {
            Log.v(TAG, "#IncommingCallReceiver : EXTRA_STATE_RINGING");
            bIsRinging =true;

            // Get the Caller's Phone Number
            Bundle bundle = intent.getExtras();
            callerPhoneNumber= bundle.getString("incoming_number");
            Log.v(TAG, "#incoming_number:" + callerPhoneNumber);
        }



        // If incoming call is received
        if(state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK))
        {
            Log.v(TAG, "IncommingCallReceiver : EXTRA_STATE_OFFHOOK");
            bIsReceived = true;
        }


        // If phone is Idle
        if (state.equals(TelephonyManager.EXTRA_STATE_IDLE))
        {
            Log.v(TAG, "IncommingCallReceiver : EXTRA_STATE_IDLE");
            // If phone was ringing(ring=true) and not received(callReceived=false) , then it is a missed call
            if(bIsRinging == true&&bIsReceived == false)
            {
                Log.v(TAG, "IncommingCallReceiver : NEW MISSED CALL:" + callerPhoneNumber);
                //Toast.makeText(mContext, "It was A MISSED CALL from : " + callerPhoneNumber, Toast.LENGTH_LONG).show();
            }

            bIsRinging = false;
            bIsReceived = false;
        }

        // an Intent broadcast.
        //throw new UnsupportedOperationException("Not yet implemented");
    }
}
