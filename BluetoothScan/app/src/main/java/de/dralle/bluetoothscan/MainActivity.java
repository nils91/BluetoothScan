package de.dralle.bluetoothscan;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public static final String LOG_TAG = MainActivity.class.getName();
    public static final int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_PERMISSION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SwipeRefreshLayout refresh = (SwipeRefreshLayout) findViewById(R.id.refresh);
        ListView lvDevices = (ListView) findViewById(R.id.lv_devices);

        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                scanForBluetoothDevices();
                refresh.setRefreshing(false);
            }
        });

        checkBluetooth();
    }

    private boolean checkBluetooth() {
        BluetoothAdapter bAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bAdapter == null) {
            Log.w(LOG_TAG, "Bluetooth not supported");
            Toast.makeText(this, getString(R.string.btNotSupported), Toast.LENGTH_LONG).show();
        }
        return bAdapter != null;
    }

    private boolean checkBluetoothOn() {
        BluetoothAdapter bAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!checkBluetooth() || bAdapter == null) {
            return false;
        }
        if (bAdapter.isEnabled()) {
            Log.i(LOG_TAG, "Bluetooth is enabled");
            return true;
        } else {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            return bAdapter.isEnabled();
        }
    }

    private boolean scanForBluetoothDevices() {
        BluetoothAdapter bAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!checkBluetoothOn() || bAdapter == null) {
            return false;
        }
        if (!getPermission(Manifest.permission.ACCESS_COARSE_LOCATION, getString(R.string.btScanPermissionExplanation))) {
            Log.w(LOG_TAG, "Lacking permissions");
            Toast.makeText(this, getString(R.string.btNoScanPermission), Toast.LENGTH_LONG).show();
            return false;
        }
        if (bAdapter.isDiscovering()) {
            Log.i(LOG_TAG, "Restarting scan");
            bAdapter.cancelDiscovery();
        }
        return bAdapter.startDiscovery();


    }

    private boolean getPermission(final String permission, String reason) {
        int locPermission = ContextCompat.checkSelfPermission(this, permission);
        if (locPermission == PermissionChecker.PERMISSION_GRANTED) {
            Log.i(LOG_TAG, "Permission granted: " + permission);
        } else {
            Log.w(LOG_TAG, "Permission not granted: " + permission + ". Now requesting");
            //show permission explanation
            new AlertDialog.Builder(this).setTitle(getString(R.string.permissionRequestDialogTitle)).setMessage(reason).setCancelable(true).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showRequestPermissionDialog(permission,REQUEST_PERMISSION);


                }
            })
            .show();
            }
        return ContextCompat.checkSelfPermission(this, permission) == PermissionChecker.PERMISSION_GRANTED;
    }

    private void showRequestPermissionDialog(String permission, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, permissionRequestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(LOG_TAG, "Request code " + requestCode + " Result code " + resultCode);
        switch (requestCode){
            case REQUEST_ENABLE_BT:
                scanForBluetoothDevices();
                break;
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(LOG_TAG, "Request code " + requestCode);
        for(int i=0;i<permissions.length;i++){
            Log.v(LOG_TAG,"Permission requested: "+permissions[i]);
            switch(permissions[i]){
                case Manifest.permission.ACCESS_COARSE_LOCATION:
                    if(grantResults[i]== PackageManager.PERMISSION_GRANTED){
                        scanForBluetoothDevices();
                    }
            }
        }
    }
}
