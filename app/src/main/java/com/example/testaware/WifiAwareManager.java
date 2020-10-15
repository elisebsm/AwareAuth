/*
package com.example.testaware;

import android.net.wifi.aware.WifiAwareNetworkSpecifier;

import android.annotation.IntDef;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.RequiresPermission;
import android.annotation.SdkConstant;
import android.annotation.SdkConstant.SdkConstantType;
import android.annotation.SystemService;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.wifi.util.HexEncoding;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.nio.BufferOverflowException;
import java.util.List;

@SystemService(Context.WIFI_AWARE_SERVICE)
public class WifiAwareManager {
    private static final String TAG = "WifiAwareManager";
    private static final boolean DBG = false;
    private static final boolean VDBG = false; // STOPSHIP if true
    */
/**
     * Broadcast intent action to indicate that the state of Wi-Fi Aware availability has changed.
     * Use the {@link #isAvailable()} to query the current status.
     * This broadcast is <b>not</b> sticky, use the {@link #isAvailable()} API after registering
     * the broadcast to check the current state of Wi-Fi Aware.
     * <p>Note: The broadcast is only delivered to registered receivers - no manifest registered
     * components will be launched.
     *//*

    @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
    public static final String ACTION_WIFI_AWARE_STATE_CHANGED =
            "android.net.wifi.aware.action.WIFI_AWARE_STATE_CHANGED";
    */
/** @hide *//*

    @IntDef({
            WIFI_AWARE_DATA_PATH_ROLE_INITIATOR, WIFI_AWARE_DATA_PATH_ROLE_RESPONDER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DataPathRole {
    }
    */
/**
     * Connection creation role is that of INITIATOR. Used to create a network specifier string
     * when requesting a Aware network.
     *
     * //@see WifiAwareSession#createNetworkSpecifierOpen(int, byte[])
     * //@see WifiAwareSession#createNetworkSpecifierPassphrase(int, byte[], String)
     *//*

    public static final int WIFI_AWARE_DATA_PATH_ROLE_INITIATOR = 0;
    */
/**
     * Connection creation role is that of RESPONDER. Used to create a network specifier string
     * when requesting a Aware network.
     *
     * //@see WifiAwareSession#createNetworkSpecifierOpen(int, byte[])
     * //@see WifiAwareSession#createNetworkSpecifierPassphrase(int, byte[], String)
     *//*

    public static final int WIFI_AWARE_DATA_PATH_ROLE_RESPONDER = 1;
    private final Context mContext;
    private final WifiAwareManager mService;
    private final Object mLock = new Object(); // lock access to the following vars
    */
/** //@hide *//*

    public WifiAwareManager( Context context,WifiAwareManager service) {
        mContext = context;
        mService = service;
    }
    */
/**
     * Returns the current status of Aware API: whether or not Aware is available. To track
     * changes in the state of Aware API register for the
     * {@link #ACTION_WIFI_AWARE_STATE_CHANGED} broadcast.
     *
     * @return A boolean indicating whether the app can use the Aware API at this time (true) or
     * not (false).
     *//*

    public boolean isAvailable() {
        try {
            mService.isAvailable();
        }
            return mService.isUsageEnabled();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
    */
/**
     * Returns the characteristics of the Wi-Fi Aware interface: a set of parameters which specify
     * limitations on configurations, e.g. the maximum service name length.
     *
     * @return An object specifying configuration limitations of Aware.
     *//*

    public Characteristics getCharacteristics() {
        try {
            return mService.getCharacteristics();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
    */
/**
     * Attach to the Wi-Fi Aware service - enabling the application to create discovery sessions or
     * create connections to peers. The device will attach to an existing cluster if it can find
     * one or create a new cluster (if it is the first to enable Aware in its vicinity). Results
     * (e.g. successful attach to a cluster) are provided to the {@code attachCallback} object.
     * An application <b>must</b> call {@link WifiAwareSession#close()} when done with the
     * Wi-Fi Aware object.
     * <p>
     * Note: a Aware cluster is a shared resource - if the device is already attached to a cluster
     * then this function will simply indicate success immediately using the same {@code
     * attachCallback}.
     *
     * @param attachCallback A callback for attach events, extended from
     * {@link AttachCallback}.
     * @param handler The Handler on whose thread to execute the callbacks of the {@code
     * attachCallback} object. If a null is provided then the application's main thread will be
     *                used.
     *//*

    public void attach(@NonNull AttachCallback attachCallback, @Nullable Handler handler) {
        attach(handler, null, attachCallback, null);
    }
    */
