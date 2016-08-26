package com.nanfeng.pet.yytcallphonedemoapplication.linphone;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;

import com.nanfeng.pet.yytcallphonedemoapplication.R;
import com.nanfeng.pet.yytcallphonedemoapplication.utils.ToastUtils;

import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCallStats;
import org.linphone.core.LinphoneChatMessage;
import org.linphone.core.LinphoneChatRoom;
import org.linphone.core.LinphoneContent;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneCoreListener;
import org.linphone.core.LinphoneEvent;
import org.linphone.core.LinphoneFriend;
import org.linphone.core.LinphoneFriendList;
import org.linphone.core.LinphoneInfoMessage;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.core.PublishState;
import org.linphone.core.Reason;
import org.linphone.core.SubscriptionState;
import org.linphone.core.ToneID;
import org.linphone.mediastream.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by 90Chris on 2015/7/1.
 */
public class LinphoneManager implements LinphoneCoreListener {
    final String TAG = getClass().getSimpleName();
    private static PhoneServiceCallBack mPhoneServiceCallBack;
    private static LinphoneManager instance;
    private LinphoneCore mLc;

    private String mLPConfigXsd = null;
    private String mLinphoneFactoryConfigFile = null;
    private String mLinphoneConfigFile = null;
    private String mLinphoneRootCaFile = null;
    private String mRingSoundFile = null;
    private String mRingbackSoundFile = null;
    private String mPauseSoundFile = null;
    private String mChatDatabaseFile = null;
    private String mErrorToneFile = null;
    private static boolean sExited;
    private Context mServiceContext;
    private Resources mR;
    private Timer mTimer;
    private BroadcastReceiver mKeepAliveReceiver = new KeepAliveReceiver();

    /**
     * 添加服务监听
     *
     * @param phoneServiceCallBack
     */
    public static void addCallBack(PhoneServiceCallBack phoneServiceCallBack) {
        mPhoneServiceCallBack = phoneServiceCallBack;
    }

    protected LinphoneManager(final Context c) {
        LinphoneCoreFactory.instance().setDebugMode(true, "MyCall");
        sExited = false;
        mServiceContext = c;

        String basePath = c.getFilesDir().getAbsolutePath();
        mLPConfigXsd = basePath + "/lpconfig.xsd";
        mLinphoneFactoryConfigFile = basePath + "/linphonerc";
        mLinphoneConfigFile = basePath + "/.linphonerc";
        mLinphoneRootCaFile = basePath + "/rootca.pem";
        mRingSoundFile = basePath + "/oldphone_mono.wav";
        mRingbackSoundFile = basePath + "/ringback.wav";
        mPauseSoundFile = basePath + "/toy_mono.wav";
        mChatDatabaseFile = basePath + "/linphone-history.db";
        mErrorToneFile = basePath + "/error.wav";

        mR = c.getResources();
    }

    public synchronized static final LinphoneManager createAndStart(Context c) {
        if (instance != null)
            throw new RuntimeException("Linphone Manager is already initialized");

        instance = new LinphoneManager(c);
        instance.startLibLinphone(c);
        /*TelephonyManager tm = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
        boolean gsmIdle = tm.getCallState() == TelephonyManager.CALL_STATE_IDLE;
        setGsmIdle(gsmIdle);*/

        return instance;
    }

    public static synchronized LinphoneCore getLcIfManagerNotDestroyedOrNull() {
        if (sExited || instance == null) {
            // Can occur if the UI thread play a posted event but in the meantime the LinphoneManager was destroyed
            // Ex: stop call and quickly terminate application.
            Log.w("Trying to get linphone core while LinphoneManager already destroyed or not created");
            return null;
        }
        return getLc();
    }

    public static final boolean isInstanciated() {
        return instance != null;
    }

    public static synchronized final LinphoneCore getLc() {
        return getInstance().mLc;
    }

