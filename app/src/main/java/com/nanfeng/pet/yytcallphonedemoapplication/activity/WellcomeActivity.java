package com.nanfeng.pet.yytcallphonedemoapplication.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.nanfeng.pet.yytcallphonedemoapplication.MainActivity;
import com.nanfeng.pet.yytcallphonedemoapplication.R;
import com.nanfeng.pet.yytcallphonedemoapplication.linphone.LinphoneService;
import com.nanfeng.pet.yytcallphonedemoapplication.linphone.PhoneServiceCallBack;
import com.nanfeng.pet.yytcallphonedemoapplication.linphone.PhoneVoiceUtils;
import com.nanfeng.pet.yytcallphonedemoapplication.linphone.Utility;
import com.nanfeng.pet.yytcallphonedemoapplication.utils.ImageHelper;
import com.nanfeng.pet.yytcallphonedemoapplication.utils.ToastUtils;

import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;

import static android.content.Intent.ACTION_MAIN;

public class WellcomeActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText userNameInput, userPasswordInput, userHostInput, userAudioPort;
    private Button login;
    Handler mHandler = new Handler();
    private ServiceWaitThread mThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wellcome);
        ImageView gifImageVIew = (ImageView) findViewById(R.id.imageview);
        ImageHelper.loadGif(this, R.mipmap.screen, gifImageVIew);
        userNameInput = (EditText) findViewById(R.id.iput_username);
        userPasswordInput = (EditText) findViewById(R.id.iput_password);
        userHostInput = (EditText) findViewById(R.id.iput_hostserver);
        userAudioPort = (EditText) findViewById(R.id.iput_audioport);
        login = (Button) findViewById(R.id.login);
        login.setOnClickListener(this);

        init();

    }

    /**
     * 开始初始化
     */
    private void init() {
        if (LinphoneService.isReady()) {
            login.setVisibility(View.VISIBLE);
        } else {
            // start linphone as background
            login.setVisibility(View.GONE);
            startService(new Intent(ACTION_MAIN).setClass(this, LinphoneService.class));
            mThread = new ServiceWaitThread();
            mThread.start();
        }

    }

    /**
     * 初始化完成，跳转页面
     */
    protected void onServiceReady() {
        ToastUtils.showLong("注册成功，服务开始");
        startActivity(new Intent().setClass(WellcomeActivity.this, MainActivity.class));
        finish();

    }

    @Override
    public void onClick(View view) {
        login();
    }

    /**
     * 登陆
     */
    private void login() {
        LinphoneService.addCallBack(new PhoneServiceCallBack() {
            @Override
            public void registrationState(LinphoneCore.RegistrationState registrationState) {
                if ("RegistrationOk".equals(registrationState.toString())) {
                    onServiceReady();
                }
            }
        });
        if (userNameInput.getText().toString().length() > 0) {
            Utility.setUsername(userNameInput.getText().toString());
        }
        if (userPasswordInput.getText().toString().length() > 0) {
            Utility.setPassword( userPasswordInput.getText().toString());
        }
        if (userHostInput.getText().toString().length() > 0) {
            Utility.setHost(userHostInput.getText().toString());
        }
        if (userAudioPort.getText().toString().length() > 0) {
            int port = Integer.parseInt(userAudioPort.getText().toString());
            PhoneVoiceUtils.getInstance().setAudiPort(port);
        } else {
        }
        try {
            PhoneVoiceUtils.getInstance().registerUserAuth(Utility.getUsername(), Utility.getPassword(), Utility.getHost());
        } catch (LinphoneCoreException e) {
            e.printStackTrace();
            ToastUtils.showShort("LinphoneCoreException:" + e.toString());
        }
    }

    /**
     * 等待服务开启线程
     */
    private class ServiceWaitThread extends Thread {
        public void run() {
            while (!LinphoneService.isReady()) {
                try {
                    sleep(80);
                } catch (InterruptedException e) {
                    throw new RuntimeException("waiting thread sleep() has been interrupted");
                }
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    login.setVisibility(View.VISIBLE);
                }
            });
            mThread = null;
        }
    }
}