/**
     * Attach to the Wi-Fi Aware service - enabling the application to create discovery sessions or
     * create connections to peers. The device will attach to an existing cluster if it can find
     * one or create a new cluster (if it is the first to enable Aware in its vicinity). Results
     * (e.g. successful attach to a cluster) are provided to the {@code attachCallback} object.
     * An application <b>must</b> call {@link WifiAwareSession#close()} when done with the
     * Wi-Fi Aware object.
     * <p>
     * Note: a Aware cluster is a shared resource - if the device is already attached to a cluster
     * then this function will simply indicate success immediately using the same {@code
     * attachCallback}.
     * <p>
     * This version of the API attaches a listener to receive the MAC address of the Aware interface
     * on startup and whenever it is updated (it is randomized at regular intervals for privacy).
     * The application must have the {@link android.Manifest.permission#ACCESS_FINE_LOCATION}
     * permission to execute this attach request. Otherwise, use the
     * {@link #attach(AttachCallback, Handler)} version. Note that aside from permission
     * requirements this listener will wake up the host at regular intervals causing higher power
     * consumption, do not use it unless the information is necessary (e.g. for OOB discovery).
     *
     * @param attachCallback A callback for attach events, extended from
     * {@link AttachCallback}.
     * @param identityChangedListener A listener for changed identity, extended from
     * {@link IdentityChangedListener}.
     * @param handler The Handler on whose thread to execute the callbacks of the {@code
     * attachCallback} and {@code identityChangedListener} objects. If a null is provided then the
     *                application's main thread will be used.
     *//*

    public void attach(@NonNull AttachCallback attachCallback,
                       @NonNull IdentityChangedListener identityChangedListener,
                       @Nullable Handler handler) {
        attach(handler, null, attachCallback, identityChangedListener);
    }
    */
/** @hide *//*

    public void attach(Handler handler, ConfigRequest configRequest,
                       AttachCallback attachCallback,
                       IdentityChangedListener identityChangedListener) {
        if (VDBG) {
            Log.v(TAG, "attach(): handler=" + handler + ", callback=" + attachCallback
                    + ", configRequest=" + configRequest + ", identityChangedListener="
                    + identityChangedListener);
        }
        if (attachCallback == null) {
            throw new IllegalArgumentException("Null callback provided");
        }
        synchronized (mLock) {
            Looper looper = (handler == null) ? Looper.getMainLooper() : handler.getLooper();
            try {
                Binder binder = new Binder();
                mService.connect(binder, mContext.getOpPackageName(), mContext.getAttributionTag(),
                        new WifiAwareEventCallbackProxy(this, looper, binder, attachCallback,
                                identityChangedListener), configRequest,
                        identityChangedListener != null);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }
    */
/** @hide *//*

    public void disconnect(int clientId, Binder binder) {
        if (VDBG) Log.v(TAG, "disconnect()");
        try {
            mService.disconnect(clientId, binder);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
    */
/** @hide *//*

    public void publish(int clientId, Looper looper, PublishConfig publishConfig,
                        DiscoverySessionCallback callback) {
        if (VDBG) Log.v(TAG, "publish(): clientId=" + clientId + ", config=" + publishConfig);
        if (callback == null) {
            throw new IllegalArgumentException("Null callback provided");
        }
        try {
            mService.publish(mContext.getOpPackageName(), mContext.getAttributionTag(), clientId,
                    publishConfig,
                    new WifiAwareDiscoverySessionCallbackProxy(this, looper, true, callback,
                            clientId));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
    */
/** @hide *//*

    public void updatePublish(int clientId, int sessionId, PublishConfig publishConfig) {
        if (VDBG) {
            Log.v(TAG, "updatePublish(): clientId=" + clientId + ",sessionId=" + sessionId
                    + ", config=" + publishConfig);
        }
        try {
            mService.updatePublish(clientId, sessionId, publishConfig);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
    */
/** @hide *//*

    public void subscribe(int clientId, Looper looper, SubscribeConfig subscribeConfig,
                          DiscoverySessionCallback callback) {
        if (VDBG) {
            if (VDBG) {
                Log.v(TAG,
                        "subscribe(): clientId=" + clientId + ", config=" + subscribeConfig);
            }
        }
        if (callback == null) {
            throw new IllegalArgumentException("Null callback provided");
        }
        try {
            mService.subscribe(mContext.getOpPackageName(), mContext.getAttributionTag(), clientId,
                    subscribeConfig,
                    new WifiAwareDiscoverySessionCallbackProxy(this, looper, false, callback,
                            clientId));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
    */
/** @hide *//*

    public void updateSubscribe(int clientId, int sessionId, SubscribeConfig subscribeConfig) {
        if (VDBG) {
            Log.v(TAG, "updateSubscribe(): clientId=" + clientId + ",sessionId=" + sessionId
                    + ", config=" + subscribeConfig);
        }
        try {
            mService.updateSubscribe(clientId, sessionId, subscribeConfig);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
    */
/** @hide *//*

    public void terminateSession(int clientId, int sessionId) {
        if (VDBG) {
            Log.d(TAG,
                    "terminateSession(): clientId=" + clientId + ", sessionId=" + sessionId);
        }
        try {
            mService.terminateSession(clientId, sessionId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
    */
