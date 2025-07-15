package com.example.microphone;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class FileOperations {

    public static void writetofile(Activity av, String fname) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String dir = av.getExternalFilesDir(null).toString();
                    File path = new File(dir);
                    if (!path.exists()) {
                        path.mkdirs();
                    }
                    File file = new File(dir, fname+"-acc.txt");
                    BufferedWriter outfile = new BufferedWriter(new FileWriter(file,false));
                    int total = Constants.accx.size();
                    int lastLoggedPercent = -1;
                    for (int i = 0; i < Constants.accx.size(); i++) {
                        outfile.append(Constants.acc_ts.get(i)+","+Constants.accx.get(i)+","+Constants.accy.get(i)+","+Constants.accz.get(i));
                        outfile.newLine();
                        int percent = (int) ((i * 100.0f) / total);
                        if (percent != lastLoggedPercent && percent % 10 == 0) {
                            Log.e("AccelWriter", System.currentTimeMillis()+" Progress: " + percent + "%");
                            lastLoggedPercent = percent;
                        }
                    }
                    outfile.flush();
                    outfile.close();

                    file = new File(dir, fname+"-gyro.txt");
                    outfile = new BufferedWriter(new FileWriter(file,false));
                    total = Constants.gyrox.size();
                    lastLoggedPercent = -1;
                    for (int i = 0; i < Constants.gyrox.size(); i++) {
                        outfile.append(Constants.gyro_ts.get(i)+","+Constants.gyrox.get(i)+","+Constants.gyroy.get(i)+","+Constants.gyroz.get(i));
                        outfile.newLine();
                        int percent = (int) ((i * 100.0f) / total);
                        if (percent != lastLoggedPercent && percent % 10 == 0) {
                            Log.e("GyroWriter", System.currentTimeMillis()+" Progress: " + percent + "%");
                            lastLoggedPercent = percent;
                        }
                    }
                    outfile.flush();
                    outfile.close();

//                    file = new File(dir, fname+"-grav.txt");
//                    outfile = new BufferedWriter(new FileWriter(file,false));
//                    for (int i = 0; i < Constants.gravx.size(); i++) {
//                        outfile.append(Constants.gravx.get(i)+","+Constants.gravy.get(i)+","+Constants.gravz.get(i));
//                        outfile.newLine();
//                    }
                    outfile.flush();
                    outfile.close();
                } catch(Exception e) {
                    Log.e("ex", "writeRecToDisk");
                    Log.e("ex", e.getMessage());
                }
                Toast.makeText(av, "Saved successfully", Toast.LENGTH_LONG).show();

                av.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Constants.statusField.setText("finish write");
                    }
                });
            }
        }).run();
    }

    public static void writeToDisk(Context cxt, String fname) {
        Log.e("asdf","file io");
        ((Activity)cxt).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Constants.statusField.setText("start write");
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String dir = cxt.getExternalFilesDir(null).toString();
                    File path = new File(dir);
                    if (!path.exists()) {
                        path.mkdirs();
                    }
                    File file = new File(dir, fname+"-pcg.txt");
                    BufferedWriter outfile = new BufferedWriter(new FileWriter(file,false));
                    for (Short s : Constants.samples) {
                        outfile.append(s+"");
                        outfile.newLine();
                    }
                    outfile.flush();
                    outfile.close();
                } catch(Exception e) {
                    Log.e("ex", "writeRecToDisk");
                    Log.e("ex", e.getMessage());
                }
                ((Activity)cxt).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Constants.statusField.setText("finish write");
                    }
                });
            }
        }).run();
    }
}
