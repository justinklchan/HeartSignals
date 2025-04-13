package com.example.microphone;

import android.app.Activity;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

public class Worker  extends AsyncTask<Void, Void, Void> {
    int freq;
    double vol;
    Context context;
    int fs;
    int length;
    String fname;
    Activity av;
    Vibrator vibrator;

    public Worker(Context context, Activity av, int freq, double vol, int length, int fs, String fname, Vibrator vibrator) {
        this.freq = freq;
        this.av = av;
        this.vol=vol;
        this.length = length;
        this.context = context;
        this.fs = fs;
        this.fname = fname;
        this.vibrator = vibrator;
    }

    @Override
    protected void onPostExecute(Void unused) {
        super.onPostExecute(unused);
        Constants.startButton.setEnabled(true);
        Constants.stopButton.setEnabled(false);
        FileOperations.writetofile(av,fname);
        Constants.start=false;
    }

    @Override
    protected Void doInBackground(Void... voids) {
//        short[] tone = Tone.generateTone(freq,1,fs);

        OfflineRecorder rec = new OfflineRecorder(MediaRecorder.AudioSource.MIC,fs,fs*length, context, fname, freq);
        rec.start();

//        long[] pattern = {0, 1000, 1000, 1000, 1000, 1000};
        long[] pattern = {0, 1000, 1000, 1000, 1000, 1000};
        VibrationEffect effect = VibrationEffect.createWaveform(pattern, -1); // no repeat
        vibrator.vibrate(effect);
//        short[] chirp = Utils.generateChirpSpeaker(1000,4000,.1,.1, 48000,0,.5);
//        AudioSpeaker speaker = new AudioSpeaker(context, chirp, fs);
//        speaker.play(vol,3);

        Log.e("asdf","start");
        try {
            Thread.sleep(length*1000);
        }
        catch(Exception e){
            Log.e("asdf","Asdf");
        }
//        speaker.track1.stop();
        rec.halt();
        Log.e("asdf","stop");
        return null;
    }
}
