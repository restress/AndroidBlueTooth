package com.example.happy_yaonima.smartswitch;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.StreamHandler;

/**
 * Created by Happy_yaonima on 2016/7/8.
 */

public class ConnectActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {
    /*创建全局变量蓝牙适配器*/
    private BluetoothAdapter mcBluetoothAdapter;
    /*创建与xml中相连的listview*/
    private ListView lvDevices;
    /*创建本地蓝牙适配器*/

    /*创建listview对应的数组*/
    private List<String> bluetoothDevices = new ArrayList<String>();
    /*创建数组对应的蓝牙适配器*/
    private ArrayAdapter<String> arrayAdapter;
    /*创建本地UUID*/
    /*private final UUID MY_UUID = UUID
            .fromString("db764ac8-4b08-7f25-aafe-59d03c27bae3");*/

    private final String NAME = "Bluetooth_Socket";
    // 返回时数据标签
    public static String EXTRA_DEVICE_ADDRESS = "设备地址";

    /*private BluetoothSocket clientSocket;*/
    private BluetoothDevice cDevice;
 /*   private AcceptThread acceptThread;*/
    /*private OutputStream os;*/
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_connect);

        // 设定默认返回值为取消
        setResult(Activity.RESULT_CANCELED);

        /*创建蓝牙对象和listview*/
        mcBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        lvDevices = (ListView) findViewById(R.id.deviceList);

        Set<BluetoothDevice> pairedDevices = mcBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                bluetoothDevices.add(device.getName() + ":" + device.getAddress() + "\n");
            }
        }

        arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1,
                bluetoothDevices);

        lvDevices.setAdapter(arrayAdapter);
        lvDevices.setOnItemClickListener((AdapterView.OnItemClickListener) this);
        /*acceptThread = new AcceptThread();
        acceptThread.start();*/

        /*找到了的广播*/
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(receiver, filter);
        /*搜索完发的广播，同一个接收器*/
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(receiver, filter);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    //TODO有问题，点击之后会跳转回来
    //没有必要
   /* public void openBluetooth(View view) {
        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, 1);
      *//*  setProgressBarVisibility(true);*//*
        setTitle("searching");
        if (mcBluetoothAdapter.isDiscovering()) {
            mcBluetoothAdapter.cancelDiscovery();
        }
        mcBluetoothAdapter.startDiscovery();
    }*/

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                cDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_NAME);
                if (cDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
                    bluetoothDevices.add(cDevice.getName() + ":" + cDevice.getAddress() + "\n");
                    arrayAdapter.notifyDataSetChanged();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
               /* setProgressBarVisibility(false);*/
                setTitle("已搜索完成");
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //得到点击设备的adress
        String s = arrayAdapter.getItem(position);
        String address = s.substring(s.indexOf(":") + 1).trim();

        // 设置返回数据
        Intent intent = new Intent();
        intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

        // 设置返回值并结束程序
        setResult(Activity.RESULT_OK, intent);
        finish();

        /*try {
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
            try {
                if (device == null) {
                    device = mBluetoothAdapter.getRemoteDevice(address);
                }
                if (clientSocket == null) {
                    clientSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                    clientSocket.connect();
                    os = clientSocket.getOutputStream();

            }
            } catch (Exception e) {
                // TODO: handle exception
            }
            if (os != null) {
                os.write("发送信息到其他蓝牙设备".getBytes("utf-8"));
            }
        } catch (Exception e) {
           // TODO: handle exception
        }*/

    }

    @Override
    //onDestroy进行一些清理操作
    protected void onDestroy() {
        super.onDestroy();
        // 关闭服务查找
        if (mcBluetoothAdapter!= null) {
            mcBluetoothAdapter.cancelDiscovery();
        }
        // 注销action接收器
        this.unregisterReceiver(receiver);
    }

    //取消按钮onclick事件
    public void onCancle(View v) { finish();}


   /* //TODO 以下部分在MainActivity中进行
    private android.os.Handler handler = new android.os.Handler() {
        public void handleMessage(Message msg) {
            Toast.makeText(ConnectActivity.this, String.valueOf(msg.obj),
                    Toast.LENGTH_LONG).show();
            super.handleMessage(msg);
        }
    };

        @Override
        public void onStart() {
            super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Connect Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.happy_yaonima.smartswitch/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Connect Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.happy_yaonima.smartswitch/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    private class AcceptThread extends Thread {
        private BluetoothServerSocket serverSocket;
        private BluetoothSocket socket;
        private InputStream is;
        private OutputStream os;

        public AcceptThread() {
            try {
                serverSocket = mBluetoothAdapter
                        .listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }

        public void run() {
            try {
                socket = serverSocket.accept();
                is = socket.getInputStream();
                os = socket.getOutputStream();

                while (true) {
                    byte[] buffer = new byte[128];
                    int count = is.read(buffer);
                    Message msg = new Message();
                    msg.obj = new String(buffer, 0, count, "utf-8");
                    handler.sendMessage(msg);
                }
            } catch (Exception e) {
                // TODO: handle exception
            }

        }


    }*/
}
