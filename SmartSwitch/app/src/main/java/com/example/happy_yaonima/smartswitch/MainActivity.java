package com.example.happy_yaonima.smartswitch;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {
    private final static int REQUEST_CONNECT_DEVICE = 1;    //宏定义查询设备句柄
    private BluetoothDevice device;
    /*创建全局变量蓝牙适配器*/
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket clientSocket;
    //输入流，用来接收蓝牙数据;输出流
    private InputStream is;
    private OutputStream os;
    /*创建本地蓝牙串口UUID*/
    private final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");

    boolean bThread = false;
    boolean bRun = false;

    private String smsg = "";    //显示用数据缓存
    private String fmsg = "";    //保存用数据缓存
    private TextView dis;        //接受数据柄
    private EditText setTimeText;   //接受数据输入句柄
    private Button setTimeBtn;    //接受定时开关指令
    Button openBulb;            //接受开关灯指令

    //参数初始化
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //得到数据显示句柄,数据输入句柄，开灯按钮，定时开关按钮
        dis = (TextView) findViewById(R.id.in);
        setTimeText = (EditText) findViewById(R.id.setTimeText) ;
        openBulb = (Button) findViewById(R.id.openBulb);
        setTimeBtn = (Button) findViewById(R.id.setTimeBtn);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //如果打开本地蓝牙设备不成功，提示信息，结束程序,Toast 简易消息提示框
        if (mBluetoothAdapter == null){
            Toast.makeText(this, "打开本地蓝牙失败，确定手机是否有蓝牙功能", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        //设置设备可以被搜索
        new Thread() {
            @Override
            public void run() {
                if(mBluetoothAdapter.isEnabled() == false)
                    mBluetoothAdapter.enable();
            }
        }.start();
    }

    //开灯按键响应函数
    public void onSwitchBulbClicked(View v){
        if (clientSocket == null){
            Toast.makeText(MainActivity.this, "请先连接设备", Toast.LENGTH_SHORT).show();
        }else{
            if (openBulb.getText() == "开灯"){
                send("open");
                openBulb.getBackground().setAlpha(50);
                setTimeBtn.getBackground().setAlpha(50);
                openBulb.setText("关灯");
                setTimeBtn.setText("定时关灯");
            }else/* if (openBulb.getText() == "关灯")*/ {
                send("close");
                openBulb.getBackground().setAlpha(200);
                setTimeBtn.getBackground().setAlpha(200);
                openBulb.setText("开灯");
                setTimeBtn.setText("定时开灯");
            }
        }

    }

    //定时发送开关灯命令
    public  void onSetTimeOpenBulb(View v){
        //转化成是string类型
        String mText = setTimeText.getText().toString();
        //判断用户输入的定时是否是数字
        //TODO 对用户定时的时间是否有要求
        Pattern p = Pattern.compile("[0-9]*");
        Matcher m = p.matcher(mText);
        if (clientSocket == null){
            Toast.makeText(MainActivity.this, "请先连接设备", Toast.LENGTH_SHORT).show();
        }else{
            if (mText == null){
                Toast.makeText(MainActivity.this, "请先输入时间", Toast.LENGTH_SHORT).show();
            }else{
                if (m.matches()){
                    if (setTimeBtn.getText() == "定时开灯"){
                        send("open:"+mText);
                        Toast.makeText(MainActivity.this, "在"+mText+"分钟后"+setTimeBtn.getText(), Toast.LENGTH_SHORT).show();
                        setTimeText.setText(null);
                        /*openBulb.getBackground().setAlpha(50);
                        setTimeBtn.getBackground().setAlpha(50);
                        setTimeBtn.setText("定时关灯");
                        openBulb.setText("关灯");*/
                    }else/* if (setTimeBtn.getText() == "定时开灯")*/{
                        send("close:"+mText);
                        Toast.makeText(MainActivity.this, "在"+mText+"分钟后"+setTimeBtn.getText(), Toast.LENGTH_SHORT).show();
                        setTimeText.setText(null);
                        /*openBulb.getBackground().setAlpha(200);
                        setTimeBtn.getBackground().setAlpha(200);
                        setTimeBtn.setText("定时开灯");
                        openBulb.setText("开灯");*/
                    }
                }else{
                    Toast.makeText(MainActivity.this, "请输入数字", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    //退出按键响应函数
    public void onQuitButtonClicked(View v){
        finish();
    }

    //跳转到蓝牙设备连接页
    public void switchConnect(View view)
    {
        if(mBluetoothAdapter.isEnabled()==false){  //如果蓝牙服务不可用则提示
            Toast.makeText(this, " 打开蓝牙中...（请手动打开您的蓝牙）", Toast.LENGTH_LONG).show();
            return;
        }

        Button btn = (Button) findViewById(R.id.connectBluetooth);
        if(clientSocket==null){
            //如未连接设备则打开ConnectActivity进行设备搜索
            Intent serverIntent = new Intent(this, ConnectActivity.class); //跳转程序设置
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);  //设置返回宏定义
        }
        else{
            //如连接了设备关闭连接socket
            try{
                is.close();
                clientSocket.close();
                clientSocket = null;
                bRun = false;
                btn.setText("连接");
            }catch(IOException e){}
        }
        return;
    }

    //接受活动结果，响应startActivityForResult（）
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       switch (requestCode){
           case REQUEST_CONNECT_DEVICE:  //连接结果，由ConnectActivity设置返回
               // 响应返回结果
               if (resultCode == Activity.RESULT_OK) {   //连接成功，由DeviceListActivity设置返回
                   // MAC地址，由DeviceListActivity设置返回
                   String address = data.getExtras()
                           .getString(ConnectActivity.EXTRA_DEVICE_ADDRESS);
                   // 得到蓝牙设备句柄
                   device = mBluetoothAdapter.getRemoteDevice(address);

                   // 用服务号得到socket
                   try{
                       clientSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                   }catch(IOException e){
                       Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
                   }
                   //连接socket
                   Button btn = (Button) findViewById(R.id.connectBluetooth);
                   try{
                       clientSocket.connect();
                       Toast.makeText(this, "连接"+device.getName()+"成功！", Toast.LENGTH_SHORT).show();
                       btn.setText("断开");
                   }catch(IOException e){
                       try{
                           Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
                           clientSocket.close();
                           clientSocket = null;
                       }catch(IOException ee){
                           Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
                       }
                       return;
                   }

                   //打开接收线程
                   try {
                       is = clientSocket.getInputStream();   //得到蓝牙数据输入流
                   } catch (IOException e) {
                       Toast.makeText(this, "接收数据失败！", Toast.LENGTH_SHORT).show();
                       return;
                   }
                   if (!bThread) {
                       ReadThread.start();
                       bThread = true;
                   } else {
                       bRun = true;
                   }
               }
               break;
           default:
               break;
       }
    }

    //接收数据线程
    Thread ReadThread = new Thread() {

        @Override
        public void run() {
            int num = 0;
            byte[] buffer = new byte[1024];
            byte[] buffer_new = new byte[1024];
            int i = 0;
            int n = 0;
            bRun = true;
            //接收线程
            while (true) {
                try {
                    while (is.available() == 0) {
                        while (bRun == false) {
                        }
                    }
                    while (true) {
                        num = is.read(buffer);         //读入数据
                        n = 0;

                        String s0 = new String(buffer, 0, num);
                        fmsg += s0;    //保存收到数据
                        for (i = 0; i < num; i++) {
                            if ((buffer[i] == 0x0d) && (buffer[i + 1] == 0x0a)) {
                                buffer_new[n] = 0x0a;
                                i++;
                            } else {
                                buffer_new[n] = buffer[i];
                            }
                            n++;
                        }
                        String s = new String(buffer_new, 0, n);
                        smsg = s;   //写入接收缓存
                        if (is.available() == 0) break;  //短时间没有数据才跳出进行显示
                    }
                    //发送显示消息，进行显示刷新
                    handler.sendMessage(handler.obtainMessage());
                } catch (IOException e) {
                }
            }
        }
    };

    //消息处理队列
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            dis.setText(smsg);   //显示数据
            //每当灯的状态发生改变，开关的指令显示发生变化
            if (smsg.equals("open") ){
                setTimeBtn.setText("定时关灯");
                openBulb.setText("关灯");
            }else/* if (smsg == "close")*/{
                setTimeBtn.setText("定时开灯");
                openBulb.setText("开灯");
            }
        }
    };

    //给对方发送消息的函数
    public void send(String com) {
        int i=0;
        int n=0;
        try{
            OutputStream os = clientSocket.getOutputStream();   //蓝牙连接输出流
            byte[] bos = com.getBytes();
            for(i=0;i<bos.length;i++){
                if(bos[i]==0x0a)n++;
            }
            byte[] bos_new = new byte[bos.length+n];
            n=0;
            for(i=0;i<bos.length;i++){ //手机中换行为0a,将其改为0d 0a后再发送
                if(bos[i]==0x0a){
                    bos_new[n]=0x0d;
                    n++;
                    bos_new[n]=0x0a;
                }else{
                    bos_new[n]=bos[i];
                }
                n++;
            }
            os.write(bos_new);
        }catch(IOException e){
        }
    }

    //关闭程序掉用处理部分
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (clientSocket != null)  //关闭连接socket
            try {
                clientSocket.close();
            } catch (IOException e) {
            }
        //	_bluetooth.disable();  //关闭蓝牙服务
    }



}

