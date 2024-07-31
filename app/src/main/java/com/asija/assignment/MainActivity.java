package com.asija.assignment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.asija.assignment.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.jahnen.libaums.core.UsbMassStorageDevice;
import me.jahnen.libaums.core.fs.UsbFile;
import me.jahnen.libaums.core.fs.UsbFileInputStream;

public class MainActivity extends AppCompatActivity {
    private String TAG=MainActivity.class.getSimpleName();
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    private long totalSize = 0;
    private long copiedSize = 0;
    private ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFileCopy();
            }
        });

    }
    private void startFileCopy() {
        UsbMassStorageDevice[] devices = UsbMassStorageDevice.getMassStorageDevices(this );
        UsbMassStorageDevice device=devices[0];
        try {
            device.init();
            UsbFile root = device.getPartitions().get(0).getFileSystem().getRootDirectory();
            totalSize=calculateTotalSize(root.listFiles());
            File folder= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File destinationFile = new File(folder,"assignment");
            UsbFile[] files = root.listFiles();
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    copyFiles(files,destinationFile);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private long calculateTotalSize(UsbFile[] files) {
        long size = 0;
        for (UsbFile file : files) {
            if (!file.isDirectory()) {
                size += file.getLength();
            } else {
                try {
                    size += calculateTotalSize(file.listFiles());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return size;
    }
    public void copyFiles(UsbFile[] files, File destinationFile) {
        if (!destinationFile.exists()) {
            destinationFile.mkdirs();
        }

        for (UsbFile file : files) {
            Log.d(TAG, "Copying: " + file.getName());
            if (!file.isDirectory()) {
                if (file.getName().endsWith(".crt")) {
                    Log.d(TAG, "Skipping: " + file.getName());
                    continue; // Skip .crt files
                }

                File dFile = new File(destinationFile, file.getName());
                try (InputStream is = new UsbFileInputStream(file)) {
                    writeToFile(is, dFile);
                    copiedSize+=dFile.length();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateProgress();
                        }
                    });
                    Log.d(TAG, "Copied: " + file.getName() + " to " + dFile.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                File newDir = new File(destinationFile, file.getName());
                try {
                    copyFiles(file.listFiles(), newDir);
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

    private void updateProgress() {
        double progress=(copiedSize*100)/totalSize;
        binding.progressBar.setProgress((int) progress);
        binding.tvProgress.setText(String.format("%d%s",(int)progress,"%"));
    }
}