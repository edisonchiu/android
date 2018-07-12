/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.contactmanager;

//import android.support.v7.app.AppCompatActivity;
import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

enum ViewMode {
    CALL_LOG, CONTACT_BOOK, CONTACT_ALL, SMS
}
// AppCompatActivity
//public final class ContactManager extends Activity
public final class ContactManager extends Activity
{
    /*
            <uses-permission android:name="android.permission.GET_ACCOUNTS" />
            <uses-permission android:name="android.permission.READ_CONTACTS" />
            <uses-permission android:name="android.permission.WRITE_CONTACTS" />
            <uses-permission android:name="android.permission.CALL_PHONE" />
            <uses-permission android:name="android.permission.READ_SMS" />
            <uses-permission android:name="android.permission.SEND_SMS" />
            <uses-permission android:name="android.permission.WRITE_SMS" />
            <uses-permission android:name="android.permission.READ_PHONE_STATE" />
     */
    //---- multiple Permission in API level 23 -----------------------------------
    // android - Storage permission error in Marshmallow - Stack Overflow
    // http://stackoverflow.com/questions/33162152/storage-permission-error-in-marshmallow
    private static final int PERMISSION_REQUEST_CODE = 1;
    String[] permissions = new String[]{
            Manifest.permission.GET_ACCOUNTS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_STATE,
    };
    //---- multiple Permission in API level 23 -----------------------------------


    public static final String TAG = "ContactManager";
    private ListView mContactList;
    MyIntentService mServiceIntent;
    private UIHandler mUIHandler;

    private ViewMode nViewMode =
          ViewMode.CALL_LOG;
//            ViewMode.CONTACT_BOOK;
//              ViewMode.SMS;

    //private Button mAddAccountButton;
    //private boolean mShowInvisible;
    //private CheckBox mShowInvisibleControl;
    final Context m_context = this;
    //List<CallItem> myCallList;
    ContactUlt contactUlt = new ContactUlt(m_context);

    @Override
    protected void onResume() {
        super.onResume();

//        if(TimerService.getTimerState().equals(TimerService.State.Running)){
//            buttonToggle.setText("停止");
//            textTimer.setText(TimerService.getStringRemainSeconds());
//
//            mUIHandler = new UIHandler(this);
//            TimerService.registerHandler(mUIHandler);
//            TimerService.resetServiceThreadHandler();
//
//        } else {
//            mUIHandler = new UIHandler(MainActivity.this);
//            TimerService.registerHandler(mUIHandler);
//            textTimer.setText(TimerService.getStringRemainSeconds());
//        }

        final String myPackageName = getPackageName();
        if (!Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName)) {
            // App is not default.
            // Show the "not currently set as the default SMS app" interface
            //View viewGroup = findViewById(R.id.not_default_app);
            //viewGroup.setVisibility(View.VISIBLE);

            Intent intent =
                    new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                    myPackageName);
            startActivity(intent);

//            // Set up a button that allows the user to change the default SMS app
//            Button button = (Button) findViewById(R.id.change_default_app);
//            button.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View v) {
//                    Intent intent =
//                            new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
//                    intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
//                            myPackageName);
//                    startActivity(intent);
//                }
//            });

        } else {
            // App is the default.
            // Hide the "not currently set as the default SMS app" interface
            //View viewGroup = findViewById(R.id.not_default_app);
            //viewGroup.setVisibility(View.GONE);
        }
    }

    /**
         *      onCreate()
         *      Called when the activity is first created. Responsible for initializing the UI.
         */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.v(TAG, "Activity State: onCreate()");
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        this.setTitle("已接/未接來電");
        super.onCreate(savedInstanceState);

        // setTitle
//        ActionBar actionBar = this.getActionBar();
//        final TextView myTitleText = (TextView) findViewById(R.id.myTitle);
//        if ( myTitleText != null ) {
//            myTitleText.setText("NEW TITLE");
//        }

        // MissedCalls
//        ContactUlt.getMissedCallNumber(this);
//        MissedCallsContentObserver mcco = new MissedCallsContentObserver();
//        getApplicationContext().getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, true, mcco);

        /*
         * Creates a new Intent to start the RSSPullService
         * IntentService. Passes a URI in the
         * Intent's "data" field.
         */
