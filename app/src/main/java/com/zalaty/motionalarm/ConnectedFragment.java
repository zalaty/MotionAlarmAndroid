package com.zalaty.motionalarm;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.UUID;


public class ConnectedFragment extends Fragment {

    private BluetoothAdapter mBTAdapter;
    private ArrayAdapter<String> mBTArrayAdapter;
    BluetoothViewModel viewModel;

    ListView lvDevices;
    TextView tvStatus, tvBuffer;
    Button btSend;

    private Handler mHandler;
    private ConnectedThread mConnectedThread;
    private BluetoothSocket mBTSocket = null;

    RelativeLayout currentLayout;
    ToneGenerator toneG;

    private final String TAG = ConnectedFragment.class.getSimpleName();
    private final static int CONNECTING_STATUS = 3;
    private static final UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public final static int MESSAGE_READ = 2;

    public ConnectedFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);

        viewModel = ViewModelProviders.of(getActivity()).get(BluetoothViewModel.class);
        mBTAdapter = viewModel.getmBTAdapter();
        mBTArrayAdapter = viewModel.getmBTArrayAdapter();
        discover();

        mHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg){
                if(msg.what == MESSAGE_READ){
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    if(readMessage.contains("1")){
                        tvBuffer.setText(R.string.alarm);

                        currentLayout.setBackgroundColor(getResources().getColor(R.color.colorRed));
                        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1000);
                        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1000);
                        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1000);

                    }else{
                        tvBuffer.setText(R.string.ready);
                        currentLayout.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                    }

                }

                if(msg.what == CONNECTING_STATUS){
                    if(msg.arg1 == 1)
                        tvStatus.setText(R.string.connected_to_device + " " + msg.obj);
                    else
                        tvStatus.setText(R.string.connection_failed);
                }
            }
        };

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_connected, container, false);

        lvDevices =  view.findViewById(R.id.lvDevices);
        lvDevices.setAdapter(mBTArrayAdapter);
        lvDevices.setOnItemClickListener(mDeviceClickListener);

        tvStatus = view.findViewById(R.id.tvStatus);
        tvBuffer = view.findViewById(R.id.tvBuffer);
        btSend = view.findViewById(R.id.btSend);

        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // si esta conectado, en lugar de enviar un 1, debe enviar otro digito, para que arduino interprete que se apaga la alarma

                sendString("1");
            }
        });
        btSend.setEnabled(false);
        currentLayout = (RelativeLayout) view.findViewById(R.id.rl_layout);

        return view;
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if(!mBTAdapter.isEnabled()) {
                //Toast.makeText(getActivity(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
                tvStatus.setText(R.string.bluetooth_not_on);
                return;
            }

            tvStatus.setText(R.string.connecting);
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) view).getText().toString();
            final String address = info.substring(info.length() - 17);
            final String name = info.substring(0,info.length() - 17);

            btSend.setEnabled(true);

            viewModel.setmHandler(mHandler);
            // Spawn a new thread to avoid blocking the GUI one
            new Thread()
            {
                @Override
                public void run() {
                    boolean fail = false;

                    BluetoothDevice device = mBTAdapter.getRemoteDevice(address);

                    try {
                        mBTSocket = createBluetoothSocket(device);
                        //btSend.setEnabled(true);
                    } catch (IOException e) {
                        fail = true;
                        //Toast.makeText(getActivity(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                        tvStatus.setText(R.string.socket_creation_failed);
                        //btSend.setEnabled(false);
                    }
                    // Establish the Bluetooth socket connection.
                    try {
                        mBTSocket.connect();
                    } catch (IOException e) {
                        try {
                            fail = true;
                            mBTSocket.close();
                            mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                    .sendToTarget();
                        } catch (IOException e2) {
                            //insert code to deal with this
                            //Toast.makeText(getActivity(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                            tvStatus.setText(R.string.socket_creation_failed);
                        }
                    }

                    if(!fail) {
                        mConnectedThread = new ConnectedThread(mBTSocket, mHandler);
                        mConnectedThread.start();

                        mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name)
                                .sendToTarget();
                    }
                }
            }.start();
        }
    };

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            //btSend.setEnabled(true);
            return (BluetoothSocket) m.invoke(device, BT_MODULE_UUID);
        } catch (Exception e) {
            Log.e(TAG, "Could not create Insecure RFComm Connection",e);
            //btSend.setEnabled(false);
        }
        return  device.createRfcommSocketToServiceRecord(BT_MODULE_UUID);
    }

    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                mBTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    private void discover(){
        if(mBTAdapter.isDiscovering()){
            mBTAdapter.cancelDiscovery();
            Toast.makeText(getActivity(),"Discovery stopped",Toast.LENGTH_SHORT).show();
            //tvStatus.setText(R.string.discovery_stopped);
        }
        else{
            if(mBTAdapter.isEnabled()) {
                mBTArrayAdapter.clear(); // clear items
                mBTAdapter.startDiscovery();
                Toast.makeText(getActivity(), "Discovery started", Toast.LENGTH_SHORT).show();
                //tvStatus.setText(R.string.discovery_started);
                getActivity().registerReceiver(blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            }
            else{
                Toast.makeText(getActivity(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
                //tvStatus.setText(R.string.bluetooth_not_on);
                btSend.setEnabled(false);
            }
        }
    }

    private void sendString(String sendString){
        if(mConnectedThread != null) //First check to make sure thread created
            mConnectedThread.write(sendString);
    }
}