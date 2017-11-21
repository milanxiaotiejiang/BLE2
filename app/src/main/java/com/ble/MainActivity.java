package com.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private BluetoothManager bluetoothManager;//蓝牙管理
    private BluetoothAdapter bluetoothAdapter;//蓝牙适配器

    private Handler mHandler = new Handler();

    private String h1 = "E8:EB:11:0A:FF:7C";
    private String h2 = "9C:1D:58:94:F3:13";

    private BluetoothDevice device1;
    private BluetoothDevice device2;

    private BluetoothGatt bluetoothGatt1;
    private BluetoothGatt bluetoothGatt2;
    private List<BluetoothGatt> bluetoothGattList;

    private BluetoothGattCharacteristic mCharacteristic1;
    private BluetoothGattCharacteristic mCharacteristic2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        bluetoothGattList = new ArrayList<>();
        scan();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stop();
                connect();
            }
        }, 5000);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                send();
            }
        }, 15000);
    }

    private void scan() {

        bluetoothAdapter.getBluetoothLeScanner().startScan(new MyScanCallback());
    }

    private void stop() {
        bluetoothAdapter.getBluetoothLeScanner().stopScan(new MyScanCallback());
    }

    private void connect() {
        if (device1 != null && device2 != null) {

            device1.connectGatt(this, false, coreGattCallback);
            device2.connectGatt(this, false, coreGattCallback);
        }
    }

    private void connect1() {
        if (device1 != null) {

            device1.connectGatt(this, false, coreGattCallback);
        }
    }

    private void send() {
        if (bluetoothGattList.size() == 2) {
            for (int i = 0; i < bluetoothGattList.size(); i++) {
                if (i == 0) {
                    bluetoothGatt1 = bluetoothGattList.get(i);
                    List<List<BluetoothGattCharacteristic>> chats = displayGattServices(bluetoothGatt1.getServices(), i);
                }
                if (i == 1) {
                    bluetoothGatt2 = bluetoothGattList.get(i);
                    List<List<BluetoothGattCharacteristic>> chats = displayGattServices(bluetoothGatt2.getServices(), i);
                }
            }
        }
        if (bluetoothGatt1 != null && bluetoothGatt2 != null && mCharacteristic1 != null && mCharacteristic2 != null) {
            mCharacteristic1.setValue("(1)");
            bluetoothGatt1.writeCharacteristic(mCharacteristic1);
            mCharacteristic2.setValue("(2)");
            bluetoothGatt2.writeCharacteristic(mCharacteristic2);
        }

    }

    private void send1() {
        if (bluetoothGattList.size() == 1) {
            for (int i = 0; i < bluetoothGattList.size(); i++) {
                if (i == 0) {
                    bluetoothGatt1 = bluetoothGattList.get(i);
                    List<List<BluetoothGattCharacteristic>> chats = displayGattServices(bluetoothGatt1.getServices(), i);
                }
            }
        }

        if (bluetoothGatt1 != null && mCharacteristic1 != null) {
            mCharacteristic1.setValue("(1)");
            bluetoothGatt1.writeCharacteristic(mCharacteristic1);
        }
    }

    /**
     * 蓝牙所有相关操作的核心回调类
     */
    private BluetoothGattCallback coreGattCallback = new BluetoothGattCallback() {

        /**
         * 连接状态改变，主要用来分析设备的连接与断开
         * @param gatt GATT
         * @param status 改变前状态
         * @param newState 改变后状态
         */
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            Log.e(TAG, "onConnectionStateChange  status: " + status + " ,newState: " + newState +
                    "  ,thread: " + Thread.currentThread().getId());
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                gatt.discoverServices();
                Log.e(TAG, "连接成功");
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                Log.e(TAG, "连接失败");
            } else if (newState == BluetoothGatt.STATE_CONNECTING) {
                Log.e(TAG, "连接成功");
            }
        }

        /**
         * 发现服务，主要用来获取设备支持的服务列表
         * @param gatt GATT
         * @param status 当前状态
         */
        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            Log.e(TAG, "onServicesDiscovered  status: " + status);
            if (status == 0) {
                Log.e(TAG, "发现服务成功");
                bluetoothGattList.add(gatt);

            } else {
                Log.e(TAG, "发现服务失败");
            }
        }

        /**
         * 读取特征值，主要用来读取该特征值包含的可读信息
         * @param gatt GATT
         * @param characteristic 特征值
         * @param status 当前状态
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
            Log.e(TAG, "onCharacteristicRead  status: " + status + ", data:" + HexUtil.encodeHexStr(characteristic.getValue()));
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "读取特征值成功");
            } else {
                Log.e(TAG, "读取特征值失败");
            }
        }

        /**
         * 写入特征值，主要用来发送数据到设备
         * @param gatt GATT
         * @param characteristic 特征值
         * @param status 当前状态
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
            Log.e(TAG, "onCharacteristicWrite  status: " + status + ", data:" + HexUtil.encodeHexStr(characteristic.getValue()));
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "发送成功");
            }else{
                Log.e(TAG, "发送失败");
            }
        }

        /**
         * 特征值改变，主要用来接收设备返回的数据信息
         * @param gatt GATT
         * @param characteristic 特征值
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            Log.e(TAG, "onCharacteristicChanged data:" + HexUtil.encodeHexStr(characteristic.getValue()));
        }

        /**
         * 读取属性描述值，主要用来获取设备当前属性描述的值
         * @param gatt GATT
         * @param descriptor 属性描述
         * @param status 当前状态
         */
        @Override
        public void onDescriptorRead(BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, final int status) {
            Log.e(TAG, "onDescriptorRead  status: " + status + ", data:" + HexUtil.encodeHexStr(descriptor.getValue()));
        }

        /**
         * 写入属性描述值，主要用来根据当前属性描述值写入数据到设备
         * @param gatt GATT
         * @param descriptor 属性描述值
         * @param status 当前状态
         */
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, final int status) {
            Log.e(TAG, "onDescriptorWrite  status: " + status + ", data:" + HexUtil.encodeHexStr(descriptor.getValue()));
        }

        /**
         * 阅读设备信号值
         * @param gatt GATT
         * @param rssi 设备当前信号
         * @param status 当前状态
         */
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, final int rssi, final int status) {
            Log.e(TAG, "onReadRemoteRssi  status: " + status + ", rssi:" + rssi);
        }
    };

    class MyScanCallback extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (device1 == null || device2 == null) {
                Log.e(TAG, result.getDevice().toString());
                if (result.getDevice().toString().equals(h1)) {
                    device1 = result.getDevice();
                }
                if (result.getDevice().toString().equals(h2)) {
                    device2 = result.getDevice();
                }
            }
//            if(device1 == null) {
//                Log.e(TAG, result.getDevice().toString());
//                if (result.getDevice().toString().equals(h1)) {
//                    device1 = result.getDevice();
//                }
//            }
        }
    }

    private List<List<BluetoothGattCharacteristic>> displayGattServices(final List<BluetoothGattService> gattServices, int i) {
        if (gattServices == null) return null;

        List<List<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();

        for (final BluetoothGattService gattService : gattServices) {
//            Log.e(TAG, "group : " + gattService.getUuid().toString());
            List<BluetoothGattCharacteristic> charas = new ArrayList<>();

            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();

            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
//                Log.e(TAG, "item   : " + gattCharacteristic.getUuid().toString());
                mGattCharacteristics.add(charas);
                if (gattCharacteristic.getUuid().toString().equals("0000ffe1-0000-1000-8000-00805f9b34fb")) {
                    if(i == 0){
                        mCharacteristic1 = gattCharacteristic;
                    }
                    if(i == 1){
                        mCharacteristic2 = gattCharacteristic;
                    }
                }

            }
        }

        return mGattCharacteristics;
    }

}
