package com.nanfeng.pet.yytcallphonedemoapplication.activity;

import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.nanfeng.pet.yytcallphonedemoapplication.R;
import com.nanfeng.pet.yytcallphonedemoapplication.linphone.PhoneBean;
import com.nanfeng.pet.yytcallphonedemoapplication.linphone.PhoneVoiceUtils;
import com.nanfeng.pet.yytcallphonedemoapplication.linphone.Utility;
import com.nanfeng.pet.yytcallphonedemoapplication.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

public class MoreCallActivity extends AppCompatActivity {
    private ListView mListView;
    private List<PhoneBean> mList = new ArrayList<>();
    private MoreCallAdapter mMoreCallAdapter;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_call);
        mContext = this;
        init();
    }

    private void init() {
        mListView = (ListView) findViewById(R.id.listview);
        for (int i = 6000; i < 6005; i++) {
            PhoneBean bena = new PhoneBean();
            bena.userName = String.valueOf(i);
            bena.displayName = String.valueOf(i);
            bena.passWord = "000000";
            bena.host = Utility.getHost();
            Log.i("MoreCallActivity", " bena.userName:" + bena.userName);
            mList.add(bena);
        }
        mMoreCallAdapter = new MoreCallAdapter(mContext, mList);
        mListView.setAdapter(mMoreCallAdapter);
        startMoreCall();
    }

    private void onCreatConference() {

    }

    private void startMoreCall() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ToastUtils.showShort("开始拨打");
                PhoneVoiceUtils.getInstance().onCreart(mList);
            }
        }, 2000);

    }
}
