package com.zalaty.motionalarm;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

public class MainActivity extends AppCompatActivity {

    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names

    private BluetoothAdapter mBTAdapter;
    private ArrayAdapter<String> mBTArrayAdapter;


    Button btScanFragment, btAlarmFragment;
    Button btBluetooth;
    TextView tvBluetooth;
    BluetoothViewModel viewModel;

    Integer switchTurn = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btScanFragment = (Button) findViewById(R.id.btScanFragment);
        btAlarmFragment = (Button) findViewById(R.id.btAlarmFragment);
        btBluetooth = (Button) findViewById(R.id.btBluetooth);
        tvBluetooth = (TextView)findViewById(R.id.tvBluetooth);

        mBTArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();

      viewModel = ViewModelProviders.of(this).get(BluetoothViewModel.class);

        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        bluetoothOff();
        loadFragment(new BlankFragment());

        btBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SwitchTurn();
            }
        });

        btScanFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.setmBTAdapter(mBTAdapter);
                viewModel.setmBTArrayAdapter(mBTArrayAdapter);
                loadFragment(new ConnectedFragment());
            }
        });

        btAlarmFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new AlarmFragment());
            }
        });

    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }

    private void SwitchTurn(){
        if (switchTurn.equals(0)){
            switchTurn = 1;
            bluetoothOn();
        }else if (switchTurn.equals(1)){
            switchTurn = 0;
            bluetoothOff();
        }
    }

    private void bluetoothOn(){
        if (!mBTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            tvBluetooth.setText(R.string.bluetooth_enable);
            Toast.makeText(getApplicationContext(),R.string.Bluetooth_turned_on,Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(getApplicationContext(),R.string.Bluetooth_is_already_on, Toast.LENGTH_SHORT).show();
        }
        mBTArrayAdapter.clear();
        //enableButton(true);
        btScanFragment.setEnabled(true);
    }

    private void bluetoothOff(){
        mBTAdapter.disable();
        tvBluetooth.setText(R.string.bluetooth_disabled);
        Toast.makeText(getApplicationContext(),R.string.Bluetooth_turned_Off, Toast.LENGTH_SHORT).show();
        mBTAdapter.cancelDiscovery();
        mBTArrayAdapter.clear();
        enableButton(false);
        loadFragment(new BlankFragment());
    }

    private void enableButton(boolean enableButton){
        btScanFragment.setEnabled(enableButton);
        btAlarmFragment.setEnabled(enableButton);
    }

}