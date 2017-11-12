package com.dlazaro66.qrcodereaderview;

import android.util.Log;

public class SimpleLog {

  private static boolean loggingEnabled = false;

  public static void setLoggingEnabled(boolean enabled) {
      loggingEnabled = enabled;
  }

  public static void d(String tag, String text) {
      if (loggingEnabled) {
          Log.d(tag, text);
      }
  }

  public static void w(String tag, String text) {
      if (loggingEnabled) {
          Log.w(tag, text);
      }
  }

  public static void w(String tag, String text, Throwable e) {
      if (loggingEnabled) {
          Log.w(tag, text, e);
      }
  }

  public static void e(String tag, String text) {
      if (loggingEnabled) {
          Log.e(tag, text);
      }
  }

  public static void d(String tag, String text, Throwable e) {
      if (loggingEnabled) {
          Log.d(tag, text, e);
      }
  }

  public static void i(String tag, String text) {
      if (loggingEnabled) {
          Log.i(tag, text);
      }
  }
}
