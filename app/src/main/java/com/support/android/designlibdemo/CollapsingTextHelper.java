package com.support.android.designlibdemo;

import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.text.TextDirectionHeuristicsCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Interpolator;

/**
 * Created by eneim on 9/30/15.
 */
final class CollapsingTextHelper {
  private static final boolean USE_SCALING_TEXTURE;
  private static final boolean DEBUG_DRAW = false;
  private static final Paint DEBUG_DRAW_PAINT;

  static {
    USE_SCALING_TEXTURE = Build.VERSION.SDK_INT < 18;
    DEBUG_DRAW_PAINT = null;
    if (DEBUG_DRAW_PAINT != null) {
      DEBUG_DRAW_PAINT.setAntiAlias(true);
      DEBUG_DRAW_PAINT.setColor(0xff00ff);
    }

  }

  private final View mView;
  private final Rect mExpandedBounds;
  private final Rect mCollapsedBounds;
  private final RectF mCurrentBounds;
  private final TextPaint mTextPaint;
  private boolean mDrawTitle;
  private float mExpandedFraction;
  private int mExpandedTextGravity = 16;
  private int mCollapsedTextGravity = 16;
  private float mExpandedTextSize = 15.0F;
  private float mCollapsedTextSize = 15.0F;
  private int mExpandedTextColor;
  private int mCollapsedTextColor;
  private float mExpandedDrawY;
  private float mCollapsedDrawY;
  private float mExpandedDrawX;
  private float mCollapsedDrawX;
  private float mCurrentDrawX;
  private float mCurrentDrawY;
  private CharSequence mText;
  private CharSequence mTextToDraw;
  private boolean mIsRtl;
  private boolean mUseTexture;
  private Bitmap mExpandedTitleTexture;
  private Paint mTexturePaint;
  private float mTextureAscent;
  private float mTextureDescent;
  private float mScale;
  private float mCurrentTextSize;
  private boolean mBoundsChanged;
  private Interpolator mPositionInterpolator;
  private Interpolator mTextSizeInterpolator;

  public CollapsingTextHelper(View view) {
    this.mView = view;
    this.mTextPaint = new TextPaint();
    this.mTextPaint.setAntiAlias(true);
    this.mCollapsedBounds = new Rect();
    this.mExpandedBounds = new Rect();
    this.mCurrentBounds = new RectF();
  }

  void setTextSizeInterpolator(Interpolator interpolator) {
    this.mTextSizeInterpolator = interpolator;
    this.recalculate();
  }

  public void recalculate() {
    if (this.mView.getHeight() > 0 && this.mView.getWidth() > 0) {
      this.calculateBaseOffsets();
      this.calculateCurrentOffsets();
    }

  }

  private void calculateBaseOffsets() {
    this.mTextPaint.setTextSize(this.mCollapsedTextSize);
    float width = this.mTextToDraw != null ? this.mTextPaint.measureText(this.mTextToDraw, 0,
        this.mTextToDraw.length()) : 0.0F;
    int collapsedAbsGravity = GravityCompat.getAbsoluteGravity(this.mCollapsedTextGravity, this
        .mIsRtl ? 1 : 0);
    float textHeight;
    switch (collapsedAbsGravity & 112) {
      case 16:
      default:
        float expandedAbsGravity = this.mTextPaint.descent() - this.mTextPaint.ascent();
        textHeight = expandedAbsGravity / 2.0F - this.mTextPaint.descent();
        this.mCollapsedDrawY = (float) this.mCollapsedBounds.centerY() + textHeight;
        break;
      case 48:
        this.mCollapsedDrawY = (float) this.mCollapsedBounds.top - this.mTextPaint.ascent();
        break;
      case 80:
        this.mCollapsedDrawY = (float) this.mCollapsedBounds.bottom;
    }

    switch (collapsedAbsGravity & 7) {
      case 1:
        this.mCollapsedDrawX = (float) this.mCollapsedBounds.centerX() - width / 2.0F;
        break;
      case 2:
      case 3:
      case 4:
      default:
        this.mCollapsedDrawX = (float) this.mCollapsedBounds.left;
        break;
      case 5:
        this.mCollapsedDrawX = (float) this.mCollapsedBounds.right - width;
    }

    this.mTextPaint.setTextSize(this.mExpandedTextSize);
    width = this.mTextToDraw != null ? this.mTextPaint.measureText(this.mTextToDraw, 0, this
        .mTextToDraw.length()) : 0.0F;
    int expandedAbsGravity1 = GravityCompat.getAbsoluteGravity(this.mExpandedTextGravity, this
        .mIsRtl ? 1 : 0);
    switch (expandedAbsGravity1 & 112) {
      case 16:
      default:
        textHeight = this.mTextPaint.descent() - this.mTextPaint.ascent();
        float textOffset = textHeight / 2.0F - this.mTextPaint.descent();
        this.mExpandedDrawY = (float) this.mExpandedBounds.centerY() + textOffset;
        break;
      case 48:
        this.mExpandedDrawY = (float) this.mExpandedBounds.top - this.mTextPaint.ascent();
        break;
      case 80:
        this.mExpandedDrawY = (float) this.mExpandedBounds.bottom;
    }

    switch (expandedAbsGravity1 & 7) {
      case 1:
        this.mExpandedDrawX = (float) this.mExpandedBounds.centerX() - width / 2.0F;
        break;
      case 2:
      case 3:
      case 4:
      default:
        this.mExpandedDrawX = (float) this.mExpandedBounds.left;
        break;
      case 5:
        this.mExpandedDrawX = (float) this.mExpandedBounds.right - width;
    }

    this.clearTexture();
  }