//        //        mServiceIntent = new IntentService(MyIntentService.class);
//        //        startService(mServiceIntent)
//        //mServiceIntent.setData(Uri.parse(dataUrl));
//        Intent intent = new Intent();
//        intent.setClass(this, MyIntentService.class);
//        startService(intent);
//        //stopService(intent);
//        //bindService(intent);

        // Starts the IntentService
        //getActivity().startService(mServiceIntent);

        //setActionBar("已接/未接來電");
        setContentView(R.layout.contact_manager);

        //        // TitleText setting
        //        final boolean customTitleSupported =
        //                requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        //        if (customTitleSupported) {
        //            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
        //                    R.layout.titlebar);
        //        }
        //        final TextView myTitleText = (TextView) findViewById(R.id.myTitle);
        //        if (myTitleText != null) {
        //            myTitleText.setText("NEW TITLE");
        //            // user can also set color using "Color" and then
        //            // "Color value constant"
        //            // myTitleText.setBackgroundColor(Color.GREEN);
        //        }

        //----- Obtain handles to UI objects -----
        mContactList = (ListView) findViewById(R.id.contactList);
        //        mAddAccountButton = (Button) findViewById(R.id.addContactButton);
        //        mShowInvisibleControl = (CheckBox) findViewById(R.id.showInvisible);

        // Initialize class properties
        //        mShowInvisible = false;
        //        mShowInvisibleControl.setChecked(mShowInvisible);

        /*
                //----- Register handler for UI elements -----
                // OnClick
                mAddAccountButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Log.d(TAG, "mAddAccountButton clicked");
                        launchContactAdder();
                    }
                });

                // OnCheckedChange
                mShowInvisibleControl.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        Log.d(TAG, "mShowInvisibleControl changed: " + isChecked);
                        mShowInvisible = isChecked;
                        //populateContactList();

                    }
                });
        */

//        mTextView = (TextView) findViewById(R.id.contactEntryText);
//        mTextView.setTextColor(0xffFF0000);


        //----- Populate the contact list -----
        if(nViewMode == ViewMode.CALL_LOG)
            populateCallLogList();
        else if(nViewMode == ViewMode.CONTACT_ALL)
            populateContactList();
        else if(nViewMode == ViewMode.SMS)
            populateSmsList();
        //-----------------------------


//        //----- Set Font Size -----
//        mTextView = (TextView) findViewById(R.id.contactEntryText);
//        mTextView.setTextSize(getResources().getDimension(R.dimen.textSize));
//        //mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
//        //mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 50);

