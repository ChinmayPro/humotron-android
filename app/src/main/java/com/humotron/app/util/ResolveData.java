package com.humotron.app.util;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;
import android.util.Log;

import java.io.UnsupportedEncodingException;

@SuppressLint("MissingPermission")
public class ResolveData {

    private static final int SHORTENED_LOCAL_NAME = 0x08;
    private static final int COMPLETE_LOCAL_NAME = 0x09;

    public static String decodeDeviceName(BluetoothDevice bluetoothDevice, byte[] data) {
        String name = bluetoothDevice.getName();
        if(!TextUtils.isEmpty(name))return name;
        int fieldLength, fieldName;
        int packetLength = data.length;
        for (int index = 0; index < packetLength; index++) {
            fieldLength = data[index];
            if (fieldLength <= 0)
                break;
            fieldName = data[++index];
            if (fieldName == COMPLETE_LOCAL_NAME
                    || fieldName == SHORTENED_LOCAL_NAME) {
                name = decodeLocalName(data, index + 1, fieldLength - 1);
                break;
            }
            index += fieldLength - 1;
        }
        return name;
    }

    public static String decodeLocalName(final byte[] data, final int start,
                                         final int length) {
        try {
            return new String(data, start, length, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            Log.e("scan", "Unable to convert the complete local name to UTF-8",
                    e);
            return null;
        } catch (final IndexOutOfBoundsException e) {
            Log.e("scan", "Error when reading complete local name", e);
            return null;
        }
    }
}