  private void calculateCurrentOffsets() {
    float fraction = this.mExpandedFraction;
    this.interpolateBounds(fraction);
    this.mCurrentDrawX = lerp(this.mExpandedDrawX, this.mCollapsedDrawX, fraction, this
        .mPositionInterpolator);
    this.mCurrentDrawY = lerp(this.mExpandedDrawY, this.mCollapsedDrawY, fraction, this
        .mPositionInterpolator);
    this.setInterpolatedTextSize(lerp(this.mExpandedTextSize, this.mCollapsedTextSize, fraction,
        this.mTextSizeInterpolator));
    if (this.mCollapsedTextColor != this.mExpandedTextColor) {
      this.mTextPaint.setColor(blendColors(this.mExpandedTextColor, this.mCollapsedTextColor,
          fraction));
    } else {
      this.mTextPaint.setColor(this.mCollapsedTextColor);
    }

    ViewCompat.postInvalidateOnAnimation(this.mView);
  }

  private void clearTexture() {
    if (this.mExpandedTitleTexture != null) {
      this.mExpandedTitleTexture.recycle();
      this.mExpandedTitleTexture = null;
    }

  }

  private void interpolateBounds(float fraction) {
    this.mCurrentBounds.left = lerp((float) this.mExpandedBounds.left, (float) this
        .mCollapsedBounds.left, fraction, this.mPositionInterpolator);
    this.mCurrentBounds.top = lerp(this.mExpandedDrawY, this.mCollapsedDrawY, fraction, this
        .mPositionInterpolator);
    this.mCurrentBounds.right = lerp((float) this.mExpandedBounds.right, (float) this
        .mCollapsedBounds.right, fraction, this.mPositionInterpolator);
    this.mCurrentBounds.bottom = lerp((float) this.mExpandedBounds.bottom, (float) this
        .mCollapsedBounds.bottom, fraction, this.mPositionInterpolator);
  }

  private static float lerp(float startValue, float endValue, float fraction, Interpolator
      interpolator) {
    if (interpolator != null) {
      fraction = interpolator.getInterpolation(fraction);
    }

    return AnimationUtils.lerp(startValue, endValue, fraction);
  }

  private void setInterpolatedTextSize(float textSize) {
    if (this.mText != null) {
      boolean updateDrawText = false;
      float availableWidth;
      float newTextSize;
      if (isClose(textSize, this.mCollapsedTextSize)) {
        availableWidth = (float) this.mCollapsedBounds.width();
        newTextSize = this.mCollapsedTextSize;
        this.mScale = 1.0F;
      } else {
        availableWidth = (float) this.mExpandedBounds.width();
        newTextSize = this.mExpandedTextSize;
        if (isClose(textSize, this.mExpandedTextSize)) {
          this.mScale = 1.0F;
        } else {
          this.mScale = textSize / this.mExpandedTextSize;
        }
      }

      if (availableWidth > 0.0F) {
        updateDrawText = this.mCurrentTextSize != newTextSize || this.mBoundsChanged;
        this.mCurrentTextSize = newTextSize;
        this.mBoundsChanged = false;
      }

      if (this.mTextToDraw == null || updateDrawText) {
        this.mTextPaint.setTextSize(this.mCurrentTextSize);
        CharSequence title = TextUtils.ellipsize(this.mText, this.mTextPaint, availableWidth,
            TextUtils.TruncateAt.END);
        if (this.mTextToDraw == null || !this.mTextToDraw.equals(title)) {
          this.mTextToDraw = title;
        }

        this.mIsRtl = this.calculateIsRtl(this.mTextToDraw);
      }

      this.mUseTexture = USE_SCALING_TEXTURE && this.mScale != 1.0F;
      if (this.mUseTexture) {
        this.ensureExpandedTexture();
      }

      ViewCompat.postInvalidateOnAnimation(this.mView);
    }
  }