    public static synchronized final LinphoneManager getInstance() {
        if (instance != null) return instance;

        if (sExited) {
            throw new RuntimeException("Linphone Manager was already destroyed. "
                    + "Better use getLcIfManagerNotDestroyed and check returned value");
        }

        throw new RuntimeException("Linphone Manager should be created before accessed");
    }

    private synchronized void startLibLinphone(Context c) {
        try {
            copyAssetsFromPackage();
            mLc = LinphoneCoreFactory.instance().createLinphoneCore(this, mLinphoneConfigFile, mLinphoneFactoryConfigFile, null, c);
            try {
                initLiblinphone();
            } catch (LinphoneCoreException e) {
                Log.e(e);
            }

            TimerTask lTask = new TimerTask() {
                @Override
                public void run() {
                    UIThreadDispatcher.dispatch(new Runnable() {
                        @Override
                        public void run() {
                            if (mLc != null) {
                                mLc.iterate();
                            }
                        }
                    });
                }
            };
            /*use schedule instead of scheduleAtFixedRate to avoid iterate from being call in burst after cpu wake up*/
            mTimer = new Timer("Linphone scheduler");
            mTimer.schedule(lTask, 0, 20);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Cannot start linphone");
        }
    }

    private synchronized void initLiblinphone() throws LinphoneCoreException {
        mLc.setContext(mServiceContext);
        try {
            String versionName = mServiceContext.getPackageManager().getPackageInfo(mServiceContext.getPackageName(), 0).versionName;
            if (versionName == null) {
                versionName = String.valueOf(mServiceContext.getPackageManager().getPackageInfo(mServiceContext.getPackageName(), 0).versionCode);
            }
            mLc.setUserAgent("LinphoneAndroid", versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(e, "cannot get version name");
        }
        mLc.setRemoteRingbackTone(mRingSoundFile);//设置一个用于回铃WAV文件的路径。
        mLc.setTone(ToneID.CallWaiting, mRingSoundFile);//设置呼叫等待的铃声
        mLc.setRing(mRingSoundFile);
        mLc.setRootCA(mLinphoneRootCaFile);
        mLc.setPlayFile(mPauseSoundFile);
        mLc.setChatDatabasePath(mChatDatabaseFile);
        mLc.setCallErrorTone(Reason.NotFound, mErrorToneFile);//设置呼叫错误播放的铃声

        int availableCores = Runtime.getRuntime().availableProcessors();
        Log.w(TAG, "MediaStreamer : " + availableCores + " cores detected and configured");
        mLc.setCpuCount(availableCores);

        int migrationResult = getLc().migrateToMultiTransport();
        Log.d(TAG, "Migration to multi transport result = " + migrationResult);

        mLc.setNetworkReachable(true);
        mLc.enableEchoCancellation(true);//启用或禁用语音的回声消除，这个是利用降噪的
        IntentFilter lFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        lFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mServiceContext.registerReceiver(mKeepAliveReceiver, lFilter);
    }

    private void copyAssetsFromPackage() throws IOException {
        copyIfNotExist(R.raw.oldphone_mono, mRingSoundFile);
        copyIfNotExist(R.raw.ringback, mRingbackSoundFile);
        copyIfNotExist(R.raw.toy_mono, mPauseSoundFile);
        copyIfNotExist(R.raw.incoming_chat, mErrorToneFile);
        copyIfNotExist(R.raw.linphonerc_default, mLinphoneConfigFile);
        copyFromPackage(R.raw.linphonerc_factory, new File(mLinphoneFactoryConfigFile).getName());
        copyIfNotExist(R.raw.lpconfig, mLPConfigXsd);
        copyIfNotExist(R.raw.rootca, mLinphoneRootCaFile);
    }

    public void copyIfNotExist(int ressourceId, String target) throws IOException {
        File lFileToCopy = new File(target);
        if (!lFileToCopy.exists()) {
            copyFromPackage(ressourceId, lFileToCopy.getName());
        }
    }

    public void copyFromPackage(int ressourceId, String target) throws IOException {
        FileOutputStream lOutputStream = mServiceContext.openFileOutput(target, 0);
        InputStream lInputStream = mR.openRawResource(ressourceId);
        int readByte;
        byte[] buff = new byte[8048];
        while ((readByte = lInputStream.read(buff)) != -1) {
            lOutputStream.write(buff, 0, readByte);
        }
        lOutputStream.flush();
        lOutputStream.close();
        lInputStream.close();
    }

    public static synchronized void destroy() {
        if (instance == null) return;
        //getInstance().changeStatusToOffline();
        sExited = true;
        instance.doDestroy();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void doDestroy() {
        try {
            mTimer.cancel();
            mLc.destroy();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            mLc = null;
            instance = null;
        }
    }

    @Override
    public void authInfoRequested(LinphoneCore linphoneCore, String s, String s1, String s2) {
        Log.e(TAG, "authInfoRequested");
    }

    @Override
    public void callStatsUpdated(LinphoneCore linphoneCore, LinphoneCall linphoneCall, LinphoneCallStats linphoneCallStats) {
        Log.e(TAG, "callStatsUpdated");
    }

    @Override
    public void newSubscriptionRequest(LinphoneCore linphoneCore, LinphoneFriend linphoneFriend, String s) {
        Log.e(TAG, "newSubscriptionRequest");
    }

    @Override
    public void notifyPresenceReceived(LinphoneCore linphoneCore, LinphoneFriend linphoneFriend) {
        Log.e(TAG, "notifyPresenceReceived");
    }

    @Override
    public void dtmfReceived(LinphoneCore linphoneCore, LinphoneCall linphoneCall, int i) {
        Log.e(TAG, "dtmfReceived");
    }

    @Override
    public void notifyReceived(LinphoneCore linphoneCore, LinphoneCall linphoneCall, LinphoneAddress linphoneAddress, byte[] bytes) {
        Log.e(TAG, "notifyReceived");
    }

    @Override
    public void transferState(LinphoneCore linphoneCore, LinphoneCall linphoneCall, LinphoneCall.State state) {
        Log.e(TAG, "transferState");
    }

    @Override
    public void infoReceived(LinphoneCore linphoneCore, LinphoneCall linphoneCall, LinphoneInfoMessage linphoneInfoMessage) {
        Log.e(TAG, "infoReceived");
    }

    @Override
    public void subscriptionStateChanged(LinphoneCore linphoneCore, LinphoneEvent linphoneEvent, SubscriptionState subscriptionState) {
        Log.e(TAG, "subscriptionStateChanged");
    }

    @Override
    public void publishStateChanged(LinphoneCore linphoneCore, LinphoneEvent linphoneEvent, PublishState publishState) {
        Log.e(TAG, "publishStateChanged");
    }

    @Override
    public void show(LinphoneCore linphoneCore) {
        Log.e(TAG, "show");
    }

    @Override
    public void displayStatus(LinphoneCore linphoneCore, String s) {
        Log.e(TAG, "displayStatus");
    }

    @Override
    public void displayMessage(LinphoneCore linphoneCore, String s) {
        Log.e(TAG, "displayMessage");
    }

    @Override
    public void displayWarning(LinphoneCore linphoneCore, String s) {
        Log.e(TAG, "displayWarning");
    }

    @Override
    public void fileTransferProgressIndication(LinphoneCore linphoneCore, LinphoneChatMessage linphoneChatMessage, LinphoneContent linphoneContent, int i) {
        Log.e(TAG, "fileTransferProgressIndication");
    }

    @Override
    public void fileTransferRecv(LinphoneCore linphoneCore, LinphoneChatMessage linphoneChatMessage, LinphoneContent linphoneContent, byte[] bytes, int i) {
        Log.e(TAG, "fileTransferRecv");
    }

    @Override
    public int fileTransferSend(LinphoneCore linphoneCore, LinphoneChatMessage linphoneChatMessage, LinphoneContent linphoneContent, ByteBuffer byteBuffer, int i) {
        Log.e(TAG, "fileTransferSend");
        return 0;
    }

    @Override
    public void callEncryptionChanged(LinphoneCore linphoneCore, LinphoneCall linphoneCall, boolean b, String s) {
        Log.e(TAG, "callEncryptionChanged");
    }

    @Override
    public void callState(LinphoneCore linphoneCore, LinphoneCall linphoneCall, LinphoneCall.State state, String s) {
        Log.i(TAG, "callState" + state.toString() + "--String:" + s);
        if (state == LinphoneCall.State.IncomingReceived) {//有电话打进来
            String str = "linphoneCore:" + linphoneCore.isInConference() + "--linphoneCall:" + linphoneCall.isInConference();
            android.util.Log.e(TAG, "callState = " + str);
            ToastUtils.showLong(str);
            if (linphoneCall.isInConference()) {
                android.util.Log.i(TAG, "是会议 ，加入会议 = ");
                linphoneCore.enterConference();
            } else {
                android.util.Log.i(TAG, "不是会议，连接通话 ");
                LinphoneService.instance().callIncome(linphoneCore, linphoneCall);
            }
            if (null != mPhoneServiceCallBack) {
                mPhoneServiceCallBack.incomingCall(linphoneCore, linphoneCall, state, s);
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
    public void isComposingReceived(LinphoneCore linphoneCore, LinphoneChatRoom linphoneChatRoom) {
        Log.e(TAG, "isComposingReceived");
    }

    @Override
    public void ecCalibrationStatus(LinphoneCore linphoneCore, LinphoneCore.EcCalibratorStatus ecCalibratorStatus, int i, Object o) {
        Log.e(TAG, "ecCalibrationStatus");
    }

    @Override
    public void globalState(LinphoneCore linphoneCore, LinphoneCore.GlobalState globalState, String s) {
        Log.e(TAG, "globalState");
    }

    @Override
    public void uploadProgressIndication(LinphoneCore linphoneCore, int i, int i1) {
        Log.e(TAG, "uploadProgressIndication");
    }

    @Override
    public void uploadStateChanged(LinphoneCore linphoneCore, LinphoneCore.LogCollectionUploadState logCollectionUploadState, String s) {
        Log.e(TAG, "uploadStateChanged");
    }

    @Override
    public void friendListCreated(LinphoneCore linphoneCore, LinphoneFriendList linphoneFriendList) {
        Log.e(TAG, "friendListCreated");
    }

    @Override
    public void friendListRemoved(LinphoneCore linphoneCore, LinphoneFriendList linphoneFriendList) {
        Log.e(TAG, "friendListRemoved");
    }

    @Override
    public void messageReceived(LinphoneCore linphoneCore, LinphoneChatRoom linphoneChatRoom, LinphoneChatMessage linphoneChatMessage) {
        Log.e(TAG, "messageReceived");
    }

    @Override
    public void notifyReceived(LinphoneCore linphoneCore, LinphoneEvent linphoneEvent, String s, LinphoneContent linphoneContent) {
        Log.e(TAG, "notifyReceived");
    }

    @Override
    public void registrationState(LinphoneCore linphoneCore, LinphoneProxyConfig linphoneProxyConfig, LinphoneCore.RegistrationState registrationState, String s) {
        Log.e(TAG, "registrationState");
        if (null != mPhoneServiceCallBack) {
            mPhoneServiceCallBack.registrationState(registrationState);
        }
    }

    @Override
    public void configuringStatus(LinphoneCore linphoneCore, LinphoneCore.RemoteProvisioningState remoteProvisioningState, String s) {
        Log.e(TAG, "configuringStatus");
    }
}
