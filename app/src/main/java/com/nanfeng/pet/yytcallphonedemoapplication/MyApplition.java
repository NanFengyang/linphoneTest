package com.nanfeng.pet.yytcallphonedemoapplication;

import android.app.Application;
import android.content.Intent;

import com.nanfeng.pet.yytcallphonedemoapplication.linphone.LinphoneService;

import static android.content.Intent.ACTION_MAIN;

/**
 * Created by yangyoutao on 2016/8/18.
 */
public class MyApplition extends Application {

    private static MyApplition mMyApplition;

    @Override
    public void onCreate() {
        super.onCreate();
        mMyApplition = this;
        startService(new Intent(ACTION_MAIN).setClass(this, LinphoneService.class));
    }

    public static MyApplition getInstance() {
        return mMyApplition;
    }
}
