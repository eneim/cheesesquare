package com.support.android.designlibdemo;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.view.ViewOutlineProvider;

/**
 * Created by eneim on 9/30/15.
 */
public class ViewUtilsLollipop {
  ViewUtilsLollipop() {
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  static void setBoundsViewOutlineProvider(View view) {
    view.setOutlineProvider(ViewOutlineProvider.BOUNDS);
  }
}
