package com.support.android.designlibdemo;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.support.v4.widget.ScrollerCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by eneim on 9/29/15.
 */
@CoordinatorLayout.DefaultBehavior(AppBarLayout.Behavior.class)
public class AppBarLayout extends LinearLayout {

  private static final int PENDING_ACTION_NONE = 0;
  private static final int PENDING_ACTION_EXPANDED = 1;
  private static final int PENDING_ACTION_COLLAPSED = 2;
  private static final int PENDING_ACTION_ANIMATE_ENABLED = 4;
  private static final int INVALID_SCROLL_RANGE = -1;
  private final List<AppBarLayout.OnOffsetChangedListener> mListeners;
  boolean mHaveChildWithInterpolator;
  private int mTotalScrollRange;
  private int mDownPreScrollRange;
  private int mDownScrollRange;
  private float mTargetElevation;
  private int mPendingAction;
  private WindowInsetsCompat mLastInsets;

  public AppBarLayout(Context context) {
    this(context, (AttributeSet) null);
  }

  public AppBarLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.mTotalScrollRange = -1;
    this.mDownPreScrollRange = -1;
    this.mDownScrollRange = -1;
    this.mPendingAction = 0;
    this.setOrientation(VERTICAL);
    TypedArray a = context.obtainStyledAttributes(attrs, android.support.design.R.styleable
        .AppBarLayout, 0, android.support.design.R.style.Widget_Design_AppBarLayout);
    this.mTargetElevation = (float) a.getDimensionPixelSize(android.support.design.R.styleable
        .AppBarLayout_elevation, 0);
    this.setBackgroundDrawable(a.getDrawable(android.support.design.R.styleable
        .AppBarLayout_android_background));
    if (a.hasValue(android.support.design.R.styleable.AppBarLayout_expanded)) {
      this.setExpanded(a.getBoolean(android.support.design.R.styleable.AppBarLayout_expanded,
          false));
    }

