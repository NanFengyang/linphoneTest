package com.nanfeng.pet.yytcallphonedemoapplication.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.nanfeng.pet.yytcallphonedemoapplication.R;
import com.nanfeng.pet.yytcallphonedemoapplication.linphone.PhoneBean;

import java.util.List;

/**
 * Created by yangyoutao on 2016/8/25.
 */
public class MoreCallAdapter extends BaseAdapter {
    private Context mComtext;
    private List<PhoneBean> mList;
    private LayoutInflater mLayoutInflater;

    public MoreCallAdapter(Context comtext, List<PhoneBean> list) {
        mLayoutInflater = LayoutInflater.from(comtext);
        mComtext = comtext;
        mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public PhoneBean getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHodler mViewHodler = null;
        if (view == null) {
            mViewHodler = new ViewHodler();
            view = mLayoutInflater.inflate(R.layout.morecall_itemlayout, null);
            mViewHodler.userName_tv = (TextView) view.findViewById(R.id.username_tv);
            mViewHodler.click_btn = (Button) view.findViewById(R.id.click_btn);
            view.setTag(mViewHodler);
        } else {
            mViewHodler = (ViewHodler) view.getTag();
        }
        PhoneBean bean = mList.get(i);
        if (null != bean) {
            mViewHodler.userName_tv.setText("用户：" + bean.userName);
        }

        return view;
    }

    private class ViewHodler {
        TextView userName_tv;
        Button click_btn;
    }
}
