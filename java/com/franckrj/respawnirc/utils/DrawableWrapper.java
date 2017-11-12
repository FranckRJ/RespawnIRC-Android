package com.franckrj.respawnirc.utils;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;

/* Classe copiée de https://android.googlesource.com/platform/frameworks/support/+/master/v7/appcompat/src/android/support/v7/graphics/drawable/DrawableWrapper.java
 * mais sans le @RestrictTo(LIBRARY_GROUP) pour pouvoir l'utiliser quand même.
 * Classe à possiblement supprimer si le @RestrictTo venait à être enlevé.*/

public class DrawableWrapper extends Drawable implements Drawable.Callback {
    private Drawable mDrawable;

    public DrawableWrapper(Drawable drawable) {
        setWrappedDrawable(drawable);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        mDrawable.draw(canvas);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        mDrawable.setBounds(bounds);
    }

    @Override
    public void setChangingConfigurations(int configs) {
        mDrawable.setChangingConfigurations(configs);
    }

    @Override
    public int getChangingConfigurations() {
        return mDrawable.getChangingConfigurations();
    }

    @Override
    public void setDither(boolean dither) {
        mDrawable.setDither(dither);
    }

    @Override
    public void setFilterBitmap(boolean filter) {
        mDrawable.setFilterBitmap(filter);
    }

    @Override
    public void setAlpha(int alpha) {
        mDrawable.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mDrawable.setColorFilter(cf);
    }

    @Override
    public boolean isStateful() {
        return mDrawable.isStateful();
    }

    @Override
    public boolean setState(@NonNull final int[] stateSet) {
        return mDrawable.setState(stateSet);
    }

    @NonNull
    @Override
    public int[] getState() {
        return mDrawable.getState();
    }

    @Override
    public void jumpToCurrentState() {
        mDrawable.jumpToCurrentState();
    }

    @NonNull
    @Override
    public Drawable getCurrent() {
        return mDrawable.getCurrent();
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        return super.setVisible(visible, restart) || mDrawable.setVisible(visible, restart);
    }

    @Override
    public int getOpacity() {
        return mDrawable.getOpacity();
    }

    @Override
    public Region getTransparentRegion() {
        return mDrawable.getTransparentRegion();
    }

    @Override
    public int getIntrinsicWidth() {
        return mDrawable.getIntrinsicWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return mDrawable.getIntrinsicHeight();
    }

    @Override
    public int getMinimumWidth() {
        return mDrawable.getMinimumWidth();
    }

    @Override
    public int getMinimumHeight() {
        return mDrawable.getMinimumHeight();
    }

    @Override
    public boolean getPadding(@NonNull Rect padding) {
        return mDrawable.getPadding(padding);
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable who) {
        invalidateSelf();
    }

    @Override
    public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
        scheduleSelf(what, when);
    }

    @Override
    public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
        unscheduleSelf(what);
    }

    @Override
    protected boolean onLevelChange(int level) {
        return mDrawable.setLevel(level);
    }

    @Override
    public void setAutoMirrored(boolean mirrored) {
        DrawableCompat.setAutoMirrored(mDrawable, mirrored);
    }

    @Override
    public boolean isAutoMirrored() {
        return DrawableCompat.isAutoMirrored(mDrawable);
    }

    @Override
    public void setTint(int tint) {
        DrawableCompat.setTint(mDrawable, tint);
    }

    @Override
    public void setTintList(ColorStateList tint) {
        DrawableCompat.setTintList(mDrawable, tint);
    }

    @Override
    public void setTintMode(@NonNull PorterDuff.Mode tintMode) {
        DrawableCompat.setTintMode(mDrawable, tintMode);
    }

    @Override
    public void setHotspot(float x, float y) {
        DrawableCompat.setHotspot(mDrawable, x, y);
    }

    @Override
    public void setHotspotBounds(int left, int top, int right, int bottom) {
        DrawableCompat.setHotspotBounds(mDrawable, left, top, right, bottom);
    }

    public void setWrappedDrawable(Drawable drawable) {
        if (mDrawable != null) {
            mDrawable.setCallback(null);
        }

        mDrawable = drawable;

        if (drawable != null) {
            drawable.setCallback(this);
        }
    }
}
