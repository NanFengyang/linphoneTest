package com.nanfeng.pet.yytcallphonedemoapplication.linphone;

import android.util.Log;

import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneAuthInfo;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCallParams;
import org.linphone.core.LinphoneCallParamsImpl;
import org.linphone.core.LinphoneConference;
import org.linphone.core.LinphoneConferenceParams;
import org.linphone.core.LinphoneConferenceParamsImpl;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneProxyConfig;

import java.util.List;

/**
 * Created by yangyoutao on 2016/8/18.
 */
public class PhoneVoiceUtils {
    private static PhoneVoiceUtils mPhoneVoiceUtils;
    private String TAG = "PhoneVoiceUtils";
    private LinphoneCore mLinphoneCore = null;

    /**
     * 私有构造方法
     */
    private PhoneVoiceUtils() {
        mLinphoneCore = LinphoneManager.getLc();
        mLinphoneCore.enableEchoCancellation(true);//启用或禁用语音的回声消除，这个是利用降噪的,测试无效
        mLinphoneCore.enableEchoLimiter(true);//回声啥的限制
    }

    /**
     * 返回单例模式
     *
     * @return
     */
    public static PhoneVoiceUtils getInstance() {
        if (null == mPhoneVoiceUtils) {
            mPhoneVoiceUtils = new PhoneVoiceUtils();

        }
        return mPhoneVoiceUtils;
    }

    /**
     * 设置音频端口
     *
     * @param port
     */
    public void setAudiPort(int port) {
        mLinphoneCore.setAudioPort(port);
    }


    /**
     * 点对点电话
     *
     * @param bean
     */
    public void startSingleCallingTo(PhoneBean bean) {
        LinphoneAddress lAddress = null;
        try {
            lAddress = mLinphoneCore.interpretUrl(bean.userName + "@" + bean.host);
        } catch (LinphoneCoreException e) {
            e.printStackTrace();
            Log.i("startSingleCallingTo", " LinphoneCoreException0:" + e.toString());
        }
        lAddress.setDisplayName(bean.displayName);
        LinphoneCallParams linphoneCallParams = mLinphoneCore.createCallParams(null);
        try {
            mLinphoneCore.inviteAddressWithParams(lAddress, linphoneCallParams);
        } catch (LinphoneCoreException e) {
            e.printStackTrace();
            Log.i("startSingleCallingTo", " LinphoneCoreException1:" + e.toString());
        }
    }

    /**
     * 开始一个电话会议
     */
    public void startConferenceTo(List<PhoneBean> list) {
        for (int i = 0; i < list.size(); i++) {
            startSingleCallingTo(list.get(i));
        }
        mLinphoneCore.addAllToConference();
    }

    /**
     * @param list
     */
    public void onCreart(List<PhoneBean> list) {
        LinphoneConferenceParamsImpl llParams = new LinphoneConferenceParamsImpl(mLinphoneCore);
        llParams.enableVideo(false);
        LinphoneConference confre = mLinphoneCore.createConference(llParams);
        Log.i("LinphoneManager", "创建会议:" + confre.toString() + "  ---getParticipants:" + confre.getParticipants().length);
        mLinphoneCore.enterConference();
        LinphoneAddress[] ii = confre.getParticipants();
        Log.i("LinphoneManager", "getConferenceSize:" + mLinphoneCore.getConferenceSize());
//        startConferenceTo(list);
//        LinphoneAddress[] ll = confre.getParticipants();
//        for (int i = 0; i < ll.length; i++) {
//            Log.i("LinphoneConference", "LinphoneAddress:" + ll[i].getUserName());
//        }
    }

    /**
     * 是否静音
     *
     * @param isMicMuted
     */
    public void toggleMicro(Boolean isMicMuted) {
        mLinphoneCore.muteMic(isMicMuted);
    }

    /**
     * 是否外放
     *
     * @param isSpeakerEnabled
     */
    public void toggleSpeaker(Boolean isSpeakerEnabled) {
        mLinphoneCore.enableSpeaker(isSpeakerEnabled);
    }

    /**
     * 挂断电话
     */
    public void hangUp() {
        LinphoneCall currentCall = mLinphoneCore.getCurrentCall();
        if (currentCall != null) {
            mLinphoneCore.terminateCall(currentCall);
        } else if (mLinphoneCore.isInConference()) {
            mLinphoneCore.terminateConference();
        } else {
            mLinphoneCore.terminateAllCalls();
        }
    }

    /**
     * 注册到服务器
     *
     * @param name
     * @param password
     * @param host
     */
    public void registerUserAuth(String name, String password, String host) throws LinphoneCoreException {
        Log.e(TAG, "registerUserAuth name = " + name);
        Log.e(TAG, "registerUserAuth pw = " + password);
        Log.e(TAG, "registerUserAuth host = " + host);
        String identity = "sip:" + name + "@" + host;
        String proxy = "sip:" + host;
        LinphoneAddress proxyAddr = LinphoneCoreFactory.instance().createLinphoneAddress(proxy);
        LinphoneAddress identityAddr = LinphoneCoreFactory.instance().createLinphoneAddress(identity);
        LinphoneAuthInfo authInfo = LinphoneCoreFactory.instance().createAuthInfo(name, null, password, null, null, host);
        LinphoneProxyConfig prxCfg = mLinphoneCore.createProxyConfig(identityAddr.asString(), proxyAddr.asStringUriOnly(), proxyAddr.asStringUriOnly(), true);
        prxCfg.enableAvpf(false);
        prxCfg.setAvpfRRInterval(0);
        prxCfg.enableQualityReporting(false);
        prxCfg.setQualityReportingCollector(null);
        prxCfg.setQualityReportingInterval(0);
        prxCfg.enableRegister(true);
        mLinphoneCore.addProxyConfig(prxCfg);
        mLinphoneCore.addAuthInfo(authInfo);
        mLinphoneCore.setDefaultProxyConfig(prxCfg);

    }

    /**
     * 资源销毁,目前不清楚使用模式
     */
    private void ondestroy() {
        try {
            mLinphoneCore.destroy();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            mLinphoneCore = null;
        }
    }
}
