package com.nanfeng.pet.yytcallphonedemoapplication.linphone;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.WindowManager;

import com.nanfeng.pet.yytcallphonedemoapplication.activity.SingleCallActivity;
import com.nanfeng.pet.yytcallphonedemoapplication.utils.ToastUtils;

import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactoryImpl;
import org.linphone.core.LinphoneCoreListener;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.core.Reason;


/**
 * Created by 90Chris on 2015/7/1.
 */
public class LinphoneService extends Service implements LinphoneCoreListener.LinphoneCallStateListener,
        LinphoneCoreListener.LinphoneGlobalStateListener, LinphoneCoreListener.LinphoneRegistrationStateListener {

    final String TAG = getClass().getSimpleName();
    private PendingIntent mkeepAlivePendingIntent;
    private static LinphoneService instance;
    private static PhoneServiceCallBack mPhoneServiceCallBack;

    /**
     * 添加服务监听
     *
     * @param phoneServiceCallBack
     */
    public static void addCallBack(PhoneServiceCallBack phoneServiceCallBack) {
        mPhoneServiceCallBack = phoneServiceCallBack;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LinphoneCoreFactoryImpl.instance();
        LinphoneManager.createAndStart(LinphoneService.this);
        instance = this; // instance is ready once linphone manager has been created
        //make sure the application will at least wakes up every 10 mn
        Intent intent = new Intent(this, KeepAliveHandler.class);
        mkeepAlivePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        ((AlarmManager) this.getSystemService(Context.ALARM_SERVICE)).setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP
                , SystemClock.elapsedRealtime() + 600000
                , 600000
                , mkeepAlivePendingIntent);
    }

    public static boolean isReady() {
        return instance != null;
    }

    /**
     * @throws RuntimeException service not instantiated
     */
    public static LinphoneService instance() {
        if (isReady()) return instance;

        throw new RuntimeException("LinphoneService not instantiated yet");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LinphoneManager.destroy();
        ((AlarmManager) this.getSystemService(Context.ALARM_SERVICE)).cancel(mkeepAlivePendingIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void callState(LinphoneCore linphoneCore, LinphoneCall linphoneCall, LinphoneCall.State state, String s) {
        Log.e(TAG, "callState = " + state.toString());
        if (state == LinphoneCall.State.IncomingReceived) {//有电话打进来
            String str = "有电话打进来了：" + linphoneCall.getRemoteAddress().getPort() + "--" + linphoneCall.getRemoteAddress().getDisplayName();
            Log.e(TAG, "callState = " + str);
            ToastUtils.showLong(str);
            callIncome(linphoneCore, linphoneCall);
            if (null != mPhoneServiceCallBack) {
                mPhoneServiceCallBack.incomingCall(linphoneCall);
            }
        }
        if (state == LinphoneCall.State.Connected) {
            if (null != mPhoneServiceCallBack) {
                mPhoneServiceCallBack.callConnected();
            }
        }
        if (state == LinphoneCall.State.CallEnd) {
            if (null != mPhoneServiceCallBack) {
                mPhoneServiceCallBack.callReleased();
            }
        }
    }

    @Override
    public void globalState(LinphoneCore linphoneCore, LinphoneCore.GlobalState globalState, String s) {
        Log.e(TAG, "globalState = " + globalState.toString());
    }

    @Override
    public void registrationState(LinphoneCore linphoneCore, LinphoneProxyConfig linphoneProxyConfig, LinphoneCore.RegistrationState registrationState, String s) {
        Log.e(TAG, "registrationState = " + registrationState.toString());
        if (null != mPhoneServiceCallBack) {
            mPhoneServiceCallBack.registrationState(registrationState);
        }
    }

    /**
     * 电话接听对话框
     *
     * @param linphoneCore
     * @param linphoneCall
     */
    private void callIncome(final LinphoneCore linphoneCore, final LinphoneCall linphoneCall) {
        PhoneVoiceUtils.getInstance().toggleSpeaker(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("来电：" + linphoneCall.getRemoteAddress().getUserName() + " port:" + linphoneCall.getRemoteAddress().getPort());
        builder.setMessage("您的好友来电，是否接受通话")
                .setCancelable(false)
                .setPositiveButton("接受", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            PhoneVoiceUtils.getInstance().toggleSpeaker(false);
                            linphoneCore.acceptCall(linphoneCall);
                            Intent intent = new Intent(LinphoneService.this, SingleCallActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("isCall", false);
                            intent.putExtra("userName",linphoneCall.getRemoteAddress().getDisplayName());
                            startActivity(intent);
                        } catch (LinphoneCoreException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        linphoneCore.declineCall(linphoneCall, Reason.None);
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alert.show();
    }
}
