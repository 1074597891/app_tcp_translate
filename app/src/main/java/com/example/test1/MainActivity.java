package com.example.test1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;


class MyTcpClient{
    private Socket client;
    private OutputStream out;
    private InputStream in;
    public int serverStatus =1;



    public boolean connect(String IP , int Port){
        boolean isConnect = false;
        try {
            if (client == null){
                client = new Socket();
            }
            SocketAddress socketAddress = new InetSocketAddress(IP, Port);
            client.connect(socketAddress,2000);
            if(client.isConnected()){
                Log.v("MyTcpClient------->", "成功连接服务器");
                isConnect = true;
            }
        }catch (IOException e) {
            Log.v("MyTcpClient------->","连接服务器失败"+e.getMessage());
            isConnect = false;
            e.printStackTrace();
        }
        return isConnect;
    }

    public  void sendMsg(String msg){
        try{
            if(out == null) {
                out = client.getOutputStream();
            }
            out.write(msg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String rcvMsg(){
        byte[] msg = new byte[128];
        int msg_len;
        String Msg = null;
        try{
            if(in == null){
                in = client.getInputStream();
            }
            msg_len = in.read(msg);
            if(msg_len == -1){
                serverStatus = msg_len;
                Log.v("MyTcpClient------->", "服务已断开 ");
                closeAll();
                return null;
            }
            Msg = new String(msg,0,msg_len);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Msg;
    }
    public  void closeAll(){
        try{
            if(out != null){
                out.close();
                out = null;
            }
            if(in != null){
                in.close();
                in = null;
            }
            if(client != null){
                client.close();
                client = null;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public  void disConnect() {closeAll();}

}

public class MainActivity<hand, MyTcpClient> extends AppCompatActivity {
    private com.example.test1.MyTcpClient myTcpClient;

    private EditText ip_et;
    private EditText port_et;
    private TextView temp_tv;
    private TextView humi_tv;
    private Handler handler;
    private Button connect_btn;

    private int connect_flag;

    private void InitView() {
        connect_flag = 0;

        ip_et = findViewById(R.id.ip_et);
        port_et = findViewById(R.id.port_et);
        temp_tv = findViewById(R.id.temp_tv);
        humi_tv = findViewById(R.id.humi_tv);
        connect_btn = findViewById(R.id.connect_btn);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitView();

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:
                        connect_flag = 1;
                        rcvMsgHandler();
                        connect_btn.setText("断开连接");
                        Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        connect_flag = 0;
                        Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        connect_flag = 0;
                        connect_btn.setText("连 接");
                        Toast.makeText(MainActivity.this, "连接已断开", Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        String Msg = msg.obj.toString();
                        String [] S0 = Msg.split(",");
                        String [] S1 = S0[0].split(":");
                        String [] S2 = S0[1].split(":");
                        String S_eq1 = "en";
                        String S_eq2 = "ch";
                        if (S1[0].equals(S_eq1)){
                            String temp = S1[1];
                            temp_tv.setText(temp);
                        }
                        if (S2[0].equals(S_eq2)) {
                            String humi = S2[1];
                            try {
                                String ch_humi = new String(humi.getBytes(),"GBK");
                                Log.v("123", ch_humi);
                                humi_tv.setText(ch_humi);
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                }
            }
        };

    }


    private void MyConnect() {
        String IP = ip_et.getText().toString().trim();
        String PORT = port_et.getText().toString();
        int Port;

        if (connect_flag == 0) {
            myTcpClient = new com.example.test1.MyTcpClient();
            if (TextUtils.isEmpty(IP)) {
                Toast.makeText(MainActivity.this, "请输入IP", Toast.LENGTH_SHORT).show();
                ip_et.requestFocus();
            } else if (TextUtils.isEmpty(PORT)) {
                Toast.makeText(MainActivity.this, "请输入端口号", Toast.LENGTH_SHORT).show();
                port_et.requestFocus();
            } else {
                Port = Integer.parseInt(PORT);
                new Thread(() -> {
                    if (myTcpClient.connect(IP, Port)) {
                        Message msg = new Message();
                        msg.what = 1;
                        handler.sendMessage(msg);
                    } else {
                        Message msg = new Message();
                        msg.what = 2;
                        handler.sendMessage(msg);
                    }
                }).start();
            }
        } else {
            myTcpClient.disConnect();
            Message msg = new Message();
            msg.what = 3;
            handler.sendMessage(msg);
        }
    }


    public void Connect(View v) {
        MyConnect();
    }

    private void rcvMsgHandler() {
        new Thread(() -> {
            while (true) {
                if (myTcpClient.serverStatus != -1) {
                    String Msg = myTcpClient.rcvMsg();
                    if (Msg != null) {
                        Log.v("接收服务端的消息", Msg);

                        Message msg = new Message();
                        msg.what = 4;
                        msg.obj = Msg;
                        handler.sendMessage(msg);
                    } else {
                        Log.v("rcvMsgHandle:", "内存已释放!!!");
                        break;
                    }
                } else {
                    break;
                }
            }
        }).start();
    }
}






