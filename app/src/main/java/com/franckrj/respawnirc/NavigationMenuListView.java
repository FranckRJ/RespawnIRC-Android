package com.franckrj.respawnirc;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

public class NavigationMenuListView extends ListView {
    private int listViewPadding = 0;

    @SuppressWarnings("unused")
    @TargetApi(21)
    public NavigationMenuListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initializeNavigationMenuList(context);
    }

    public NavigationMenuListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeNavigationMenuList(context);
    }

    public NavigationMenuListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeNavigationMenuList(context);
    }

    public NavigationMenuListView(Context context) {
        super(context);
        initializeNavigationMenuList(context);
    }

    public void setHeaderView(View headerView) {
        View spacingView = new View(headerView.getContext());
        spacingView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, listViewPadding));
        addHeaderView(headerView, null, false);
        addHeaderView(spacingView, null, false);
    }

    private void initializeNavigationMenuList(Context context) {
        listViewPadding = context.getResources().getDimensionPixelSize(R.dimen.paddingOfNavigationMenuListView);

        setOverScrollMode(View.OVER_SCROLL_NEVER);
        setClipToPadding(false);
        setDrawSelectorOnTop(true);
        setDivider(null);
        setDividerHeight(0);
        setVerticalScrollBarEnabled(true);
        setScrollBarStyle(SCROLLBARS_OUTSIDE_OVERLAY);
        setPadding(0, 0, 0, listViewPadding);
    }
}
