package com.mixotc.goimandroid.contact;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.mixotc.goimandroid.R;
import com.mixotc.imsdklib.chat.GOIMContact;
import com.mixotc.imsdklib.utils.Logger;

import java.util.List;

/**
 * Author   : xiaoyu
 * Date     : 2018/5/24 下午2:12
 * Version  : v1.0.0
 * Describe :
 */
public class ContactActivity extends AppCompatActivity implements ContactContract.View {

    private static final String TAG = ContactActivity.class.getSimpleName();

    private ListView mListView;
    private ContactContract.Presenter mPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        mListView = findViewById(R.id.lv_contact);
        mPresenter = new ContactPresenter(this, new ContactModel());
        mPresenter.onCreate();
    }

    @Override
    public void showListData(List<GOIMContact> data) {
        ContactAdapter adapter = new ContactAdapter(data);
        mListView.setAdapter(adapter);
    }

    private class ContactAdapter extends BaseAdapter {

        private List<GOIMContact> mListData;

        ContactAdapter(List<GOIMContact> data) {
            mListData = data;
        }

        @Override
        public int getCount() {
            return mListData.size();
        }

        @Override
        public GOIMContact getItem(int position) {
            return mListData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, final ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(parent.getContext(), R.layout.item_contact, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final GOIMContact contact = getItem(position);
            String avatarUrl = contact.getAvatarUrl();
            Logger.d(TAG, contact.getNick() + ":" + avatarUrl);
            Glide.with(parent.getContext()).load(avatarUrl).asBitmap().into(holder.mIcon);
            holder.mName.setText(contact.getNick());
            holder.mBtnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(parent.getContext(), "点击删除：" + contact.mNick, Toast.LENGTH_SHORT).show();
                }
            });
            return convertView;
        }

        private class ViewHolder {

            ImageView mIcon;
            TextView mName;
            TextView mBtnDelete;

            ViewHolder(View convertView) {
                mIcon = convertView.findViewById(R.id.item_iv_contact_icon);
                mName = convertView.findViewById(R.id.item_tv_contact_name);
                mBtnDelete = convertView.findViewById(R.id.item_tv_contact_delete);
            }
        }
    }
}
