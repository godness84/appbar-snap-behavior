package com.github.godness84.appbarsnapbehavior;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.List;

public class ScrollingViewBehavior extends CoordinatorLayout.Behavior<View> {
    private static final String TAG = "ScrollingViewBehavior";

    private AppBarLayout mAppBarLayout = null;
    private Integer mCurrentOffset = null;
    private int mOriginalTop = 0;

    private WeakReference<View> mChildRef;

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
        final List<View> dependencies = parent.getDependencies(child);
        for (int i = 0, z = dependencies.size(); i < z; i++) {
            // Safe, since we declared that we depends on any AppBarLayout
            mAppBarLayout = (AppBarLayout) dependencies.get(i);
            break;
        }

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
        } else {
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
        mChildRef = new WeakReference<View>(child);

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
        if (dy > 0 && child.getTop() > mOriginalTop - mAppBarLayout.getTotalScrollRange()) {
            consumed[1] = scroll(child, dy);
        }
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, View child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        if (dyUnconsumed < 0) {
            scroll(child, dyUnconsumed);
        }
    }

    @Override
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, View child, View target) {
    }

    /**
     * @return true, if the view which this behavior is attached to can move its top position up.
     */
    public boolean canScrollUp() {
        if (mChildRef != null) {
            View child = mChildRef.get();
            if (child != null && child.getTop() > mOriginalTop - mAppBarLayout.getTotalScrollRange())
                return true;
        }

        return false;
    }

    /**
     * Offset the position of the view accoring to its original position.
     *
     * @param view      View instance
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

        if (newTop <= mOriginalTop - mAppBarLayout.getTotalScrollRange()) {
            newTop = mOriginalTop - mAppBarLayout.getTotalScrollRange();
        }

        return setTopBottomOffset(view, newTop - mOriginalTop);
    }
}