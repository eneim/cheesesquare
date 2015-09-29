package com.support.android.designlibdemo;

/**
 * Created by eneim on 9/30/15.
 */
public class MathUtils {
  MathUtils() {
  }

  static int constrain(int amount, int low, int high) {
    return amount < low ? low : (amount > high ? high : amount);
  }

  static float constrain(float amount, float low, float high) {
    return amount < low ? low : (amount > high ? high : amount);
  }
}