/*
        //---- Permission in API level 23 -----------------------------------
        if (Build.VERSION.SDK_INT >= 23) //   > Android 6.0
        {
            if (checkPermission())
            {
                // Code for above or equal 23 API Oriented Device
                // Your Permission granted already .Do next code
            } else {
                //requestPermission(); // Code for permission
            }
        }
        else
        {
            // Code for Below 23 API Oriented Device
            // Do next code
        }
        //---- Permission in API level 23 -----------------------------------
*/


    }

    /*
    //---- Permission in API level 23 -----------------------------------
    private boolean checkPermission() {

        //---- multiple Permission in API level 23 -----------------------------------
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 100);
            return false;
        }
        return true;
        //---- multiple Permission in API level 23 -----------------------------------

        //---- Single permissions ----
        //result = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        //if (result == PackageManager.PERMISSION_GRANTED) {
        //    return true;
        //} else {
        //    return false;
        //}

    }
    //---- Permission in API level 23 -----------------------------------
    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(ContactManager.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(ContactManager.this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(ContactManager.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }
*/


    /**
     *      ----- Menu ----------------------------------------------------------
     *      Jollen 的 Android 教學,#12: 如何建立選單 Menu -  June 3, 2009 5:21 PM
     *      http://www.jollen.org/blog/2009/06/jollen-android-programming-12.html
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
        switch (id){
            case R.id.action_settings: break;
//            case R.id.action_delete:
//                //show_customdialog(position);
//                break;
//            case R.id.action_quit:
//                // close current activity
//                ContactManager.this.finish();
//                break;
            default: return false;
        }
        return super.onOptionsItemSelected(item);
    }

//    /**
//     *      setActionBar()
//     */
//    public void setActionBar(String heading) {
//        // TODO Auto-generated method stub
//
//        ActionBar actionBar = this.getActionBar();
//        //actionBar.setHomeButtonEnabled(true);
//        actionBar.setDisplayHomeAsUpEnabled(false);
//        actionBar.setDisplayShowHomeEnabled(false);
//        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.titlebackgroundcolor)));
//        actionBar.setTitle(heading);
//        actionBar.show();
//
//    }


    /**
         *          CallListAdapter()
         *          ListAdapter
     *              R.layout.list_entry_contact
         *          http://stackoverflow.com/questions/8166497/custom-adapter-for-list-view
         */
    public class CallListAdapter extends ArrayAdapter<CallLogInfo> {
        public CallListAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }
        public CallListAdapter(Context context, int resource, List<CallLogInfo> items) {
            super(context, resource, items);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.list_entry_contact, null);
            }

            CallLogInfo p = getItem(position);
            if (p != null) {
                TextView tt = (TextView) v.findViewById(R.id.contactEntryText1);
                TextView tt1 = (TextView) v.findViewById(R.id.contactEntryText2);
                TextView tt3 = (TextView) v.findViewById(R.id.contactEntryText3);
                TextView textViewStatus = (TextView) v.findViewById(R.id.contactEntryStatus);

                if (tt != null) {
                    tt.setText(p.getName());
                }
                if (tt1 != null) {
                    //tt1.setText(p.getTelNo());
                    if (tt != null)
                    tt1.setText("手機 " + p.getNumber());
                }
                if (tt3 != null) {
                   tt3.setText(p.getdays());
                }

//                int themeResId = 0;
//                try {
//                    Class<?> clazz = ContextThemeWrapper.class;
//                    Method method = clazz.getMethod("getThemeResId");
//                    method.setAccessible(true);
//                    themeResId = (Integer) method.invoke(this);
//                } catch (NoSuchMethodException e) {
//                    Log.e(TAG, "Failed to get theme resource ID", e);
//                } catch (IllegalAccessException e) {
//                    Log.e(TAG, "Failed to get theme resource ID", e);
//                } catch (IllegalArgumentException e) {
//                    Log.e(TAG, "Failed to get theme resource ID", e);
//                } catch (InvocationTargetException e) {
//                    Log.e(TAG, "Failed to get theme resource ID", e);
//                }
//                // use themeResId ...


                // 來電狀態 已接/已撥/未接
                if (textViewStatus != null) {
                    if(p.getStatus() == 0) {
                        textViewStatus.setText("已接");
                        //textViewStatus.setTextColor(Color.parseColor("#0000A0"));
                        textViewStatus.setTextColor(
                                //android.R.color.holo_blue_light
                                m_context.getResources().getColor(android.R.color.holo_blue_dark));
                                //m_context.getResources().getColor(R.color.color_dark_blue));
                    }
                    else if(p.getStatus() == 1) {
                        textViewStatus.setText("已撥");
                        //textViewStatus.setTextColor(Color.parseColor("#00A000"));
                        //android.R.color.holo_green_light
                        textViewStatus.setTextColor(
                                m_context.getResources().getColor(android.R.color.holo_green_dark));
                    }
                    else if(p.getStatus() == -1) {
                        textViewStatus.setText("未接");
                        //textViewStatus.setTextColor(Color.RED);
                        //android.R.color.holo_red_light
                        textViewStatus.setTextColor(
                                m_context.getResources().getColor(android.R.color.holo_red_dark));
                    }
                }
            }
            return v;
        }
    }


    /**
         *          ContactListAdapter()
         *          ListAdapter
     *              R.layout.list_entry_contact
         *          http://stackoverflow.com/questions/8166497/custom-adapter-for-list-view
         */
    public class ContactListAdapter extends ArrayAdapter<ContactInfo> {
        public ContactListAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }
        public ContactListAdapter(Context context, int resource, List<ContactInfo> items) {
            super(context, resource, items);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.list_entry_contact, null);
            }

            ContactInfo p = getItem(position);
            if (p != null) {
                TextView tt = (TextView) v.findViewById(R.id.contactEntryText1);
                TextView tt1 = (TextView) v.findViewById(R.id.contactEntryText2);
        //                TextView tt3 = (TextView) v.findViewById(R.id.contactEntryText3);
        //                TextView textViewStatus = (TextView) v.findViewById(R.id.contactEntryStatus);

                if (tt != null) {
                    tt.setText(p.getName());
                }
                if (tt1 != null) {
                    //tt1.setText(p.getTelNo());
                    if (tt != null)
                        tt1.setText("手機 " + p.getNumber());
                }
    //                if (tt3 != null) {
    //                    tt3.setText(p.getdays());
    //                }

            }
            return v;
        }
    }


    /**
         *          SmsListAdapter
         *              ListAdapter
         *          use:
         *              ArrayAdapter
         *              SmsInfo
         *              R.layout.list_entry_contact
         */
    public class SmsListAdapter extends ArrayAdapter<SmsInfo> {
        public SmsListAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }
        public SmsListAdapter(Context context, int resource, List<SmsInfo> items) {
            super(context, resource, items);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.list_entry_sms, null);
            }

            SmsInfo p = getItem(position);
            if (p != null) {
                TextView textViewNumber = (TextView) v.findViewById(R.id.tvEntryTextNumber);
                TextView textViewContent = (TextView) v.findViewById(R.id.tvEntryTextContent);
                TextView textViewDate = (TextView) v.findViewById(R.id.contactEntryText3);
                TextView textViewStatus = (TextView) v.findViewById(R.id.contactEntryStatus);

                if (textViewNumber != null) {
                    textViewNumber.setText(p.getNumber());
                }
                if (textViewContent != null) {
                    textViewContent.setText(p.getContent());
                    //tt1.setText(p.getTelNo());
                    //if (textViewContent != null)
                    //     textViewContent.setText("手機 " + p.getNumber());
                }
                if (textViewDate != null) {
                    textViewDate.setText("Date");
                }
                if (textViewStatus != null) {
                    textViewStatus.setText("Status");
                }
            }
            return v;
        }
    }


