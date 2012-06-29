package com.near.multicore.manager;

public class Log {
    private final String LOGTAG = "MultiCore";

    public void v(String TAG, String logMe) {
        android.util.Log.v(LOGTAG, TAG + ": " + logMe);
    }

    public void d(String TAG, String logMe) {
        android.util.Log.d(LOGTAG, TAG + ": " + logMe);
    }

    public void e(String TAG, String logMe) {
        android.util.Log.e(LOGTAG, TAG + ": " + logMe);
    }

    public void e(String TAG, String logMe, Throwable ex) {
        android.util.Log.e(LOGTAG, TAG + ": " + logMe, ex);
    }

    public void i(String TAG, String logMe) {
        android.util.Log.i(LOGTAG, TAG + ": " + logMe);
    }
}
