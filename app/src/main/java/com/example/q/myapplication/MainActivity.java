package com.example.q.myapplication;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

  EditText nameText;
  EditText phoneText;
  EditText addressText;

  Button nextBtn;

  String selectedString;


  ArrayList<String> items;
  ArrayAdapter adapter;
  ArrayList<Map<String, String>> dataList;
  ListView listview;


  @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        dataList = new ArrayList<Map<String, String>>();

        items = new ArrayList<String>();
        listview = (ListView) findViewById(R.id.listview);
        adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_single_choice,items);
        listview.setAdapter(adapter);

        nameText = (EditText)findViewById(R.id.nameText);
        phoneText = (EditText)findViewById(R.id.phoneText);
        addressText = (EditText)findViewById(R.id.addressText);

        nextBtn = (Button)findViewById(R.id.nextBtn);

        nextBtn.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                onClickNextBtn();
            }
        });

        if(Permissioncheck() == true) {
            loadContacts();
        }

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if( -1 < position && position < adapter.getCount()) {
                    // SET NAME AND PHONE NUMBER!
                    nameText.setText(dataList.get(position).get("name"));
                    phoneText.setText(dataList.get(position).get("phone"));

                } else {
                    Log.i("Error", "ITEM DOES NOT EXIST!");
                }
            }
        });

//      imm.hideSoftInputFromWindow(nameText.getWindowToken(), 0);
//      imm.hideSoftInputFromWindow(phoneText.getWindowToken(), 0);
//      imm.hideSoftInputFromWindow(addressText.getWindowToken(), 0);
  }

   public void onClickNextBtn(){
      int count, idx;
      count = adapter.getCount();
      if(count > 0){
          idx = listview.getCheckedItemPosition();
          if(idx > -1 && idx < count){
              // selectedString = items.get(idx);
              Toast.makeText(MainActivity.this,nameText.getText() + " " + phoneText.getText() + " " + addressText.getText() ,Toast.LENGTH_SHORT).show();
          }
      }
   }
    @Override
    public int checkSelfPermission(String permission) {
        return super.checkSelfPermission(permission);
    }

    private boolean Permissioncheck(){
      if(checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED){
          return true;
      }
      else{
          ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_CONTACTS},1);
          if(checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED){
              return true;
          }
          else{
              return false;
          }
      }
    }

    private void loadContacts() {
        ListViewBtnItem item;
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        //check there exists contact
        //cur : includes every contact
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                //check whether there exists phone number
                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", new String[]{id}, null);
                    //if there are multiple contacts per id --> Make multiple list items using the name
                    while (pCur.moveToNext()) {
                        item = new ListViewBtnItem();
                        String phoneNo = addHyphenToPhone(pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));

                        HashMap tmpMap = new HashMap<String, String>();
                        tmpMap.put("name", name);
                        tmpMap.put("phone", phoneNo);
                        tmpMap.put("address", "");

                        dataList.add(tmpMap);

                        String listItem = name + ": " + phoneNo;

                        items.add(listItem);
                        adapter.notifyDataSetChanged();
                    }
                    pCur.close();
                }
            }
        }
    }

    public String addHyphenToPhone(String phoneNum) {
      return phoneNum.replaceFirst("(\\d{3})(\\d{4})(\\d+)", "($1) $2-$3");
    }
}

//    private void insertContacts(String name,String phone) {
//        ContentResolver cr = getContentResolver();
//
//        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,null, null, null, null);
//
//        if (cur.getCount() > 0) {
//            while (cur.moveToNext()) {
//                String existName = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
//                if (existName.contains(name)) {
//                    //Toast.makeText(NativeContentProvider.this,"The contact name: " + name + " already exists", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//            }
//        }
//        if (Permissioncheck() == true) {
//            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
//            int rawContactInsertIndex = ops.size();
//
//            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
//                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
//                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
//                    .build());
//
//            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
//                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
//                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
//                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
//                    .build());
//
//            try {
//                cr.applyBatch(ContactsContract.AUTHORITY, ops);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }


  /*  View.OnClickListener myClickListener = new View.OnClickListener(){
      @Override
        public void onClick(View v) {
          String startJson ="[";
          String endJson = "]";
          if(!sb.toString().equals(""))
          {
              sb.append(",");
          }
          String temp = "{\"name\""+":"+"\""+et.getText().toString()+"\""+","
                  +"\"phone number\""+":"+"\""+et2.getText().toString()+"\""+"}";
          sb.append(temp);
          //tv.setText(startJson+sb+endJson);
          String temp2 = et.getText().toString()+" : "+et2.getText().toString();
          items.add(temp2);
          adapter.notifyDataSetChanged();
          insertContacts(et.getText().toString(),et2.getText().toString());
      }
};*/