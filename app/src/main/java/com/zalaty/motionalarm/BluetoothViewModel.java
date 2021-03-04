package com.zalaty.motionalarm;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.widget.ArrayAdapter;

import androidx.lifecycle.ViewModel;

public class BluetoothViewModel extends ViewModel {
    // TODO: Implement the ViewModel
    private BluetoothAdapter mBTAdapter;
    private ArrayAdapter<String> mBTArrayAdapter;
    private Handler mHandler;
    private ConnectedThread mConnectedThread;
    private BluetoothSocket mBTSocket = null;


    public void setmBTAdapter(BluetoothAdapter _mBTAdapter){
        mBTAdapter = _mBTAdapter;
    }

    public void setmBTArrayAdapter(ArrayAdapter<String> _mBTArrayAdapter){
        mBTArrayAdapter = _mBTArrayAdapter;
    }

    public void setmHandler(Handler _mHandler){
        mHandler = _mHandler;
    }

    public void setmConnectedThread(ConnectedThread _mConnectedThread){
        mConnectedThread = _mConnectedThread;
    }

    public void setmBTSocket(BluetoothSocket _mBTSocket){
        mBTSocket = _mBTSocket;
    }

    public BluetoothAdapter getmBTAdapter(){
        return mBTAdapter;
    }

    public ArrayAdapter<String> getmBTArrayAdapter(){
        return mBTArrayAdapter;
    }

    public Handler getmHandler(){
        return mHandler;
    }

    public ConnectedThread getmConnectedThread() { return mConnectedThread; }

    public BluetoothSocket getmBTSocket() { return mBTSocket; }

}