/** @hide *//*

    public void sendMessage(int clientId, int sessionId, PeerHandle peerHandle, byte[] message,
                            int messageId, int retryCount) {
        if (peerHandle == null) {
            throw new IllegalArgumentException(
                    "sendMessage: invalid peerHandle - must be non-null");
        }
        if (VDBG) {
            Log.v(TAG, "sendMessage(): clientId=" + clientId + ", sessionId=" + sessionId
                    + ", peerHandle=" + peerHandle.peerId + ", messageId="
                    + messageId + ", retryCount=" + retryCount);
        }
        try {
            mService.sendMessage(clientId, sessionId, peerHandle.peerId, message, messageId,
                    retryCount);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
    */
/** @hide *//*

    @RequiresPermission(android.Manifest.permission.NETWORK_STACK)
    public void requestMacAddresses(int uid, List<Integer> peerIds,
                                    IWifiAwareMacAddressProvider callback) {
        try {
            mService.requestMacAddresses(uid, peerIds, callback);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
    */
/** @hide *//*

    public NetworkSpecifier createNetworkSpecifier(int clientId, int role, int sessionId,
                                                   @NonNull PeerHandle peerHandle, @Nullable byte[] pmk, @Nullable String passphrase) {
        if (VDBG) {
            Log.v(TAG, "createNetworkSpecifier: role=" + role + ", sessionId=" + sessionId
                    + ", peerHandle=" + ((peerHandle == null) ? peerHandle : peerHandle.peerId)
                    + ", pmk=" + ((pmk == null) ? "null" : "non-null")
                    + ", passphrase=" + ((passphrase == null) ? "null" : "non-null"));
        }
        if (!WifiAwareUtils.isLegacyVersion(mContext, Build.VERSION_CODES.Q)) {
            throw new UnsupportedOperationException(
                    "API deprecated - use WifiAwareNetworkSpecifier.Builder");
        }
        if (role != WIFI_AWARE_DATA_PATH_ROLE_INITIATOR
                && role != WIFI_AWARE_DATA_PATH_ROLE_RESPONDER) {
            throw new IllegalArgumentException(
                    "createNetworkSpecifier: Invalid 'role' argument when creating a network "
                            + "specifier");
        }
        if (role == WIFI_AWARE_DATA_PATH_ROLE_INITIATOR || !WifiAwareUtils.isLegacyVersion(mContext,
                Build.VERSION_CODES.P)) {
            if (peerHandle == null) {
                throw new IllegalArgumentException(
                        "createNetworkSpecifier: Invalid peer handle - cannot be null");
            }
        }
        return new WifiAwareNetworkSpecifier(
                (peerHandle == null) ? WifiAwareNetworkSpecifier.NETWORK_SPECIFIER_TYPE_IB_ANY_PEER
                        : WifiAwareNetworkSpecifier.NETWORK_SPECIFIER_TYPE_IB,
                role,
                clientId,
                sessionId,
                peerHandle != null ? peerHandle.peerId : 0, // 0 is an invalid peer ID
                null, // peerMac (not used in this method)
                pmk,
                passphrase,
                0, // no port info for deprecated IB APIs
                -1); // no transport info for deprecated IB APIs
    }
    */
/** @hide *//*

    public NetworkSpecifier createNetworkSpecifier(int clientId, @DataPathRole int role,
                                                   @NonNull byte[] peer, @Nullable byte[] pmk, @Nullable String passphrase) {
        if (VDBG) {
            Log.v(TAG, "createNetworkSpecifier: role=" + role
                    + ", pmk=" + ((pmk == null) ? "null" : "non-null")
                    + ", passphrase=" + ((passphrase == null) ? "null" : "non-null"));
        }
        if (role != WIFI_AWARE_DATA_PATH_ROLE_INITIATOR
                && role != WIFI_AWARE_DATA_PATH_ROLE_RESPONDER) {
            throw new IllegalArgumentException(
                    "createNetworkSpecifier: Invalid 'role' argument when creating a network "
                            + "specifier");
        }
        if (role == WIFI_AWARE_DATA_PATH_ROLE_INITIATOR || !WifiAwareUtils.isLegacyVersion(mContext,
                Build.VERSION_CODES.P)) {
            if (peer == null) {
                throw new IllegalArgumentException(
                        "createNetworkSpecifier: Invalid peer MAC - cannot be null");
            }
        }
        if (peer != null && peer.length != 6) {
            throw new IllegalArgumentException("createNetworkSpecifier: Invalid peer MAC address");
        }
        return new WifiAwareNetworkSpecifier(
                (peer == null) ? WifiAwareNetworkSpecifier.NETWORK_SPECIFIER_TYPE_OOB_ANY_PEER
                        : WifiAwareNetworkSpecifier.NETWORK_SPECIFIER_TYPE_OOB,
                role,
                clientId,
                0, // 0 is an invalid session ID
                0, // 0 is an invalid peer ID
                peer,
                pmk,
                passphrase,
                0, // no port info for OOB APIs
                -1); // no transport protocol info for OOB APIs
    }*/
