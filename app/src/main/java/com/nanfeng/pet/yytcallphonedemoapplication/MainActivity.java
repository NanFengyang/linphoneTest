package com.nanfeng.pet.yytcallphonedemoapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.nanfeng.pet.yytcallphonedemoapplication.activity.MoreCallActivity;
import com.nanfeng.pet.yytcallphonedemoapplication.activity.SingleCallActivity;
import com.nanfeng.pet.yytcallphonedemoapplication.linphone.Utility;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.signle_phone).setOnClickListener(this);
        findViewById(R.id.more_phone).setOnClickListener(this);
        TextView username = (TextView) findViewById(R.id.showusernaem);
        String showtext = "用户名：" + Utility.getUsername() + " 密码：" + Utility.getPassword() + " HOST:" + Utility.getPassword();
        username.setText(showtext);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.signle_phone://单个语音聊天
                Intent intent = new Intent(this, SingleCallActivity.class);
                intent.putExtra("isCall", true);
                startActivity(intent);
                break;
            case R.id.more_phone://多人语音
                Intent intent1 = new Intent(this, MoreCallActivity.class);
                startActivity(intent1);
                break;
        }

    }
}
