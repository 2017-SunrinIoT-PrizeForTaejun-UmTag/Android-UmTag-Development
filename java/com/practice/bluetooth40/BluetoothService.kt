package com.practice.bluetooth40

import android.app.Service
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log
import java.util.*

/**
 * Created by eka on 2017. 9. 26..
 */
class BluetoothService() : Service() {

    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBluetoothGatt: BluetoothGatt? = null
    private var mHandler = Handler()
    private var string = ""
    private var address = ""
    private var mCharacteristic: BluetoothGattCharacteristic? = null
    private var get: String? = null

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            address = intent.getStringExtra("address")
        }
        if (mBluetoothAdapter == null && address != "") {
            getAdapter()
        }
        if (mBluetoothGatt == null && mBluetoothAdapter != null && address != "")
            getGatt()

        return super.onStartCommand(intent, flags, startId)
    }

    private fun getAdapter() {
        val mBluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = mBluetoothManager.adapter
    }

    private fun getGatt() {
        mBluetoothGatt = mBluetoothAdapter?.getRemoteDevice(address)?.connectGatt(this, true, mBluetoothGattCallback)
        mBluetoothGatt?.discoverServices()
    }

    //요거 중요함 바꿔줄 수도 있음
    private fun setNotification() {
        val services = mBluetoothGatt!!.services
        for (service in services!!) {
            Log.e("uuid", "" + service.uuid.toString() + "\n" + service.characteristics.size)
            if (service.uuid.toString() == "0000ffe0-0000-1000-8000-00805f9b34fb") {
                val char = service.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"))
                mBluetoothGatt?.setCharacteristicNotification(char, true)
                //UUID 넣어줘용
                mCharacteristic = service.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"))
            }
        }
    }

    fun send(string: String): Array<String>? {
        mCharacteristic?.setValue(string)
        mBluetoothGatt?.writeCharacteristic(mCharacteristic)
        return dataProcessing(recieve())
    }

    fun recieve(): String? {
        return get
    }

    fun dataProcessing(string: String?): Array<String>? {
        var str = string?.split(",")
        var returnstr = str?.toTypedArray()
        return returnstr
    }

    private val mBluetoothGattCallback = object : BluetoothGattCallback() {
        //켜지거나 꺼질때 (상태 변화)
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mHandler.post { mBluetoothGatt!!.discoverServices() }
            }
        }

        //블루투스 값 바뀔떄
        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            val data = characteristic!!.value
            string = String(data)
            get = string
        }

        //블루투스 발견 안될떄
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            setNotification()
        }
    }
}