//    /**
//         * Populate the contact list based on account currently selected in the account spinner.
//         */
//    private void populateContactList() {
//
//        // Build adapter with contact entries
//        Cursor cursor = getContacts();
//        String[] fields = new String[]{
//                ContactsContract.Data.DISPLAY_NAME
//        };
//
//        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.contact_entry, cursor,
//                fields, new int[] {R.id.contactEntryText});
//        mContactList.setAdapter(adapter);
//
//    }
//
//
//    private TextView mTextView;
//    private String[] listItems = {"鉛筆","原子筆","鋼筆","毛筆","彩色筆"};
//    private String[] listItems1;
//    private ArrayAdapter<String> listAdapter;
//    /**
//         * Populate the contact list based on account currently selected in the account spinner.
//         */
//    private void populateContactList1() {
//
////        String[] fields= new String[cursor.getCount()];
////
////        for (int i = 0; i < cursor.getCount(); i++) {
////            cursor.moveToPosition(i);
////            fields[i]= "123";//cursor.getString(1);
////        }
//        //List<String> fields = new ArrayList<String>();
////        String[] fields= new String[cursor.getCount()];
////        int i=0;
////        while (cursor.moveToNext()) {
////            // 得到名字
////            String name = cursor.getString(cursor
////                    .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
////            fields[i++]= name;
////        }
//
//
//        //--- contactListAdapter ---
//        // Android ListView Text Color
//        // http://stackoverflow.com/questions/4533440/android-listview-text-color
//        ArrayAdapter<String> contactListAdapter = new ArrayAdapter<String>(
//                this,android.R.layout.simple_list_item_1, listItems){
//            @Override
//            public View getView(int position, View convertView,
//                                ViewGroup parent) {
//                View view =super.getView(position, convertView, parent);
//
//                // 修改屬性 ID名稱是 R.layout.simple_list_item_1 裡的 android.R.id.text1
//                TextView mTextView=(TextView) view.findViewById(android.R.id.text1);
//                //mTextView.setTextColor(Color.BLUE);
//                mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 60);
//                return view;
//            }
//        };
//        //--- contactListAdapter ---
//        //mContactList.setAdapter(contactListAdapter);
//
//        //--- ArrayAdapter - simple_list_item_1 ---
//        listAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,listItems1);
//        mContactList.setAdapter(listAdapter);
//
//        //----- List - OnItemClick -----
//        mContactList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
//
//                // Dialogs:  http://developer.android.com/guide/topics/ui/dialogs.html
//                //DialogFragment newFragment = new ConfirmDialog();
//                //newFragment.show(getSupportFragmentManager(), "missiles");
//                //show_customdialog(position);
//
//                Toast.makeText(getApplicationContext(),
//                        "你選擇的是", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }


    /**
         *      populate ContactList()
         *          use:
         *              contactUlt.getAllContacts()
         *              ContactListAdapter()
         */
    public void populateContactList()
    {
        //----- customAdapter -----
        // get data from the table by the ListAdapter
        final List<ContactInfo> myContactList = contactUlt.getAllContacts();
        ContactListAdapter customAdapter = new ContactListAdapter(
                this, R.layout.list_entry_contact, myContactList);
        mContactList.setAdapter(customAdapter);

        //----- ListListener - OnItemLongClick -----
        mContactList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View v,
                                           int index, long arg3) {
                return false;
            }
        });
    }


    /**
         *      populateSmsList()
         *          use:
         *              contactUlt.getAllSms()
         *              SmsListAdapter
         */
    public void populateSmsList()
    {
        //----- customAdapter -----
        // get data from the table by the ListAdapter
        final List<SmsInfo> mySmsList = contactUlt.getAllSms(m_context);
        SmsListAdapter customAdapter = new SmsListAdapter(
                this, R.layout.list_entry_sms, mySmsList);
        mContactList.setAdapter(customAdapter);

        //----- ListListener - OnItemLongClick -----
        mContactList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View v,
                                           int index, long arg3) {
            // 取得 Call List Item, ID 內容
            String t = mySmsList.get(index).getID();
            show_customdialog(t,"","");
            return false;
            }
        });
    }


    /**
         *      populateCallLogList()
         *          use:
         *              CallListAdapter()
         */
    public void populateCallLogList() {
// ArrayAdapter
        //listAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,listItems);
        //mContactList.setAdapter(listAdapter);

        // SimpleAdapter
        //String[] ContentItem = new String[] { "LinkID","ThingTitle", "ThingContent","RTime" };
        //int[] TextViewID = new int[] { R.id.listTextView1,R.id.listTextView2,R.id.listTextView3,R.id.listTextView4};
        //String[] ContentItem = new String[] { "telName","telNo" };
        //int[] TextViewID = new int[] { R.id.contactEntryText1,R.id.contactEntryText2};
        //SimpleAdapter simpleAdapter = new SimpleAdapter(
        //    this,getCallList(),R.layout.contact_entry,ContentItem,TextViewID);


        //----- customAdapter -----
        // get data from the table by the ListAdapter
        final List<CallLogInfo> myCallList = contactUlt.getCallList();  //new ArrayList<CallItem>();
        //final List<ContactInfo> myCallList = contactUlt.getAllContacts();
        //myCallList = getCallList();
        CallListAdapter customAdapter = new CallListAdapter(
                this, R.layout.list_entry_contact, myCallList);
        mContactList.setAdapter(customAdapter);

        //final List<ContactInfo> myContactList = contactUlt.getAllContacts();

        //----- List - OnItemClick -----
        mContactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Dialogs:  http://developer.android.com/guide/topics/ui/dialogs.html
                //DialogFragment newFragment = new ConfirmDialog();
                //newFragment.show(getSupportFragmentManager(), "missiles");



                //                Toast.makeText(getApplicationContext(),
                //                        "你選擇的是" + t, Toast.LENGTH_SHORT).show();
                //                Toast.makeText(getApplicationContext(),
                //                        "你選擇的是", Toast.LENGTH_SHORT).show();
            }
        });

        // OnItemLongClick
        mContactList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View v,
                                           int index, long arg3) {
                //Toast.makeText(list.this,myList.getItemAtPosition(index).toString(), Toast.LENGTH_LONG).show();
                //                Toast.makeText(getApplicationContext(),
                //                        "LongClick " + index,
                //                        Toast.LENGTH_LONG).show();

                //----- Delete one Call  record  删除一筆紀錄 -----
                // 取得 Call List Item, ID 內容
                String t = myCallList.get(index).getCallsID();
                String strTelName = myCallList.get(index).getName();
                String strTelNo = myCallList.get(index).getNumber();
                // deleteRecordByID(t);
                show_customdialog(t,strTelName,strTelNo);

                return false;
            }
        });
    }


