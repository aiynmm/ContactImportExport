package com.sinosoft.mycontactexporttest;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mars on 2017/7/6.
 */

public class ContactDetailActivity extends AppCompatActivity {
    private ArrayList<TelephoneEntity> list;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);
        TextView textView= (TextView) findViewById(R.id.contact_name);
        Intent intent = getIntent();
        ContactEntity entity = intent.getParcelableExtra("contact_item");
        textView.setText(entity.getName());
        list = entity.getTelephoneEntities();
        ListView listView = (ListView) findViewById(R.id.detail_list);
        DetailAdapter adapter = new DetailAdapter();
        listView.setAdapter(adapter);
    }


    private class DetailAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TelephoneEntity entity = list.get(position);
            View view;
            ViewHolder holder;
            if (convertView == null) {
                view = getLayoutInflater().inflate(R.layout.contact_detail_item, null);
                holder = new ViewHolder();
                holder.number = (TextView) view.findViewById(R.id.number);
                holder.phoneType = (TextView) view.findViewById(R.id.phone_type);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHolder) view.getTag();

            }
            holder.number.setText(entity.getNumber());
            int type = entity.getPhoneType();
            if (type == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) {
                holder.phoneType.setText("手机");
            } else if (type == ContactsContract.CommonDataKinds.Phone.TYPE_HOME) {
                holder.phoneType.setText("家");
            } else if (type == ContactsContract.CommonDataKinds.Phone.TYPE_WORK) {
                holder.phoneType.setText("公司");
            }
            return view;
        }

        class ViewHolder {
            TextView number;
            TextView phoneType;
        }
    }
}