  private static int blendColors(int color1, int color2, float ratio) {
    float inverseRatio = 1.0F - ratio;
    float a = (float) Color.alpha(color1) * inverseRatio + (float) Color.alpha(color2) * ratio;
    float r = (float) Color.red(color1) * inverseRatio + (float) Color.red(color2) * ratio;
    float g = (float) Color.green(color1) * inverseRatio + (float) Color.green(color2) * ratio;
    float b = (float) Color.blue(color1) * inverseRatio + (float) Color.blue(color2) * ratio;
    return Color.argb((int) a, (int) r, (int) g, (int) b);
  }

  private static boolean isClose(float value, float targetValue) {
    return Math.abs(value - targetValue) < 0.001F;
  }

  private boolean calculateIsRtl(CharSequence text) {
    boolean defaultIsRtl = ViewCompat.getLayoutDirection(this.mView) == ViewCompat
        .LAYOUT_DIRECTION_RTL;
    return (defaultIsRtl ? TextDirectionHeuristicsCompat.FIRSTSTRONG_RTL :
        TextDirectionHeuristicsCompat.FIRSTSTRONG_LTR).isRtl(text, 0, text.length());
  }

  private void ensureExpandedTexture() {
    if (this.mExpandedTitleTexture == null && !this.mExpandedBounds.isEmpty() &&
        !TextUtils.isEmpty(this.mTextToDraw)) {
      this.mTextPaint.setTextSize(this.mExpandedTextSize);
      this.mTextPaint.setColor(this.mExpandedTextColor);
      this.mTextureAscent = this.mTextPaint.ascent();
      this.mTextureDescent = this.mTextPaint.descent();
      int w = Math.round(this.mTextPaint.measureText(this.mTextToDraw, 0,
          this.mTextToDraw.length()));
      int h = Math.round(this.mTextureDescent - this.mTextureAscent);
      if (w > 0 || h > 0) {
        this.mExpandedTitleTexture = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(this.mExpandedTitleTexture);
        c.drawText(this.mTextToDraw, 0, this.mTextToDraw.length(), 0.0F,
            (float) h - this.mTextPaint.descent(), this.mTextPaint);
        if (this.mTexturePaint == null) {
          this.mTexturePaint = new Paint(3);
        }
      }
    }
  }

  void setPositionInterpolator(Interpolator interpolator) {
    this.mPositionInterpolator = interpolator;
    this.recalculate();
  }

  void setExpandedBounds(int left, int top, int right, int bottom) {
    if (!rectEquals(this.mExpandedBounds, left, top, right, bottom)) {
      this.mExpandedBounds.set(left, top, right, bottom);
      this.mBoundsChanged = true;
      this.onBoundsChanged();
    }

  }

  private static boolean rectEquals(Rect r, int left, int top, int right, int bottom) {
    return r.left == left && r.top == top && r.right == right && r.bottom == bottom;
  }

  void onBoundsChanged() {
    this.mDrawTitle = this.mCollapsedBounds.width() > 0 && this.mCollapsedBounds.height() > 0 &&
        this.mExpandedBounds.width() > 0 && this.mExpandedBounds.height() > 0;
  }

  void setCollapsedBounds(int left, int top, int right, int bottom) {
    if (!rectEquals(this.mCollapsedBounds, left, top, right, bottom)) {
      this.mCollapsedBounds.set(left, top, right, bottom);
      this.mBoundsChanged = true;
      this.onBoundsChanged();
    }

  }

  int getExpandedTextGravity() {
    return this.mExpandedTextGravity;
  }

  void setExpandedTextGravity(int gravity) {
    if (this.mExpandedTextGravity != gravity) {
      this.mExpandedTextGravity = gravity;
      this.recalculate();
    }

  }

  int getCollapsedTextGravity() {
    return this.mCollapsedTextGravity;
  }

  void setCollapsedTextGravity(int gravity) {
    if (this.mCollapsedTextGravity != gravity) {
      this.mCollapsedTextGravity = gravity;
      this.recalculate();
    }

  }