    a.recycle();
    ViewUtils.setBoundsViewOutlineProvider(this);
    this.mListeners = new ArrayList<>();
    ViewCompat.setElevation(this, this.mTargetElevation);
    ViewCompat.setOnApplyWindowInsetsListener(this,
        new android.support.v4.view.OnApplyWindowInsetsListener() {
          @Override
          public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
            AppBarLayout.this.setWindowInsets(insets);
            return insets.consumeSystemWindowInsets();
          }
        });
  }

  public void setExpanded(boolean expanded) {
    this.setExpanded(expanded, ViewCompat.isLaidOut(this));
  }

  private void setWindowInsets(WindowInsetsCompat insets) {
    this.mTotalScrollRange = -1;
    this.mLastInsets = insets;
    int i = 0;

    for (int z = this.getChildCount(); i < z; ++i) {
      View child = this.getChildAt(i);
      insets = ViewCompat.dispatchApplyWindowInsets(child, insets);
      if (insets.isConsumed()) {
        break;
      }
    }

  }

  public void setExpanded(boolean expanded, boolean animate) {
    this.mPendingAction = (expanded ? 1 : 2) | (animate ? 4 : 0);
    this.requestLayout();
  }

  public void addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener listener) {
    if (listener != null && !this.mListeners.contains(listener)) {
      this.mListeners.add(listener);
    }

  }

  public void removeOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener listener) {
    if (listener != null) {
      this.mListeners.remove(listener);
    }

  }

  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    super.onLayout(changed, l, t, r, b);
    this.mTotalScrollRange = -1;
    this.mDownPreScrollRange = -1;
    this.mDownPreScrollRange = -1;
    this.mHaveChildWithInterpolator = false;
    int i = 0;

    for (int z = this.getChildCount(); i < z; ++i) {
      View child = this.getChildAt(i);
      AppBarLayout.LayoutParams childLp = (AppBarLayout.LayoutParams) child.getLayoutParams();
      Interpolator interpolator = childLp.getScrollInterpolator();
      if (interpolator != null) {
        this.mHaveChildWithInterpolator = true;
        break;
      }
    }

  }

  public void setOrientation(int orientation) {
    if (orientation != VERTICAL) {
      throw new IllegalArgumentException("AppBarLayout is always vertical and does not support " +
          "horizontal orientation");
    } else {
      super.setOrientation(orientation);
    }
  }

  public AppBarLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
    return new AppBarLayout.LayoutParams(this.getContext(), attrs);
  }

  protected AppBarLayout.LayoutParams generateDefaultLayoutParams() {
    return new AppBarLayout.LayoutParams(-1, -2);
  }

  protected AppBarLayout.LayoutParams generateLayoutParams(
      android.view.ViewGroup.LayoutParams p) {
    return p instanceof android.widget.LinearLayout.LayoutParams ? new AppBarLayout.LayoutParams(
        (android.widget.LinearLayout.LayoutParams) p) : (p instanceof MarginLayoutParams ? new
        AppBarLayout.LayoutParams((MarginLayoutParams) p) : new AppBarLayout.LayoutParams(p));
  }

  protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
    return p instanceof AppBarLayout.LayoutParams;
  }

  final boolean hasChildWithInterpolator() {
    return this.mHaveChildWithInterpolator;
  }

  final boolean hasScrollableChildren() {
    return this.getTotalScrollRange() != 0;
  }

  public final int getTotalScrollRange() {
    if (this.mTotalScrollRange != -1) {
      return this.mTotalScrollRange;
    } else {
      int range = 0;
      int top = 0;

      for (int z = this.getChildCount(); top < z; ++top) {
        View child = this.getChildAt(top);
        AppBarLayout.LayoutParams lp = (AppBarLayout.LayoutParams) child.getLayoutParams();
        int childHeight = ViewCompat.isLaidOut(child) ? child.getHeight() : child
            .getMeasuredHeight();
        int flags = lp.mScrollFlags;
        if ((flags & 1) == 0) {
          break;
        }

        range += childHeight + lp.topMargin + lp.bottomMargin;
        if ((flags & 2) != 0) {
          range -= ViewCompat.getMinimumHeight(child);
          break;
        }
      }

      top = this.mLastInsets != null ? this.mLastInsets.getSystemWindowInsetTop() : 0;
      return this.mTotalScrollRange = range - top;
    }
  }

  final int getUpNestedPreScrollRange() {
    return this.getTotalScrollRange();
  }

  final int getDownNestedPreScrollRange() {
    if (this.mDownPreScrollRange != -1) {
      return this.mDownPreScrollRange;
    } else {
      int range = 0;

      for (int i = this.getChildCount() - 1; i >= 0; --i) {
        View child = this.getChildAt(i);
        AppBarLayout.LayoutParams lp = (AppBarLayout.LayoutParams) child.getLayoutParams();
        int childHeight = ViewCompat.isLaidOut(child) ? child.getHeight() : child
            .getMeasuredHeight();
        int flags = lp.mScrollFlags;
        if ((flags & 5) == 5) {
          range += lp.topMargin + lp.bottomMargin;
          if ((flags & 8) != 0) {
            range += ViewCompat.getMinimumHeight(child);
          } else {
            range += childHeight;
          }
        } else if (range > 0) {
          break;
        }
      }

      return this.mDownPreScrollRange = range;
    }
  }

  final int getDownNestedScrollRange() {
    if (this.mDownScrollRange != -1) {
      return this.mDownScrollRange;
    } else {
      int range = 0;
      int i = 0;

      for (int z = this.getChildCount(); i < z; ++i) {
        View child = this.getChildAt(i);
        AppBarLayout.LayoutParams lp = (AppBarLayout.LayoutParams) child.getLayoutParams();
        int childHeight = ViewCompat.isLaidOut(child) ? child.getHeight() : child
            .getMeasuredHeight();
        childHeight += lp.topMargin + lp.bottomMargin;
        int flags = lp.mScrollFlags;
        if ((flags & 1) == 0) {
          break;
        }

        range += childHeight;
        if ((flags & 2) != 0) {
          return range - ViewCompat.getMinimumHeight(child);
        }
      }

      return this.mDownScrollRange = range;
    }
  }

  final int getMinimumHeightForVisibleOverlappingContent() {
    int topInset = this.mLastInsets != null ? this.mLastInsets.getSystemWindowInsetTop() : 0;
    int minHeight = ViewCompat.getMinimumHeight(this);
    if (minHeight != 0) {
      return minHeight * 2 + topInset;
    } else {
      int childCount = this.getChildCount();
      return childCount >= 1 ? ViewCompat.getMinimumHeight(this.getChildAt(childCount - 1)) * 2 +
          topInset : 0;
    }
  }

  public float getTargetElevation() {
    return this.mTargetElevation;
  }

  public void setTargetElevation(float elevation) {
    this.mTargetElevation = elevation;
  }

  int getPendingAction() {
    return this.mPendingAction;
  }

  void resetPendingAction() {
    this.mPendingAction = 0;
  }

  public interface OnOffsetChangedListener {
    void onOffsetChanged(AppBarLayout var1, int var2);
  }

  public static class LayoutParams extends android.widget.LinearLayout.LayoutParams {
    public static final int SCROLL_FLAG_SCROLL = 1;
    public static final int SCROLL_FLAG_EXIT_UNTIL_COLLAPSED = 2;
    public static final int SCROLL_FLAG_ENTER_ALWAYS = 4;
    public static final int SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED = 8;
    static final int FLAG_QUICK_RETURN = 5;
    int mScrollFlags = 1;
    Interpolator mScrollInterpolator;

    public LayoutParams(Context c, AttributeSet attrs) {
      super(c, attrs);
      TypedArray a = c.obtainStyledAttributes(attrs, android.support.design.R.styleable
          .AppBarLayout_LayoutParams);
      this.mScrollFlags = a.getInt(android.support.design.R.styleable
          .AppBarLayout_LayoutParams_layout_scrollFlags, 0);
      if (a.hasValue(android.support.design.R.styleable
          .AppBarLayout_LayoutParams_layout_scrollInterpolator)) {
        int resId = a.getResourceId(android.support.design.R.styleable
            .AppBarLayout_LayoutParams_layout_scrollInterpolator, 0);
        this.mScrollInterpolator = android.view.animation.AnimationUtils.loadInterpolator(c, resId);
      }

      a.recycle();
    }

    public LayoutParams(int width, int height) {
      super(width, height);
    }

    public LayoutParams(int width, int height, float weight) {
      super(width, height, weight);
    }

    public LayoutParams(android.view.ViewGroup.LayoutParams p) {
      super(p);
    }

    public LayoutParams(MarginLayoutParams source) {
      super(source);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT) public LayoutParams(android.widget.LinearLayout.LayoutParams source) {
      super(source);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT) public LayoutParams(AppBarLayout.LayoutParams source) {
      super(source);
      this.mScrollFlags = source.mScrollFlags;
      this.mScrollInterpolator = source.mScrollInterpolator;
    }

    public int getScrollFlags() {
      return this.mScrollFlags;
    }

    public void setScrollFlags(int flags) {
      this.mScrollFlags = flags;
    }

    public Interpolator getScrollInterpolator() {
      return this.mScrollInterpolator;
    }

    public void setScrollInterpolator(Interpolator interpolator) {
      this.mScrollInterpolator = interpolator;
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ScrollFlags {
    }
  }

  public static class Behavior extends ViewOffsetBehavior<AppBarLayout> {

    private static final int INVALID_POINTER = -1;
    private static final int INVALID_POSITION = -1;
    private int mOffsetDelta;
    private boolean mSkipNestedPreScroll;
    private Runnable mFlingRunnable;
    private ScrollerCompat mScroller;
    private ValueAnimatorCompat mAnimator;
    private int mOffsetToChildIndexOnLayout = -1;
    private boolean mOffsetToChildIndexOnLayoutIsMinHeight;
    private float mOffsetToChildIndexOnLayoutPerc;
    private boolean mIsBeingDragged;
    private int mActivePointerId = -1;
    private int mLastMotionY;
    private int mTouchSlop = -1;
    private WeakReference<View> mLastNestedScrollingChildRef;

    public Behavior() {
    }

    public Behavior(Context context, AttributeSet attrs) {
      super(context, attrs);
    }

    public boolean onInterceptTouchEvent(CoordinatorLayout parent, AppBarLayout child, MotionEvent
        ev) {
      if (this.mTouchSlop < 0) {
        this.mTouchSlop = ViewConfiguration.get(parent.getContext()).getScaledTouchSlop();
      }

      int action = ev.getAction();
      if (action == 2 && this.mIsBeingDragged) {
        return true;
      } else {
        int x;
        int y;
        switch (MotionEventCompat.getActionMasked(ev)) {
          case 0:
            this.mIsBeingDragged = false;
            x = (int) ev.getX();
            y = (int) ev.getY();
            if (parent.isPointInChildBounds(child, x, y) && this.canDragAppBarLayout()) {
              this.mLastMotionY = y;
              this.mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
            }
            break;
          case 1:
          case 3:
            this.mIsBeingDragged = false;
            this.mActivePointerId = -1;
            break;
          case 2:
            x = this.mActivePointerId;
            if (x != -1) {
              y = MotionEventCompat.findPointerIndex(ev, x);
              if (y != -1) {
                int y1 = (int) MotionEventCompat.getY(ev, y);
                int yDiff = Math.abs(y1 - this.mLastMotionY);
                if (yDiff > this.mTouchSlop) {
                  this.mIsBeingDragged = true;
                  this.mLastMotionY = y1;
                }
              }
            }
        }

        return this.mIsBeingDragged;
      }
    }

    public boolean onTouchEvent(CoordinatorLayout parent, AppBarLayout child, MotionEvent ev) {
      if (this.mTouchSlop < 0) {
        this.mTouchSlop = ViewConfiguration.get(parent.getContext()).getScaledTouchSlop();
      }

      int x = (int) ev.getX();
      int y = (int) ev.getY();
      switch (MotionEventCompat.getActionMasked(ev)) {
        case 0:
          if (parent.isPointInChildBounds(child, x, y) && this.canDragAppBarLayout()) {
            this.mLastMotionY = y;
            this.mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
            break;
          }

          return false;
        case 1:
        case 3:
          this.mIsBeingDragged = false;
          this.mActivePointerId = -1;
          break;
        case 2:
          int activePointerIndex = MotionEventCompat.findPointerIndex(ev, this.mActivePointerId);
          if (activePointerIndex == -1) {
            return false;
          }

          y = (int) MotionEventCompat.getY(ev, activePointerIndex);
          int dy = this.mLastMotionY - y;
          if (!this.mIsBeingDragged && Math.abs(dy) > this.mTouchSlop) {
            this.mIsBeingDragged = true;
            if (dy > 0) {
              dy -= this.mTouchSlop;
            } else {
              dy += this.mTouchSlop;
            }
          }

          if (this.mIsBeingDragged) {
            this.mLastMotionY = y;
            this.scroll(parent, child, dy, -child.getDownNestedScrollRange(), 0);
          }
      }

      return true;
    }

    private int scroll(CoordinatorLayout coordinatorLayout, AppBarLayout appBarLayout, int dy, int
        minOffset, int maxOffset) {
      return this.setAppBarTopBottomOffset(coordinatorLayout, appBarLayout, this
          .getTopBottomOffsetForScrollingSibling() - dy, minOffset, maxOffset);
    }    public boolean onStartNestedScroll(CoordinatorLayout parent, AppBarLayout child, View
        directTargetChild, View target, int nestedScrollAxes) {
      boolean started = (nestedScrollAxes & 2) != 0 && child.hasScrollableChildren() && parent
          .getHeight() - directTargetChild.getHeight() <= child.getHeight();
      if (started && this.mAnimator != null) {
        this.mAnimator.cancel();
      }

      this.mLastNestedScrollingChildRef = null;
      return started;
    }

    final int setAppBarTopBottomOffset(CoordinatorLayout coordinatorLayout, AppBarLayout
        appBarLayout, int newOffset, int minOffset, int maxOffset) {
      int curOffset = this.getTopBottomOffsetForScrollingSibling();
      int consumed = 0;
      if (minOffset != 0 && curOffset >= minOffset && curOffset <= maxOffset) {
        newOffset = MathUtils.constrain(newOffset, minOffset, maxOffset);
        if (curOffset != newOffset) {
          int interpolatedOffset = appBarLayout.hasChildWithInterpolator() ? this.interpolateOffset
              (appBarLayout, newOffset) : newOffset;
          boolean offsetChanged = this.setTopAndBottomOffset(interpolatedOffset);
          consumed = curOffset - newOffset;
          this.mOffsetDelta = newOffset - interpolatedOffset;
          if (!offsetChanged && appBarLayout.hasChildWithInterpolator()) {
            coordinatorLayout.dispatchDependentViewsChanged(appBarLayout);
          }

          this.dispatchOffsetUpdates(appBarLayout);
        }
      }

      return consumed;
    }    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child, View
        target) {
      this.mSkipNestedPreScroll = false;
      this.mLastNestedScrollingChildRef = new WeakReference<>(target);
    }

    final int getTopBottomOffsetForScrollingSibling() {
      return this.getTopAndBottomOffset() + this.mOffsetDelta;
    }    public void onNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child, View
        target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
      if (dyUnconsumed < 0) {
        this.scroll(coordinatorLayout, child, dyUnconsumed, -child.getDownNestedScrollRange(), 0);
        this.mSkipNestedPreScroll = true;
      } else {
        this.mSkipNestedPreScroll = false;
      }

    }

    private int interpolateOffset(AppBarLayout layout, int offset) {
      int absOffset = Math.abs(offset);
      int i = 0;

      for (int z = layout.getChildCount(); i < z; ++i) {
        View child = layout.getChildAt(i);
        AppBarLayout.LayoutParams childLp = (AppBarLayout.LayoutParams) child.getLayoutParams();
        Interpolator interpolator = childLp.getScrollInterpolator();
        if (absOffset >= child.getTop() && absOffset <= child.getBottom()) {
          if (interpolator != null) {
            int childScrollableHeight = 0;
            int flags = childLp.getScrollFlags();
            if ((flags & 1) != 0) {
              childScrollableHeight += child.getHeight() + childLp.topMargin + childLp.bottomMargin;
              if ((flags & 2) != 0) {
                childScrollableHeight -= ViewCompat.getMinimumHeight(child);
              }
            }

            if (childScrollableHeight > 0) {
              int offsetForView = absOffset - child.getTop();
              int interpolatedDiff = Math.round((float) childScrollableHeight * interpolator
                  .getInterpolation((float) offsetForView / (float) childScrollableHeight));
              return Integer.signum(offset) * (child.getTop() + interpolatedDiff);
            }
          }
          break;
        }
      }

      return offset;
    }    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child, View
        target, int dx, int dy, int[] consumed) {
      if (dy != 0 && !this.mSkipNestedPreScroll) {
        int min;
        int max;
        if (dy < 0) {
          min = -child.getTotalScrollRange();
          max = min + child.getDownNestedPreScrollRange();
        } else {
          min = -child.getUpNestedPreScrollRange();
          max = 0;
        }

        consumed[1] = this.scroll(coordinatorLayout, child, dy, min, max);
      }

    }

    private void dispatchOffsetUpdates(AppBarLayout layout) {
      List listeners = layout.mListeners;
      int i = 0;

      for (int z = listeners.size(); i < z; ++i) {
        AppBarLayout.OnOffsetChangedListener listener = (AppBarLayout.OnOffsetChangedListener)
            listeners.get(i);
        if (listener != null) {
          listener.onOffsetChanged(layout, this.getTopAndBottomOffset());
        }
      }

    }    public boolean onNestedFling(CoordinatorLayout coordinatorLayout, AppBarLayout child, View
        target, float velocityX, float velocityY, boolean consumed) {
      if (!consumed) {
        return this.fling(coordinatorLayout, child, -child.getTotalScrollRange(), 0, -velocityY);
      } else {
        int targetScroll;
        if (velocityY < 0.0F) {
          targetScroll = -child.getTotalScrollRange() + child.getDownNestedPreScrollRange();
          if (this.getTopBottomOffsetForScrollingSibling() > targetScroll) {
            return false;
          }
        } else {
          targetScroll = -child.getUpNestedPreScrollRange();
          if (this.getTopBottomOffsetForScrollingSibling() < targetScroll) {
            return false;
          }
        }

        if (this.getTopBottomOffsetForScrollingSibling() != targetScroll) {
          this.animateOffsetTo(coordinatorLayout, child, targetScroll);
          return true;
        } else {
          return false;
        }
      }
    }

    private boolean canDragAppBarLayout() {
      if (this.mLastNestedScrollingChildRef == null) {
        return false;
      } else {
        View view = (View) this.mLastNestedScrollingChildRef.get();
        return view != null && view.isShown() && !ViewCompat.canScrollVertically(view, -1);
      }
    }    private boolean fling(CoordinatorLayout coordinatorLayout, AppBarLayout layout, int minOffset,
                          int maxOffset, float velocityY) {
      if (this.mFlingRunnable != null) {
        layout.removeCallbacks(this.mFlingRunnable);
      }

      if (this.mScroller == null) {
        this.mScroller = ScrollerCompat.create(layout.getContext());
      }

      this.mScroller.fling(0, this.getTopBottomOffsetForScrollingSibling(), 0, Math.round
          (velocityY), 0, 0, minOffset, maxOffset);
      if (this.mScroller.computeScrollOffset()) {
        this.mFlingRunnable = new Behavior.FlingRunnable(coordinatorLayout, layout);
        ViewCompat.postOnAnimation(layout, this.mFlingRunnable);
        return true;
      } else {
        this.mFlingRunnable = null;
        return false;
      }
    }

    public boolean onLayoutChild(CoordinatorLayout parent, AppBarLayout abl, int
        layoutDirection) {
      boolean handled = super.onLayoutChild(parent, abl, layoutDirection);
      int pendingAction = abl.getPendingAction();
      int offset;
      if (pendingAction != 0) {
        boolean child = (pendingAction & 4) != 0;
        if ((pendingAction & 2) != 0) {
          offset = -abl.getUpNestedPreScrollRange();
          if (child) {
            this.animateOffsetTo(parent, abl, offset);
          } else {
            this.setAppBarTopBottomOffset(parent, abl, offset);
          }
        } else if ((pendingAction & 1) != 0) {
          if (child) {
            this.animateOffsetTo(parent, abl, 0);
          } else {
            this.setAppBarTopBottomOffset(parent, abl, 0);
          }
        }

        abl.resetPendingAction();
      } else if (this.mOffsetToChildIndexOnLayout >= 0) {
        View child1 = abl.getChildAt(this.mOffsetToChildIndexOnLayout);
        offset = -child1.getBottom();
        if (this.mOffsetToChildIndexOnLayoutIsMinHeight) {
          offset += ViewCompat.getMinimumHeight(child1);
        } else {
          offset += Math.round((float) child1.getHeight() * this.mOffsetToChildIndexOnLayoutPerc);
        }

        this.setTopAndBottomOffset(offset);
        this.mOffsetToChildIndexOnLayout = -1;
      }

      this.dispatchOffsetUpdates(abl);
      return handled;
    }    private void animateOffsetTo(final CoordinatorLayout coordinatorLayout, final AppBarLayout
        child, int offset) {
      if (this.mAnimator == null) {
        this.mAnimator = ViewUtils.createAnimator();
        this.mAnimator.setInterpolator(AnimationUtils.DECELERATE_INTERPOLATOR);
        this.mAnimator.setUpdateListener(new ValueAnimatorCompat.AnimatorUpdateListener() {
          public void onAnimationUpdate(ValueAnimatorCompat animator) {
            Behavior.this.setAppBarTopBottomOffset(coordinatorLayout, child, animator
                .getAnimatedIntValue());
          }
        });
      } else {
        this.mAnimator.cancel();
      }

      this.mAnimator.setIntValues(this.getTopBottomOffsetForScrollingSibling(), offset);
      this.mAnimator.start();
    }

    protected static class SavedState extends View.BaseSavedState {

      public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
        @Override public SavedState createFromParcel(Parcel source) {
          return new SavedState(source);
        }

        @Override public SavedState[] newArray(int size) {
          return new SavedState[size];
        }
      };

      int firstVisibleChildIndex;
      float firstVisibileChildPercentageShown;
      boolean firstVisibileChildAtMinimumHeight;

      public SavedState(Parcel source) {
        super(source);
        this.firstVisibleChildIndex = source.readInt();
        this.firstVisibileChildPercentageShown = source.readFloat();
        this.firstVisibileChildAtMinimumHeight = source.readByte() != 0;
      }

      public SavedState(Parcelable superState) {
        super(superState);
      }

      public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.firstVisibleChildIndex);
        dest.writeFloat(this.firstVisibileChildPercentageShown);
        dest.writeByte((byte) (this.firstVisibileChildAtMinimumHeight ? 1 : 0));
      }
    }    final int setAppBarTopBottomOffset(CoordinatorLayout coordinatorLayout, AppBarLayout
        appBarLayout, int newOffset) {
      return this.setAppBarTopBottomOffset(coordinatorLayout, appBarLayout, newOffset, -2147483648,
          2147483647);
    }

    private class FlingRunnable implements Runnable {
      private final CoordinatorLayout mParent;
      private final AppBarLayout mLayout;

      FlingRunnable(CoordinatorLayout parent, AppBarLayout layout) {
        this.mParent = parent;
        this.mLayout = layout;
      }

      public void run() {
        if (this.mLayout != null && Behavior.this.mScroller != null
            && Behavior.this.mScroller.computeScrollOffset()) {
          Behavior.this.setAppBarTopBottomOffset(this.mParent, this.mLayout,
              Behavior.this.mScroller.getCurrY());
          ViewCompat.postOnAnimation(this.mLayout, this);
        }

      }
    }    public void onRestoreInstanceState(CoordinatorLayout parent, AppBarLayout appBarLayout,
                                       Parcelable state) {
      if (state instanceof Behavior.SavedState) {
        Behavior.SavedState ss = (Behavior.SavedState) state;
        super.onRestoreInstanceState(parent, appBarLayout, ss.getSuperState());
        this.mOffsetToChildIndexOnLayout = ss.firstVisibleChildIndex;
        this.mOffsetToChildIndexOnLayoutPerc = ss.firstVisibileChildPercentageShown;
        this.mOffsetToChildIndexOnLayoutIsMinHeight = ss.firstVisibileChildAtMinimumHeight;
      } else {
        super.onRestoreInstanceState(parent, appBarLayout, state);
        this.mOffsetToChildIndexOnLayout = -1;
      }

    }

    public Parcelable onSaveInstanceState(CoordinatorLayout parent, AppBarLayout appBarLayout) {
      Parcelable superState = super.onSaveInstanceState(parent, appBarLayout);
      int offset = this.getTopAndBottomOffset();
      int i = 0;

      for (int count = appBarLayout.getChildCount(); i < count; ++i) {
        View child = appBarLayout.getChildAt(i);
        int visBottom = child.getBottom() + offset;
        if (child.getTop() + offset <= 0 && visBottom >= 0) {
          Behavior.SavedState ss = new Behavior.SavedState(superState);
          ss.firstVisibleChildIndex = i;
          ss.firstVisibileChildAtMinimumHeight = visBottom == ViewCompat.getMinimumHeight(child);
          ss.firstVisibileChildPercentageShown = (float) visBottom / (float) child.getHeight();
          return ss;
        }
      }

      return superState;
    }
  }

  public static class ScrollingViewBehavior extends ViewOffsetBehavior<View> {
    private int mOverlayTop;

    public ScrollingViewBehavior() {
    }

    public ScrollingViewBehavior(Context context, AttributeSet attrs) {
      super(context, attrs);
      TypedArray a = context.obtainStyledAttributes(attrs, android.support.design.R.styleable
          .ScrollingViewBehavior_Params);
      this.mOverlayTop = a.getDimensionPixelSize(android.support.design.R.styleable
          .ScrollingViewBehavior_Params_behavior_overlapTop, 0);
      a.recycle();
    }

    public int getOverlayTop() {
      return this.mOverlayTop;
    }    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
      return dependency instanceof AppBarLayout;
    }

    public void setOverlayTop(int overlayTop) {
      this.mOverlayTop = overlayTop;
    }    public boolean onMeasureChild(CoordinatorLayout parent, View child, int
        parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
      int childLpHeight = child.getLayoutParams().height;
      if (childLpHeight == -1 || childLpHeight == -2) {
        List dependencies = parent.getDependencies(child);
        if (dependencies.isEmpty()) {
          return false;
        }

        AppBarLayout appBar = findFirstAppBarLayout(dependencies);
        if (appBar != null && ViewCompat.isLaidOut(appBar)) {
          if (ViewCompat.getFitsSystemWindows(appBar)) {
            ViewCompat.setFitsSystemWindows(child, true);
          }

          int availableHeight = MeasureSpec.getSize(parentHeightMeasureSpec);
          if (availableHeight == 0) {
            availableHeight = parent.getHeight();
          }

          int height = availableHeight - appBar.getMeasuredHeight() + appBar.getTotalScrollRange();
          int heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, childLpHeight == -1 ?
              MeasureSpec.EXACTLY : MeasureSpec.AT_MOST);
          parent.onMeasureChild(child, parentWidthMeasureSpec, widthUsed, heightMeasureSpec,
              heightUsed);
          return true;
        }
      }

      return false;
    }

    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
      android.support.design.widget.CoordinatorLayout.Behavior behavior = ((android.support
          .design.widget.CoordinatorLayout.LayoutParams) dependency.getLayoutParams())
          .getBehavior();
      if (behavior instanceof Behavior) {
        int appBarOffset = ((Behavior) behavior).getTopBottomOffsetForScrollingSibling();
        int expandedMax = dependency.getHeight() - this.mOverlayTop;
        int collapsedMin = parent.getHeight() - child.getHeight();
        if (this.mOverlayTop != 0 && dependency instanceof AppBarLayout) {
          int scrollRange = ((AppBarLayout) dependency).getTotalScrollRange();
          this.setTopAndBottomOffset(AnimationUtils.lerp(expandedMax, collapsedMin, (float) Math
              .abs(appBarOffset) / (float) scrollRange));
        } else {
          this.setTopAndBottomOffset(dependency.getHeight() - this.mOverlayTop + appBarOffset);
        }
      }

      return false;
    }

    private static AppBarLayout findFirstAppBarLayout(List<View> views) {
      int i = 0;

      for (int z = views.size(); i < z; ++i) {
        View view = views.get(i);
        if (view instanceof AppBarLayout) {
          return (AppBarLayout) view;
        }
      }

      return null;
    }
  }
}
