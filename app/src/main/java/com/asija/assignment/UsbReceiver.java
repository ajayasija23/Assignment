package com.asija.assignment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class UsbReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(intent.getAction())) {
//            Toast.makeText(context, "Usb device detected", Toast.LENGTH_SHORT).show();
            Log.d("device_connection","device connected");
            Intent intent1 = new Intent(context, MainActivity2.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent1);
        }
    }
}
