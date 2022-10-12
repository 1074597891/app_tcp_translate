package com.example.test1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;

public class FunctionActivity extends AppCompatActivity {

    private TextView temp_tv;
    private TextView humi_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_function);
        temp_tv = findViewById(R.id.temp_tv);
        humi_tv = findViewById(R.id.humi_tv);


        Intent intent = getIntent();

        //通过这个标签得到之前的字符串


        String Msg = intent.getStringExtra("amessage");
        String[] S0 = Msg.split(",");
        String[] S1 = S0[0].split(":");
        String[] S2 = S0[1].split(":");
        String S_eq1 = "en";
        String S_eq2 = "ch";
        if (S1[0].equals(S_eq1)) {
            String temp = S1[1];
            temp_tv.setText(temp);
        }
        if (S2[0].equals(S_eq2)) {
            String humi = S2[1];
            humi_tv.setText(humi);


        }
    }
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);//必须要写
        String name = intent.getStringExtra("name");
        String Msg = intent.getStringExtra("amessage");
        String[] S0 = Msg.split(",");
        String[] S1 = S0[0].split(":");
        String[] S2 = S0[1].split(":");
        String S_eq1 = "en";
        String S_eq2 = "ch";
        if (S1[0].equals(S_eq1)) {
            String temp = S1[1];
            temp_tv.setText(temp);
        }
        if (S2[0].equals(S_eq2)) {
            String humi = S2[1];
            humi_tv.setText(humi);


        }
        }
    }