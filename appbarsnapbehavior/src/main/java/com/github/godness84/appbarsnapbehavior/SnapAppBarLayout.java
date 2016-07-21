package com.github.godness84.appbarsnapbehavior;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;

@CoordinatorLayout.DefaultBehavior(AppBarSnapBehavior.class)
public class SnapAppBarLayout extends AppBarLayout
{
    public SnapAppBarLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    public void setExpanded(boolean expanded)
    {
        CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) getLayoutParams()).getBehavior();
        if (behavior instanceof AppBarSnapBehavior)
            ((AppBarSnapBehavior) behavior).setExpanded(expanded);
        else
            super.setExpanded(expanded);
    }
}