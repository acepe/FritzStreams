package com.schriek.rtmpdump;

import android.util.Log;

public class Rtmpdump {

    private final String tag = "Rtmpdump";

    public Rtmpdump() {
        LoadLib();
    }

    public void parseString(String str) {
        String[] split = str.split(" ");

        run(split);
    }

    public void testHelpOutput() {
        run(new String[] { "rtmpdump", "-h" });
    }

    protected void LoadLib() {

        try {
            System.loadLibrary("dump");
            testNative();
        } catch (UnsatisfiedLinkError e) {
            Log.e(tag, e.getMessage());
        }
    }

    private native void testNative();

    public native void run(String[] args);
}
