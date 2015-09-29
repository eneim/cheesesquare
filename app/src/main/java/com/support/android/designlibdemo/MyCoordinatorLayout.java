package com.support.android.designlibdemo;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by eneim on 9/8/15.
 */
public class MyCoordinatorLayout extends CoordinatorLayout {

  public static final String TAG = "MyCoordinatorLayout";

  public MyCoordinatorLayout(Context context) {
    super(context);
  }

  public MyCoordinatorLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public MyCoordinatorLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
    Log.d(TAG, "onStartNestedScroll");
    return super.onStartNestedScroll(child, target, nestedScrollAxes);
  }

  @Override public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
    super.onNestedPreScroll(target, dx, dy, consumed);
    Log.e(TAG, "onNestedPreScroll");
  }

  @Override
  public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int
      dyUnconsumed) {
    super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
    Log.e(TAG, "onNestedScroll");
  }

  @Override public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
    Log.e(TAG, "onNestedPreFling");
    return super.onNestedPreFling(target, velocityX, velocityY);
  }

  @Override
  public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
    Log.e(TAG, "onNestedFling");
    return super.onNestedFling(target, velocityX, velocityY, consumed);
  }

  @Override public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
    super.onNestedScrollAccepted(child, target, nestedScrollAxes);
    Log.e(TAG, "onNestedScrollAccepted");
  }

  @Override public void onStopNestedScroll(View target) {
    super.onStopNestedScroll(target);
    Log.d(TAG, "onStopNestedScroll");
  }
}
