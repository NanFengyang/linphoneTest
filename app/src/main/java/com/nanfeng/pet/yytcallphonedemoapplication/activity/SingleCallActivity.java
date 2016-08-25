package com.nanfeng.pet.yytcallphonedemoapplication.activity;

import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.nanfeng.pet.yytcallphonedemoapplication.R;
import com.nanfeng.pet.yytcallphonedemoapplication.linphone.LinphoneService;
import com.nanfeng.pet.yytcallphonedemoapplication.linphone.PhoneBean;
import com.nanfeng.pet.yytcallphonedemoapplication.linphone.PhoneServiceCallBack;
import com.nanfeng.pet.yytcallphonedemoapplication.linphone.PhoneVoiceUtils;
import com.nanfeng.pet.yytcallphonedemoapplication.linphone.Utility;
import com.nanfeng.pet.yytcallphonedemoapplication.utils.ToastUtils;

public class SingleCallActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText username;
    private TextView mic, bange, speark;
    private Boolean isCall = true;//来源是打电话还是接电话，true为打电话，false为接电话
    private String userName = "莫少雪";
    private Boolean isCallConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_call);
        isCall = this.getIntent().getBooleanExtra("isCall", true);
        if (!isCall) {
            userName = this.getIntent().getStringExtra("userName");
        }
        initView();
    }

    private void initView() {
        username = (EditText) findViewById(R.id.username);
        mic = (TextView) findViewById(R.id.mic);
        bange = (TextView) findViewById(R.id.hangu);
        speark = (TextView) findViewById(R.id.speark);
        mic.setTag(false);
        speark.setTag(false);
        bange.setTag(false);
        mic.setOnClickListener(this);
        bange.setOnClickListener(this);
        speark.setOnClickListener(this);
        if (!isCall) {//如果是接电话，用户名不可编辑
            username.setEnabled(false);
            username.setText(userName);
            bange.setText("挂断");
            isCallConnected = true;
        } else {
            isCallConnected = false;
            username.setEnabled(true);
            bange.setText("拨打");
        }
        LinphoneService.addCallBack(new PhoneServiceCallBack() {
            @Override
            public void callConnected() {
                bange.setText("挂断");
                isCallConnected = true;
            }

            @Override
            public void callReleased() {
                ToastUtils.showShort("通话结束");
                finish();
            }
        });
    }

    /**
     * 图片设置
     *
     * @param tv
     * @param id
     */
    private void setTextViewPic(TextView tv, int id) {
        Drawable drawable = getResources().getDrawable(id);
/// 这一步必须要做,否则不会显示.
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        tv.setCompoundDrawables(null, drawable, null, null);
    }

    @Override
    public void onClick(View view) {
        boolean ispress = (boolean) view.getTag();
        switch (view.getId()) {
            case R.id.mic:

                if (ispress) {
                    setTextViewPic(mic, R.mipmap.icon_call_mute);
                    mic.setTag(false);
                } else {
                    setTextViewPic(mic, R.mipmap.icon_call_mute_press);
                    mic.setTag(true);
                }
                PhoneVoiceUtils.getInstance().toggleMicro((boolean) mic.getTag());
                break;
            case R.id.hangu:
                if (!isCallConnected) {
                    PhoneBean bean = new PhoneBean();
                    bean.userName = username.getText().toString();
                    bean.host = Utility.getHost();
                    PhoneVoiceUtils.getInstance().startSingleCallingTo(bean);
                    isCallConnected = true;
                    bange.setText("挂断");
                } else {
                    PhoneVoiceUtils.getInstance().hangUp();
                    finish();
                }
                break;
            case R.id.speark:
                if (ispress) {
                    setTextViewPic(speark, R.mipmap.icon_call_speak);
                    speark.setTag(false);
                } else {
                    setTextViewPic(speark, R.mipmap.icon_call_speak_press);
                    speark.setTag(true);
                }
                PhoneVoiceUtils.getInstance().toggleSpeaker((boolean) speark.getTag());
                break;
        }

    }
}