//    /**
//         *      launchContactAdder()
//         *      Launches the ContactAdder activity to add a new contact to the selected accont.
//         */
//    protected void launchContactAdder() {
//        Intent i = new Intent(this, ContactAdder.class);
//        startActivity(i);
//    }


    /**
         *      show custom dialog
         */
    private void show_customdialog(
            final String str_delete_id,
            final String str_tel_name,
            final String str_tel_no)
    {
        // Create custom dialog object
        final Dialog dialog = new Dialog(ContactManager.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog_confirm);
        // Set dialog title
        //dialog.setTitle("Custom Dialog");
        //dialog.setTitle("確認是否刪除？");
        //int textViewId = dialog.getContext().getResources().getIdentifier("android:id/alertTitle", null, null);
        //TextView tv = (TextView) dialog.findViewById(textViewId);
        //TextView msg = (TextView)dialog.findViewById(R.id.textView1);
        //tv.setTextColor(getResources().getColor(R.color.dialog_titletextcolor));

        // set values for custom dialog components - text, image and button
        TextView text = (TextView) dialog.findViewById(R.id.textDialog);
        //text.setText("Custom dialog Android example.");
        text.setText("確認刪除？");

        TextView textViewDial = (TextView) dialog.findViewById(R.id.textDialogDial);
        textViewDial.setText(str_tel_name);

        //--- ImageView ---
        //ImageView image = (ImageView) dialog.findViewById(R.id.imageDialog);
        //image.setImageResource(R.drawable.image0);

        dialog.show();


        //--- cancelButton ---
        Button cancelButton = (Button) dialog.findViewById(R.id.negativeButton);
        // if decline button is clicked, close the custom dialog
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //按下按鈕時顯示快顯
                //Toast.makeText(m_context, "您按下Cancel按鈕", Toast.LENGTH_SHORT).show();
                // Close dialog
                dialog.dismiss();
            }
        });


        //--- okButton ---
        Button okButton = (Button) dialog.findViewById(R.id.positiveButton);
        // if decline button is clicked, close the custom dialog
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(nViewMode == ViewMode.CALL_LOG) {
                    //----- Delete one Call  record  删除一筆紀錄 -----
                    contactUlt.deleteCallLogByID(str_delete_id);
                    populateContactList();
                    //按下按鈕時顯示快顯
                    Toast.makeText(m_context, "已刪除", Toast.LENGTH_SHORT).show();
                } else if(nViewMode == ViewMode.SMS) {
                    contactUlt.deleteSmsByID(m_context,str_delete_id);
                    Toast.makeText(m_context, "已刪除", Toast.LENGTH_SHORT).show();
                    populateSmsList();
                }
                // Close dialog
                dialog.dismiss();
            }
        });


        //--- dialButton ---
        Button dialButton = (Button) dialog.findViewById(R.id.buttonDialOk);
        // if decline button is clicked, close the custom dialog
        dialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //
                contactUlt.callPhone(str_tel_no, m_context);
                //按下按鈕時顯示快顯
                //Toast.makeText(m_context, "撥出", Toast.LENGTH_SHORT).show();
                // Close dialog
                dialog.dismiss();
            }
        });
    }


