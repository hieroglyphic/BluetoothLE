package com.qb20nh.bluetoothle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.List;

// TODO 1. 스캔한 결과 배열에 저장
// TODO 2. 맥어드레스로 필터링
// TODO 3. RSSI값으로 내림차순 정렬
// TODO 4. ListView에 표시
// TODO 5. 데이터 변경시 저절로 적용되도록

public class MainActivity extends AppCompatActivity {

    private final int REQUEST_BT_ENABLE = 1;

    private BluetoothManager btManager;
    private BluetoothAdapter btAdapter;
    private BluetoothGatt btGattA;
    private BluetoothGattCharacteristic btCharA;
    private BluetoothDevice btDeviceA;
    private String TAG = "디버그";
    private static final long SCAN_PERIOD = 3000;

    private String targetNameA = "HMSoft";

    BluetoothGattCallback btCallbackA;

    {
        btCallbackA = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);

                btGattA = gatt;

                if (BluetoothGatt.STATE_CONNECTED == newState) {
                    Log.d(TAG, "연결됨");
                    gatt.discoverServices();

                } else {
                    Log.d(TAG, "연결 해제됨");
                }
            }


            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);

                Log.d(TAG,"A");

                List<BluetoothGattService> services = btGattA.getServices();

                for (BluetoothGattService svc : services) {
                    Log.d(TAG, "서비스: " + svc.getUuid().toString());

                    if (svc.getUuid().toString().equals("0000ffe0-0000-1000-8000-00805f9b34fb")) {

                        List<BluetoothGattCharacteristic> chars = svc.getCharacteristics();
                        for (BluetoothGattCharacteristic ch : chars) {
                            Log.d(TAG, "캐릭터리스틱: " + ch.getUuid().toString());

                            if (ch.getUuid().toString().equals("0000ffe1-0000-1000-8000-00805f9b34fb")) {
                                btCharA = ch;
                                gatt.setCharacteristicNotification(ch, true);
                            }
                        }

                    }

                }
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                Log.d(TAG, "onCharacteristicChanged");
                Log.d(TAG, "DESC_A:"+characteristic.getDescriptors().get(0).getValue());
                Log.d(TAG, "DESC_A:"+characteristic.getStringValue(0));
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                Log.d(TAG, "onCharacteristicWrite");
                Log.d(TAG, "DESC_A:"+characteristic.getDescriptors().get(0).getValue());
                Log.d(TAG, "DESC_A:"+characteristic.getStringValue(0));
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                Log.d(TAG, "onCharacteristicRead");
                Log.d(TAG, "DESC_A:"+characteristic.getDescriptors().get(0).getValue());
                Log.d(TAG, "DESC_A:"+characteristic.getStringValue(0));
            }


        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        this.btAdapter = btManager.getAdapter();
        requestBtEnable();
    }

    private void requestBtEnable() {
        if (!btAdapter.isEnabled()) { // 블루투스 온오프 체크
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_BT_ENABLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("블루투스 활성화 필요")
                    .setMessage("어플리케이션이 블루투스 통신을 요청하였습니다. 블루투스 통신을 활성화해주세요.")
                    .setPositiveButton("확인", null)
                    .show();
        }
        switch (requestCode) {
            case REQUEST_BT_ENABLE:
                // Do something
                break;
            default:
                break;
        }
    }

    void sendCmd(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, String command) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            characteristic.setValue(command); //new String(command.getBytes(), StandardCharsets.UTF_8)
        }
        gatt.writeCharacteristic(characteristic);
    }

    void connectDevice(View v) {
        if (btDeviceA != null) {
            btDeviceA.connectGatt(MainActivity.this, false, btCallbackA);
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void btnPressed(View v) {
        requestBtEnable();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Log.d(TAG, "롤리팝 미만");
            final BluetoothAdapter.LeScanCallback leCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecords) {

                    Log.d(TAG, "기기 발견: " + device.getAddress());
                    if (device.getName() != null && device.getName().equals(targetNameA)){
                        btDeviceA = device;
                        Log.d(TAG, "연결될 기기:" + device.getName());
                    }
//                    scanResult.add(device);
                }
            };

            Log.d(TAG, "스캔 시작");
            this.btAdapter.startLeScan(leCallback);
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    btAdapter.stopLeScan(leCallback);
//                    for (BluetoothDevice device : scanResult){
//                        Log.d(TAG, (device.getName() != null) ? device.getName() : device.getAddress());
//                    }
                    Log.d(TAG, "스캔 끝");

                }
            }, SCAN_PERIOD);
        } else {
            Log.d(TAG, "롤리팝 이상");

            final ScanCallback scanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        BluetoothDevice device = result.getDevice();
                        Log.d(TAG, "기기 발견: " + device.getAddress());
                        if (device.getName() != null && device.getName().equals(targetNameA)){
                            btDeviceA = device;
                            Log.d(TAG, "연결될 기기:" + device.getName());
                        }
                    }
                }
            };
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ScanSettings st = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();
                Log.d(TAG, "스캔 시작");
                this.btAdapter.getBluetoothLeScanner().startScan(null, st, scanCallback);
            }
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        btAdapter.getBluetoothLeScanner().stopScan(scanCallback);
                        Log.d(TAG, "스캔 끝");
                    }
                }
            }, SCAN_PERIOD);
        }
    }

}
