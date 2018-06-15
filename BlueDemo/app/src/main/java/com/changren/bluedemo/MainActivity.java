package com.changren.bluedemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import sn.ISnSecretCode;

public class MainActivity extends AppCompatActivity {

    private TextView tv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tv = new TextView(this);
        tv.setText("SN:\nSecretCode:");
        setContentView(tv);

        //如果与服务端的连接处于未连接状态，则尝试连接
        if (!mBound) {
            attemptToBindService();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mBound) {
            Toast.makeText(this, "当前与服务端处于未连接状态，正在尝试重连，请稍后再试", Toast.LENGTH_SHORT).show();
            attemptToBindService();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mServiceConnection);
            mBound = false;
        }
    }

    /**
     * 尝试与服务端建立连接
     */
    private void attemptToBindService() {
        Intent intent = new Intent();
//        intent.setComponent(new ComponentName("com.sun.healthrobot", "com.sun.healthrobot.service.SnSecretCodeImpl"));
        intent.setComponent(new ComponentName("com.sun.healthrobot", "com.sun.healthrobot.service.SnSecretCodeImpl"));
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    //由AIDL文件生成的Java类
    private ISnSecretCode mSnSecretCode = null;
    //标志当前与服务端连接状况的布尔值，false为未连接，true为连接中
    private boolean mBound = false;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(getLocalClassName(), "service connected");
            mSnSecretCode = ISnSecretCode.Stub.asInterface(service);
            mBound = true;

            if (mSnSecretCode != null) {
                try {
                    String sn_code = mSnSecretCode.getDevSn();
                    String secretCode = mSnSecretCode.getDevSecretCode();
                    tv.setText("SN:\n"+ sn_code +"\nSecretCode:"+ secretCode);
                    Log.e(getLocalClassName(), "SN:\n"+ sn_code +"\nSecretCode:"+ secretCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(getLocalClassName(), "service disconnected");
            tv.setText("SN:\n"+ null +"\nSecretCode:"+ null);
            mBound = false;
        }
    };
}
