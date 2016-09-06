package com.github.godness84.appbarsnapbehavior;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

import java.lang.ref.WeakReference;

public class ScrollingViewBehavior extends CoordinatorLayout.Behavior<View> {
    private AppBarLayout mAppBarLayout = null;
    private Integer mCurrentOffset = null;
    private int mOriginalTop = 0;

    private WeakReference<View> mChildRef;

    public ScrollingViewBehavior() {

    }

    public ScrollingViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        // We depend on any AppBarLayouts
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
        // Let the coordinator layout the view. Then we'll adjust its position.
        parent.onLayoutChild(child, layoutDirection);

        // Find the first AppBarLayout instance
        mAppBarLayout = (AppBarLayout) parent.getDependencies(child).get(0);

        // The original top position is below the AppBarLayouts when it's in its original position.
        mOriginalTop = child.getTop() + mAppBarLayout.getHeight();

        // Resize the view in order to show the most bottom edge when the view is scrolled at the topmost position.
        int actualHeight = child.getMeasuredHeight() - mAppBarLayout.getHeight() + mAppBarLayout.getTotalScrollRange();
        int widthSpec = View.MeasureSpec.makeMeasureSpec(child.getMeasuredWidth(), View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(actualHeight, View.MeasureSpec.EXACTLY);
        child.measure(widthSpec, heightSpec);
        child.layout(child.getLeft(), child.getTop(), child.getRight(), child.getTop() + actualHeight);

        // Set the correct top position
        if (mCurrentOffset != null) {
            setTopBottomOffset(child, mCurrentOffset);
        }
        else {
            setTopBottomOffset(child, 0);
        }

        // Ensure that the new top position is correct for the current layout.
        if (child.getTop() >= mOriginalTop) {
            setTopBottomOffset(child, 0);
        }
        if (child.getTop() <= mOriginalTop - mAppBarLayout.getTotalScrollRange()) {
            setTopBottomOffset(child, -mAppBarLayout.getTotalScrollRange());
        }

        // Hold a reference to the view.
        mChildRef = new WeakReference<>(child);

        return true;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        return false;
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, View child, View directTargetChild, View target, int nestedScrollAxes) {
        return true;
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, View child, View target, int dx, int dy, int[] consumed) {
        if ((dy > 0 && child.getTop() > mOriginalTop - mAppBarLayout.getTotalScrollRange()) ||
                (dy < 0 && child.getTop() < mOriginalTop)) {
            consumed[1] = scroll(child, dy);
        }
    }

    /**
     * Offset the position of the view accoring to its original position.
     *
     * @param view         View instance
     * @param targetOffset the offset from its original position
     * @return the offset from the current top to the target top.
     */
    private int setTopBottomOffset(View view, int targetOffset) {
        final int targetTop = mOriginalTop + targetOffset;
        final int offset = targetTop - view.getTop();
        view.offsetTopAndBottom(offset);
        mCurrentOffset = targetOffset;

        return offset;
    }

    /**
     * Scroll the top position of the View
     *
     * @param view View instance
     * @param dy   how much the View should scroll
     * @return the actual offset from the current top position.
     */
    private int scroll(View view, int dy) {
        int newTop = view.getTop() - dy;

        if (newTop >= mOriginalTop) {
            newTop = mOriginalTop;
        }

        if (newTop < mOriginalTop - mAppBarLayout.getTotalScrollRange()) {
            newTop = mOriginalTop - mAppBarLayout.getTotalScrollRange();
        }

        return setTopBottomOffset(view, newTop - mOriginalTop);
    }

    public void adjustLayout() {
        if (mChildRef != null) {
            View child = mChildRef.get();
            if (child.getTop() < mAppBarLayout.getBottom()) {
                setTopBottomOffset(child, 0);
            }
            else {
                if (child.getTop() != mAppBarLayout.getBottom()) {
                    setTopBottomOffset(child, -mAppBarLayout.getTotalScrollRange());
                }
            }
        }
    }
}