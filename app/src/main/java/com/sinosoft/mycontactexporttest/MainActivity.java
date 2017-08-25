package com.sinosoft.mycontactexporttest;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.IllegalFormatCodePointException;
import java.util.List;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.io.text.VCardReader;
import ezvcard.io.text.VCardWriter;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.Birthday;
import ezvcard.property.FormattedName;
//import ezvcard.property.StructuredName;
import ezvcard.property.Telephone;

public class MainActivity extends AppCompatActivity {
    private List<ContactEntity> data;
    private ListView listView;
    private EditText editText;
    private TextView test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        test = (TextView) findViewById(R.id.test_text);
        editText = (EditText) findViewById(R.id.edit);
        TextWatcher capitalizeTW = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    String inputString = "" + test.getText().toString();
                    String firstLetterCapString = WordUtil.capitalize(inputString);
                    if (!firstLetterCapString.equals("" + test.getText().toString())) {
                        test.setText("" + firstLetterCapString);
                        //test.setSelection(test.getText().length());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
        test.addTextChangedListener(capitalizeTW);
        /*String inputString = "" + test.getText().toString();
        String firstLetterCapString = WordUtil.capitalize(inputString);
        if (!firstLetterCapString.equals("" + test.getText().toString())) {
            test.setText("" + firstLetterCapString);
        }*/


        listView = (ListView) findViewById(R.id.list);
        View head = getLayoutInflater().inflate(R.layout.headlayout, listView, false);
        //listView.addHeaderView(head);
        init();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ContactEntity entity = data.get(position);
                Intent intent = new Intent(MainActivity.this, ContactDetailActivity.class);
                intent.putExtra("contact_item", entity);
                startActivity(intent);
            }
        });
        findViewById(R.id.pick_contact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //已废弃
                //Uri uri = Uri.parse("content://contacts/people");//Contacts.People.CONTENT_URI
                Uri uri = ContactsContract.Contacts.CONTENT_URI;
                Intent intent = new Intent(Intent.ACTION_PICK, uri);
                startActivityForResult(intent, 0);
            }
        });

        findViewById(R.id.android_import).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse("file://" + "/sdcard/华盛通信管家/备份" + "/backup.vcf");
                intent.setDataAndType(uri, "text/x-vcard");
                //intent.setComponent(new ComponentName("com.android.contacts", "com.android.contacts.common.vcard.ImportVCardActivity"));
                //intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings"));
                intent.setComponent(null);
                startActivity(intent);*/

                Intent intent = new Intent(Intent.ACTION_VIEW);
                /*intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);*/
                Uri uri = Uri.fromFile(new File("/sdcard/华盛通信管家/备份" + "/backup.vcf"));
                intent.setDataAndType(uri, "text/x-vcard");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        //导出
        findViewById(R.id.export_contact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<VCard> list = new ArrayList<VCard>();
                VCard vcard = new VCard();
                /*StructuredName n = new StructuredName();
                n.setFamily("House");//姓
                n.setGiven("Gregory");//名
                //n.getPrefixes().add("Dr");
                //n.getSuffixes().add("MD");
                vcard.setStructuredName(n);*/

                //vcard.setFormattedName("Dr. Gregory House M.D.");
                vcard.setFormattedName("张三");
                vcard.setVersion(VCardVersion.V4_0);
                vcard.addTelephoneNumber("13240935981", TelephoneType.CELL);
                vcard.addTelephoneNumber("13240935982", TelephoneType.WORK);
                Log.e("vcard1", vcard.toString());
                list.add(vcard);

                VCard vcard2 = new VCard();
                vcard2.setVersion(VCardVersion.V4_0);
                vcard2.setFormattedName("李四");
                vcard2.addTelephoneNumber("13240935980", TelephoneType.CELL);
                list.add(vcard2);
                writeVcard2SD(list);
                //String text = Ezvcard.write(vcard).version(VCardVersion.V3_0).go();


            }
        });
        //导入
        findViewById(R.id.import_contact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readVcard(Environment.getExternalStorageDirectory().getAbsolutePath() + "/vcards.vcf");
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                if (data == null) {
                    return;
                }
                //处理返回的data,获取选择的联系人信息
                Uri uri = data.getData();
                String[] contacts = getPhoneContacts(uri);
                break;
        }
    }

    private String[] getPhoneContacts(Uri uri) {
        String[] contact = new String[2];
        //得到ContentResolver对象
        ContentResolver cr = getContentResolver();
        //取得电话本中开始一项的光标
        Cursor cursor = cr.query(uri, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            //取得联系人姓名
            int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            contact[0] = cursor.getString(nameFieldColumnIndex);
            //取得电话号码
            String ContactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            Cursor phone = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + ContactId, null, null);
            if (phone != null) {
                phone.moveToFirst();
                contact[1] = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            }
            phone.close();
            cursor.close();
        } else {
            return null;
        }
        return contact;
    }

    private void init() {
        data = new ArrayList<>();
        // select * from contacts
        Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            //注意 Contact表 -->RawContact表 -->Data表
            String id = cursor.getString(
                    cursor.getColumnIndex(ContactsContract.Contacts._ID));
            String name = cursor.getString(
                    cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            ContactEntity entity = new ContactEntity();
            entity.setName(name);
            int hasPhoneNum = Integer.parseInt(cursor.getString(
                    cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));


            ArrayList<TelephoneEntity> telephoneEntities = new ArrayList<>();
            //这个ID下是否有电话号码 1表示有  0表示没有
            if (hasPhoneNum > 0) {
                Cursor numCursor = getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id,
                        null, null);
                while (numCursor.moveToNext()) {
                    Log.e(name,numCursor.getCount()+"");
                    String number = numCursor.getString(
                            numCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    int type = numCursor.getInt(numCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                    TelephoneEntity telephoneEntity = new TelephoneEntity();
                    telephoneEntity.setNumber(number);
                    telephoneEntity.setPhoneType(type);
                    telephoneEntities.add(telephoneEntity);
                }
                numCursor.close();
            }
            entity.setTelephoneEntities(telephoneEntities);
            data.add(entity);
        }
        MyContactAdapter adapter = new MyContactAdapter();
        listView.setAdapter(adapter);
    }


    private class MyContactAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ContactEntity entity = data.get(position);
            View view;
            ViewHolder holder;
            if (convertView == null) {
                view = getLayoutInflater().inflate(R.layout.contact_item, null);
                holder = new ViewHolder();
                holder.name = (TextView) view.findViewById(R.id.name);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHolder) view.getTag();

            }
            holder.name.setText(entity.getName());
            return view;
        }

        class ViewHolder {
            TextView name;
        }
    }


    /**
     * 将电子名片写入SD卡
     *
     * @param vcards 电子名片
     */

    private void writeVcard2SD(List<VCard> vcards) {
        File file = new File(Environment.getExternalStorageDirectory(), "vcards.vcf");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        VCardWriter writer = null;
        try {
            writer = new VCardWriter(file, VCardVersion.V4_0);
            for (VCard vcard : vcards) {
                writer.write(vcard);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null)
                    writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 先读取vcard的内容
     * 添加联系人的第一种方法：
     * 首先向RawContacts.CONTENT_URI执行一个空值插入，目的是获取系统返回的rawContactId
     * 这时后面插入data表的依据，只有执行空值插入，才能使插入的联系人在通讯录里面可见
     */
    private void readVcard(String path) {
        File file = new File(path);
        VCardReader reader = null;
        try {
            reader = new VCardReader(file);
            VCard vcard;
            DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
            while ((vcard = reader.readNext()) != null) {
                FormattedName fn = vcard.getFormattedName();
                String name = (fn == null) ? null : fn.getValue();

                /*Birthday bday = vcard.getBirthday();
                Date date = (bday == null) ? null : bday.getDate();
                String birthday = (date == null) ? null : df.format(date);*/

                List<Telephone> telephones = vcard.getTelephoneNumbers();
                write2Contact(name, telephones);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void write2Contact(String name, List<Telephone> telephones) {
        ContentValues values = new ContentValues();
        //首先向RawContacts.CONTENT_URI执行一个空值插入，目的是获取系统返回的rawContactId
        Uri rawContactUri = getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, values);
        //获取id
        long rawContactId = ContentUris.parseId(rawContactUri);
        //往data表入姓名数据
        values.clear();
        values.put(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, rawContactId); //添加id
        values.put(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);//添加内容类型（MIMETYPE）
        values.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, name);//添加名字，添加到first name位置
        getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI, values);
        //往data表入电话数据
        String number;
        int phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;
        for (Telephone telephone : telephones) {
            TelephoneType type = telephone.getTypes().get(0);//一个Telephone可以有多个type
            if (type == TelephoneType.CELL) {//手机
                phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;
            } else if (type == TelephoneType.WORK) {//工作
                phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_WORK;
            } else if (type == TelephoneType.HOME) {
                phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_HOME;
            }
            number = telephone.getText();
            values.clear();
            values.put(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, rawContactId);
            values.put(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
            values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, number);
            values.put(ContactsContract.CommonDataKinds.Phone.TYPE, phoneType);
            getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI, values);
        }
    }
}
