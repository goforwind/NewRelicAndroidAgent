// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.util;

import com.newrelic.agent.android.logging.AgentLogManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import java.text.MessageFormat;
import android.content.Context;
import com.newrelic.agent.android.logging.AgentLog;

public final class Connectivity
{
    private static final String ANDROID = "Android";
    private static AgentLog log;
    
    public static String carrierNameFromContext(final Context context) {
        NetworkInfo networkInfo;
        try {
            networkInfo = getNetworkInfo(context);
        }
        catch (SecurityException e) {
            return "unknown";
        }
        if (!isConnected(networkInfo)) {
            return "none";
        }
        if (isWan(networkInfo)) {
            return carrierNameFromTelephonyManager(context);
        }
        if (isWifi(networkInfo)) {
            return "wifi";
        }
        Connectivity.log.warning(MessageFormat.format("Unknown network type: {0} [{1}]", networkInfo.getTypeName(), networkInfo.getType()));
        return "unknown";
    }
    
    public static String wanType(final Context context) {
        NetworkInfo networkInfo;
        try {
            networkInfo = getNetworkInfo(context);
        }
        catch (SecurityException e) {
            return "unknown";
        }
        if (!isConnected(networkInfo)) {
            return "none";
        }
        if (isWifi(networkInfo)) {
            return "wifi";
        }
        if (isWan(networkInfo)) {
            return connectionNameFromNetworkSubtype(networkInfo.getSubtype());
        }
        return "unknown";
    }
    
    private static boolean isConnected(final NetworkInfo networkInfo) {
        return networkInfo != null && networkInfo.isConnected();
    }
    
    private static boolean isWan(final NetworkInfo networkInfo) {
        switch (networkInfo.getType()) {
            case 0:
            case 2:
            case 3:
            case 4:
            case 5: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    private static boolean isWifi(final NetworkInfo networkInfo) {
        switch (networkInfo.getType()) {
            case 1:
            case 6:
            case 7:
            case 9: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    private static NetworkInfo getNetworkInfo(final Context context) throws SecurityException {
        final ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService("connectivity");
        try {
            return connectivityManager.getActiveNetworkInfo();
        }
        catch (SecurityException e) {
            Connectivity.log.warning("Cannot determine network state. Enable android.permission.ACCESS_NETWORK_STATE in your manifest.");
            throw e;
        }
    }
    
    private static String carrierNameFromTelephonyManager(final Context context) {
        final TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService("phone");
        final String networkOperator = telephonyManager.getNetworkOperatorName();
        final boolean smellsLikeAnEmulator = Build.PRODUCT.equals("google_sdk") || Build.PRODUCT.equals("sdk") || Build.PRODUCT.equals("sdk_x86") || Build.FINGERPRINT.startsWith("generic");
        if (networkOperator.equals("Android") && smellsLikeAnEmulator) {
            return "wifi";
        }
        return networkOperator;
    }
    
    private static String connectionNameFromNetworkSubtype(final int subType) {
        switch (subType) {
            case 7: {
                return "1xRTT";
            }
            case 4: {
                return "CDMA";
            }
            case 2: {
                return "EDGE";
            }
            case 5: {
                return "EVDO rev 0";
            }
            case 6: {
                return "EVDO rev A";
            }
            case 1: {
                return "GPRS";
            }
            case 8: {
                return "HSDPA";
            }
            case 10: {
                return "HSPA";
            }
            case 9: {
                return "HSUPA";
            }
            case 3: {
                return "UMTS";
            }
            case 11: {
                return "IDEN";
            }
            case 12: {
                return "EVDO rev B";
            }
            case 15: {
                return "HSPAP";
            }
            case 14: {
                return "HRPD";
            }
            case 13: {
                return "LTE";
            }
            default: {
                return "unknown";
            }
        }
    }
    
    static {
        Connectivity.log = AgentLogManager.getAgentLog();
    }
}
