package com.asija.assignment;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;

import me.jahnen.libaums.core.UsbMassStorageDevice;
import me.jahnen.libaums.core.fs.UsbFile;
import me.jahnen.libaums.core.fs.UsbFileInputStream;

public class MainActivity2 extends AppCompatActivity {
    private static String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private String TAG=MainActivity2.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        UsbMassStorageDevice[] devices = UsbMassStorageDevice.getMassStorageDevices(this );
        UsbMassStorageDevice device=devices[0];
        try {
            device.init();
        } catch (IOException e) {
            e.printStackTrace();
        }

       UsbFile root = device.getPartitions().get(0).getFileSystem().getRootDirectory();
        File folder= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File destinationFile = new File(folder,"assignment");
        destinationFile.mkdir();
        Log.d(TAG,destinationFile.toString());

        try {
            UsbFile[] files = root.listFiles();
            new Thread(){
                @Override
                public void run() {
                    copyFiles(files,destinationFile);
                }
            }.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyFiles(UsbFile[] files, File destinationFile) {
        for(UsbFile file: files) {
            Log.d(TAG, file.getName());
            if(!file.isDirectory()) {
                InputStream is = new UsbFileInputStream(file);
                File dfile = new File(destinationFile,file.getName());
                if (file.getName().endsWith("*.crt")){
                    return;
                }
                try {
                    writeToFile(is,dfile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void writeToFile(InputStream inputStream, File file) throws IOException {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}