//    /**
//         *      show alert dialog
//         */
//    private void show_alertdialog()
//    {
//        //產生一個Builder物件
//        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
//                m_context);
//
//        // set title //設定Dialog的標題
//        alertDialogBuilder.setTitle("Your Title");
//
//        // set dialog message //設定Dialog的內容
//        alertDialogBuilder
//                .setMessage("Click yes to exit!")
//                .setCancelable(false)
//
//                        //設定Positive按鈕資料
//                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        // if this button is clicked, close
//                        // current activity
//                        //MainActivity.this.finish();
//                    }
//                })
//
//                        //設定Negative按鈕資料
//                .setNegativeButton("No", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        // if this button is clicked, just close
//                        // the dialog box and do nothing
//                        dialog.cancel();
//                    }
//                });
//
//        // create alert dialog //利用Builder物件建立AlertDialog
//        AlertDialog alertDialog = alertDialogBuilder.create();
//
//        // show it
//        alertDialog.show();
//    }

//    private Handler uiMsgHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            if (msg != null) {
//                // {Code to check for other UI messages here}
//                // Check for dialog box responses
//                if (msg.what == (clearDlgId + ConfirmDialog.dlgResultYes)) {
//                    doClearDlgYesClicked();
//                }
//                else if (msg.what == (recordDlgId + ConfirmDialog.dlgResultYes)) {
//                    doRecordDlgYesClicked();
//                }
//                else if (msg.what == (recordDlgId + ConfirmDialog.dlgResultNo)) {
//                    doRecordDlgNoClicked();
//                }
//            }
//        }
//    };
}
