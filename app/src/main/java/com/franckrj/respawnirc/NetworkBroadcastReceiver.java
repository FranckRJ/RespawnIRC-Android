package com.franckrj.respawnirc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkBroadcastReceiver extends BroadcastReceiver {
    private static boolean isConnectedWithWifi = false;

    public static void updateConnectionInfos(Context context) {
        ConnectivityManager connectManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;

        if (connectManager != null) {
            activeNetwork = connectManager.getActiveNetworkInfo();
        }

        //noinspection SimplifiableIfStatement
        if (activeNetwork != null) {
            isConnectedWithWifi = (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI);
        } else {
            isConnectedWithWifi = false;
        }
    }

    public static boolean getIsConnectedWithWifi() {
        return isConnectedWithWifi;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        updateConnectionInfos(context);
    }
}