  void setCollapsedTextAppearance(int resId) {
    TypedArray a = this.mView.getContext().obtainStyledAttributes(resId, android.support.design.R
        .styleable.TextAppearance);
    if (a.hasValue(android.support.design.R.styleable.TextAppearance_android_textColor)) {
      this.mCollapsedTextColor = a.getColor(android.support.design.R.styleable
          .TextAppearance_android_textColor, this.mCollapsedTextColor);
    }

    if (a.hasValue(android.support.design.R.styleable.TextAppearance_android_textSize)) {
      this.mCollapsedTextSize = (float) a.getDimensionPixelSize(android.support.design.R
          .styleable.TextAppearance_android_textSize, (int) this.mCollapsedTextSize);
    }

    a.recycle();
    this.recalculate();
  }

  void setExpandedTextAppearance(int resId) {
    TypedArray a = this.mView.getContext().obtainStyledAttributes(resId, android.support.design.R
        .styleable.TextAppearance);
    if (a.hasValue(android.support.design.R.styleable.TextAppearance_android_textColor)) {
      this.mExpandedTextColor = a.getColor(android.support.design.R.styleable
          .TextAppearance_android_textColor, this.mExpandedTextColor);
    }

    if (a.hasValue(android.support.design.R.styleable.TextAppearance_android_textSize)) {
      this.mExpandedTextSize = (float) a.getDimensionPixelSize(android.support.design.R.styleable
          .TextAppearance_android_textSize, (int) this.mExpandedTextSize);
    }

    a.recycle();
    this.recalculate();
  }

  Typeface getTypeface() {
    return this.mTextPaint.getTypeface();
  }

  void setTypeface(Typeface typeface) {
    if (typeface == null) {
      typeface = Typeface.DEFAULT;
    }

    if (this.mTextPaint.getTypeface() != typeface) {
      this.mTextPaint.setTypeface(typeface);
      this.recalculate();
    }

  }

  float getExpansionFraction() {
    return this.mExpandedFraction;
  }

  void setExpansionFraction(float fraction) {
    fraction = MathUtils.constrain(fraction, 0.0F, 1.0F);
    if (fraction != this.mExpandedFraction) {
      this.mExpandedFraction = fraction;
      this.calculateCurrentOffsets();
    }

  }

  float getCollapsedTextSize() {
    return this.mCollapsedTextSize;
  }

  void setCollapsedTextSize(float textSize) {
    if (this.mCollapsedTextSize != textSize) {
      this.mCollapsedTextSize = textSize;
      this.recalculate();
    }
  }

  float getExpandedTextSize() {
    return this.mExpandedTextSize;
  }

  void setExpandedTextSize(float textSize) {
    if (this.mExpandedTextSize != textSize) {
      this.mExpandedTextSize = textSize;
      this.recalculate();
    }

  }

  public void draw(Canvas canvas) {
    int saveCount = canvas.save();
    if (this.mTextToDraw != null && this.mDrawTitle) {
      float x = this.mCurrentDrawX;
      float y = this.mCurrentDrawY;
      boolean drawTexture = this.mUseTexture && this.mExpandedTitleTexture != null;
      this.mTextPaint.setTextSize(this.mCurrentTextSize);
      float ascent;
      if (drawTexture) {
        ascent = this.mTextureAscent * this.mScale;
        float var10000 = this.mTextureDescent * this.mScale;
      } else {
        ascent = this.mTextPaint.ascent() * this.mScale;
        float descent = this.mTextPaint.descent() * this.mScale;
      }

      if (drawTexture) {
        y += ascent;
      }

      if (this.mScale != 1.0F) {
        canvas.scale(this.mScale, this.mScale, x, y);
      }

      if (drawTexture) {
        canvas.drawBitmap(this.mExpandedTitleTexture, x, y, this.mTexturePaint);
      } else {
        canvas.drawText(this.mTextToDraw, 0, this.mTextToDraw.length(), x, y, this.mTextPaint);
      }
    }

    canvas.restoreToCount(saveCount);
  }

  CharSequence getText() {
    return this.mText;
  }

  void setText(CharSequence text) {
    if (text == null || !text.equals(this.mText)) {
      this.mText = text;
      this.mTextToDraw = null;
      this.clearTexture();
      this.recalculate();
    }

  }

  int getExpandedTextColor() {
    return this.mExpandedTextColor;
  }

  void setExpandedTextColor(int textColor) {
    if (this.mExpandedTextColor != textColor) {
      this.mExpandedTextColor = textColor;
      this.recalculate();
    }

  }

  int getCollapsedTextColor() {
    return this.mCollapsedTextColor;
  }

  void setCollapsedTextColor(int textColor) {
    if (this.mCollapsedTextColor != textColor) {
      this.mCollapsedTextColor = textColor;
      this.recalculate();
    }

  }
}
