package com.github.godness84.appbarsnapbehavior;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

public class AppBarSnapBehavior extends CoordinatorLayout.Behavior<AppBarLayout> {
    public AppBarSnapBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private Integer mCurrentTop = null;
    private int mOriginalTop = 0;
    private int mLastDyConsumed = 0;
    private ValueAnimator mAnimator;

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, AppBarLayout abl, int layoutDirection) {
        parent.onLayoutChild(abl, layoutDirection);

        mOriginalTop = abl.getTop();

        if (mCurrentTop != null)
            updateTopBottomOffset(abl, mCurrentTop);
        else
            mCurrentTop = mOriginalTop;

        return true;
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child, View directTargetChild, View target, int nestedScrollAxes) {
        mLastDyConsumed = 0;

        if (mAnimator != null)
            mAnimator.cancel();

        return true;
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, int dx, int dy, int[] consumed) {
        if (dy < 0)
            return;

        View parent = getParentViewWithBehavior(coordinatorLayout, target);
        ScrollingViewBehavior behavior = (ScrollingViewBehavior) ((CoordinatorLayout.LayoutParams) parent.getLayoutParams()).getBehavior();
        if (!behavior.canScrollUp())
            return;

        int offset = scroll(child, dy);

        if (offset != 0) {
            mLastDyConsumed = offset;
        }

        consumed[1] = -offset;
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        int offset = scroll(child, dyConsumed + dyUnconsumed);

        if (offset != 0) {
            mLastDyConsumed = offset;
        }
    }

    @Override
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target) {
        if (mLastDyConsumed > 0) {
            animateTopPositionTo(child, mOriginalTop);
        } else if (mLastDyConsumed < 0) {
            View parent = getParentViewWithBehavior(coordinatorLayout, target);

            // If the scroll container is not enough up, the appbar must scroll down to its original position,
            // otherwise a blank space would be revealed.
            if (parent != null && parent.getTop() > mOriginalTop + child.getHeight() - child.getTotalScrollRange()) {
                animateTopPositionTo(child, mOriginalTop);
            } else {
                animateTopPositionTo(child, mOriginalTop - child.getTotalScrollRange());
            }
        }
    }

    @Override
    public boolean onNestedFling(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, float velocityX, float velocityY, boolean consumed) {
        return false;
    }

    @Override
    public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, float velocityX, float velocityY) {
        return false;
    }

    private void animateTopPositionTo(final AppBarLayout abl, int targetTop) {
        if (mAnimator != null) {
            mAnimator.cancel();
            mAnimator = null;
        }

        mAnimator = ObjectAnimator.ofInt(abl.getTop(), targetTop);
        mAnimator.setDuration(200);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int top = ((Integer) animation.getAnimatedValue()).intValue();
                updateTopBottomOffset(abl, top);
            }
        });

        mAnimator.start();
    }

    private View getParentViewWithBehavior(CoordinatorLayout coordinatorLayout, View target) {
        while (target != null && target != coordinatorLayout) {
            if (target.getLayoutParams() instanceof CoordinatorLayout.LayoutParams) {
                final CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) target.getLayoutParams()).getBehavior();
                if (behavior instanceof ScrollingViewBehavior) {
                    return target;
                }
            }

            target = (View) target.getParent();
        }

        return null;
    }

    /**
     * Update the top position of the AppBarLayout to the new top value.
     *
     * @param abl       AppBarLayout instance
     * @param targetTop the new top value to which the AppBarLayout should be set.
     * @return the offset from the current top to the target top.
     */
    private int updateTopBottomOffset(AppBarLayout abl, int targetTop) {
        final int offset = targetTop - abl.getTop();
        abl.offsetTopAndBottom(offset);
        mCurrentTop = targetTop;

        return offset;
    }

    /**
     * Scroll the top position of the AppBarLayout
     *
     * @param abl AppBarLayout instance
     * @param dy  how much the AppBarLayout should scroll
     * @return the actual offset from the current top position.
     */
    private int scroll(AppBarLayout abl, int dy) {
        int newTop = abl.getTop() - dy;

        if (newTop >= mOriginalTop) {
            newTop = mOriginalTop;
        }

        if (newTop <= mOriginalTop - abl.getTotalScrollRange()) {
            newTop = mOriginalTop - abl.getTotalScrollRange();
        }

        return updateTopBottomOffset(abl, newTop);
    }
}
