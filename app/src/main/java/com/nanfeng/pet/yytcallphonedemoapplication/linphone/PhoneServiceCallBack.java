package com.nanfeng.pet.yytcallphonedemoapplication.linphone;

import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCore;

/**
 * Created by yangyoutao on 2016/8/18.
 */
public abstract class PhoneServiceCallBack {
    /**
     * 注册状态
     *
     * @param registrationState
     */
    public void registrationState(LinphoneCore.RegistrationState registrationState) {

    }

    /**
     * 注册状态
     *
     * @param linphoneCall
     */
    public void incomingCall(LinphoneCore linphoneCore, LinphoneCall linphoneCall, LinphoneCall.State state, String s) {

    }

    /**
     * 电话接通
     */
    public void callConnected() {

    }

    /**
     * 电话被挂断
     */
    public void callReleased() {

    }
}
