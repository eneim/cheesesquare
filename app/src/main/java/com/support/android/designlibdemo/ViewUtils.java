package com.support.android.designlibdemo;

import android.os.Build;
import android.view.View;

/**
 * Created by eneim on 9/30/15.
 */
public class ViewUtils {
  static final ValueAnimatorCompat.Creator DEFAULT_ANIMATOR_CREATOR = new ValueAnimatorCompat
      .Creator() {
    public ValueAnimatorCompat createAnimator() {
      return new ValueAnimatorCompat((Build.VERSION.SDK_INT >= 12 ?
          new ValueAnimatorCompatImplHoneycombMr1() : new ValueAnimatorCompatImplEclairMr1()));
    }
  };
  private static final ViewUtils.ViewUtilsImpl IMPL;

  static {
    int version = Build.VERSION.SDK_INT;
    if (version >= 21) {
      IMPL = new ViewUtils.ViewUtilsImplLollipop();
    } else {
      IMPL = new ViewUtils.ViewUtilsImplBase();
    }

  }

  ViewUtils() {
  }

  static void setBoundsViewOutlineProvider(View view) {
    IMPL.setBoundsViewOutlineProvider(view);
  }

  static ValueAnimatorCompat createAnimator() {
    return DEFAULT_ANIMATOR_CREATOR.createAnimator();
  }

  private interface ViewUtilsImpl {
    void setBoundsViewOutlineProvider(View var1);
  }

  private static class ViewUtilsImplLollipop implements ViewUtils.ViewUtilsImpl {
    private ViewUtilsImplLollipop() {
    }

    public void setBoundsViewOutlineProvider(View view) {
      ViewUtilsLollipop.setBoundsViewOutlineProvider(view);
    }
  }

  private static class ViewUtilsImplBase implements ViewUtils.ViewUtilsImpl {
    private ViewUtilsImplBase() {
    }

    public void setBoundsViewOutlineProvider(View view) {
    }
  }
}
