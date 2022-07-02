package com.rajkumarsinghrb.prototype;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;

import androidx.core.app.ActivityCompat;

import java.net.DatagramSocket;

//type extends Broadcast Receivers
//press Alt+Enter to create method On receive line 11
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
    //Creating 3 objects here
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private MainActivity activity;

    //create a constructor below alt+ins
    //Select all three this methods
    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, MainActivity activity) {
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //creating one string object
        //Using this intent we will get action and save in this action object below.
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            //Broadcast when wifi p2p is enabled or disabled on the device.
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            //call wifiP2pManager.request() to get a list of current peers
            //Peer list Listener connection of main activity.
            if (manager != null) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                manager.requestPeers(channel, activity.peerListListener);
            }
        }else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
            //respond to new connection or disconnection
            if (manager!=null)
            {
                NetworkInfo networkInfo=intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);//new
            }


            DatagramSocket networkInfo = null;
            if(networkInfo.isConnected())//Error to understand
            {
                manager.requestConnectionInfo(channel,activity.connectionInfoListener);
            }else {
                activity.connectionStatus.setText("Device Disconnected");
            }
        }

    }
}
