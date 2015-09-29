package com.support.android.designlibdemo;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by eneim on 9/8/15.
 */
public class CoordinatorLayout extends android.support.design.widget.CoordinatorLayout {

  public static final String TAG = "MyCoordinatorLayout";

  public CoordinatorLayout(Context context) {
    super(context);
  }

  public CoordinatorLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public CoordinatorLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
    Log.d(TAG, "onStartNestedScroll");
    return super.onStartNestedScroll(child, target, nestedScrollAxes);
  }

  @Override public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
    super.onNestedScrollAccepted(child, target, nestedScrollAxes);
    Log.e(TAG, "onNestedScrollAccepted");
  }

  @Override public void onStopNestedScroll(View target) {
    super.onStopNestedScroll(target);
    Log.d(TAG, "onStopNestedScroll");
  }

  @Override
  public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int
      dyUnconsumed) {
    super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
    Log.e(TAG, "onNestedScroll");
  }

  @Override public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
    super.onNestedPreScroll(target, dx, dy, consumed);
    Log.e(TAG, "onNestedPreScroll");
  }

  @Override
  public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
    Log.e(TAG, "onNestedFling: " + velocityX + " | " + velocityY + " | " + consumed);
    return super.onNestedFling(target, velocityX, velocityY, consumed);
  }

  @Override public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
    Log.e(TAG, "onNestedPreFling: " + velocityX + " | " + velocityY);
    return super.onNestedPreFling(target, velocityX, velocityY);
  }
}
