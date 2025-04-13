package com.example.microphone;

public class Utils {

    public static short[] generateChirpSpeaker(double startFreq, double endFreq, double time, double gap, double fs, double initialPhase,double scale) {
        int N = (int) (time * fs);
        short[] ans = new short[(int)((time+gap)*fs)];
        double f = startFreq;
        double k = (endFreq - startFreq) / time;
        double mult=(32767)*scale;
        for (int i = 0; i < N; i++) {
            double t = (double) i / fs;
            double phase = initialPhase + 2*Math.PI*(startFreq * t + 0.5 * k * t * t);
            phase = Normalize(phase);
            ans[i] = (short) (Math.sin(phase) * mult);
        }

        return ans;
    }

    public static double Normalize(double ang) {
        double angle = ang;
        while (angle < 0)
        {
            angle += 2 * Math.PI;
        }
        while (angle >= 2 * Math.PI)
        {
            angle -= 2 * Math.PI;
        }
        return angle;
    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }
    public static boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }
}
