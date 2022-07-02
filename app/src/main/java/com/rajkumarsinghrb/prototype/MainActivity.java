package com.rajkumarsinghrb.prototype;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;

import android.Manifest;
//import android.app.Activity;
//import android.bluetooth.BluetoothClass;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
//import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    TextView connectionStatus, messageTextView;
    Button aSwitch, discoverButton;
    ListView listView;
    EditText typeMsg;
    ImageButton sendButton; //These are buttons objects 01step

    WifiP2pManager manager;// creating objects for Wifi broadcast receivers
    WifiP2pManager.Channel channel;
    BroadcastReceiver receiver;
    IntentFilter intentFilter;

    List<WifiP2pDevice> peers = new ArrayList<>(); // Object for peerList Listener
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;

    Socket socket; //global socket object

    //static final int MESSAGE_READ = 1;

    ServerClass serverClass;//error 2
    ClientClass clientClass;
    Boolean isHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Creating a method for the button widget for line 22-26
        //Purpose:- To initialize all the objects 02step

        initialWork();//alt+enter to create method
        exqListener();// Method For ON/OFF
    }

    private void exqListener() {
        // ON/OFF Buttons
        aSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Creating intent to access wifi setting under Wifi Section in Setting Option
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivityForResult(intent, 1); // we can use this to get extra information regarding the user.
                //WiFI ON/OFF established here code till on pause

            }
        });
        // For Discover Buttons
        discoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        connectionStatus.setText("Discovery Started");
                        // This message will be shown in Connection Status message panel.

                    }

                    @Override
                    public void onFailure(int i) {
                        connectionStatus.setText("Discovery Started");

                    }
                });
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                final WifiP2pDevice device = deviceArray[i];

                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;

                if (ActivityCompat.checkSelfPermission( getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        connectionStatus.setText("Connected: " + device.deviceAddress);

                    }

                    @Override
                    public void onFailure(int i) {
                        connectionStatus.setText("Not Connected:");

                    }
                });
            }
        });

        //Execute Image button to send message,Button Implementation Work done
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //a blocking method
                //using java concurrency
                ExecutorService executor = Executors.newSingleThreadExecutor();
                String msg = typeMsg.getText().toString();//Whatever typed in the edit text
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        // To confirm Device is host or client
                        if (msg != null && isHost)
                        {
                            serverClass.write(msg.getBytes());
                        }else if (msg != null && !isHost);
                        clientClass.write(msg.getBytes());

                    }
                });
            }
        });
    }

    private void initialWork() {
        connectionStatus = findViewById(R.id.connection_status);
        messageTextView = findViewById(R.id.messageTextView);
        aSwitch = findViewById(R.id.switch1);
        discoverButton = findViewById(R.id.buttonDiscover);
        listView = findViewById(R.id.listView);
        typeMsg = findViewById(R.id.editTextTypeMsg);
        sendButton = findViewById(R.id.sendButton);
        // #3video 6:39 below code
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this,getMainLooper(), null);
        receiver = new WiFiDirectBroadcastReceiver(manager,channel,this);

        intentFilter =new IntentFilter();
        //Now we need to add action for these three intent in Broadcast Receivers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        // Now we need to register the receiver in On Resume and On Pause.
        //Below created Register and unregister Broadcast Receivers.

    }
    // Creating Peer List Listener For Discovery Process
    //line 41-43
    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        private WifiP2pDevice device;

        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
            if (!wifiP2pDeviceList.equals(peers))
            {
                peers.clear();
                peers.addAll(wifiP2pDeviceList.getDeviceList());

                deviceNameArray = new String[wifiP2pDeviceList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[wifiP2pDeviceList.getDeviceList().size()];

                int index = 0;
                for(WifiP2pDevice device : wifiP2pDeviceList.getDeviceList());
                {
                    deviceNameArray[index] = device.deviceName;
                    deviceArray[index] = device;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1);
                listView.setAdapter(adapter);
                if(peers.size() == 0)
                {
                    connectionStatus.setText("No Device Found");
                    return;
                }
            }
        }
    };

    // To get Host and Client Info
    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {

            final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;

            if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner);
            {
                connectionStatus.setText("Host");
                isHost = true; // to confirm Host
                serverClass = new ServerClass();
                serverClass.start();
            }if (wifiP2pInfo.groupFormed)
            {
                connectionStatus.setText("Client");
                isHost = false; // to confirm client
                clientClass =new ClientClass(groupOwnerAddress);
                clientClass.start();
            }
        }
    };

    //Press Ctrl+O for onPostResume();
    @Override
    protected void onPostResume() {
        super.onPostResume();
        registerReceiver(receiver,intentFilter);
    }
    //Press Ctrl+O for onPostResume();

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }
    //Creating Server class 02
    public class ServerClass extends Thread{
        ServerSocket serverSocket;
        private InputStream inputStream;
        private OutputStream outputStream;

        //One method for sending message
        public void write(byte[] bytes){
            try {
                outputStream.write(bytes);//press alt+Enter surround with try catch

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //Now Override run method press Ctrl+O
        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(8888);//surrounded with try catch
                socket = serverSocket.accept();
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            ExecutorService executor =Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    byte[] buffer = new byte[1024];
                    int bytes;

                    while (socket != null) {
                        try {
                            bytes = inputStream.read(buffer);//To find meaning of surround with try catch
                            if (bytes > 0) {
                                int finalBytes = bytes;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        String tempMSG = new String(buffer, 0, finalBytes);
                                        messageTextView.setText(tempMSG);
                                    }
                                });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }


            });
        }
    }

    // Creating client 01
    public class ClientClass extends Thread{
        String hostAdd; //Creating string for the host address
        private InputStream inputStream;
        private OutputStream outputStream;

        //Here adding Constructor Alt+Insert
        public ClientClass(InetAddress hostAddress) {
            hostAdd = hostAddress.getHostAddress();
            socket = new Socket();

            //Need to create Socket Object globally.

        }
        //One method for sending message
        public void write(byte[] bytes){
            try {
                outputStream.write(bytes);//press alt+Enter surround with try catch

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                socket.connect(new InetSocketAddress(hostAdd, 8888),500);//500 is timer
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler =new Handler(Looper.getMainLooper());

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    byte[] buffer = new byte[1024];
                    int bytes;

                    //Always ready to receive message
                    while (socket!= null){
                        try {
                            bytes = inputStream.read(buffer);//To find meaning of surround with try catch
                            if(bytes>0){
                                int finalBytes =bytes;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        String tempMSG = new String(buffer, 0, finalBytes);
                                        messageTextView.setText(tempMSG);
                                    }
                                });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }
}

