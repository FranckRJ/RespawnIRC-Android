package com.franckrj.respawnirc.jvcforumlist;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.utils.ThemeManager;
import com.franckrj.respawnirc.utils.JVCParser;

import java.util.ArrayList;

public class JVCForumListAdapter extends BaseAdapter {
    private static final String SAVE_LIST_OF_SEARCHED_FORUM_SHOWED = "flaSaveListOfSearchedForumShowed";

    private ArrayList<JVCParser.NameAndLink> currentListOfForums = new ArrayList<>();
    private LayoutInflater serviceInflater;
    private Activity parentActivity = null;

    public JVCForumListAdapter(Activity newParentActivity) {
        parentActivity = newParentActivity;
        serviceInflater = (LayoutInflater) parentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setNewListOfForums(ArrayList<JVCParser.NameAndLink> newListOfForums) {
        if (newListOfForums == null) {
            currentListOfForums = new ArrayList<>();
        } else {
            currentListOfForums = newListOfForums;
        }

        notifyDataSetChanged();
    }

    public String getForumLinkAtThisPos(int position) {
        if (position >= 0 && position < currentListOfForums.size()) {
            return currentListOfForums.get(position).link;
        } else {
            return "";
        }
    }

    public void saveToBundle(Bundle savedInstanceState) {
        savedInstanceState.putParcelableArrayList(SAVE_LIST_OF_SEARCHED_FORUM_SHOWED, currentListOfForums);
    }

    public void loadFromBundle(Bundle savedInstanceState) {
        currentListOfForums = savedInstanceState.getParcelableArrayList(SAVE_LIST_OF_SEARCHED_FORUM_SHOWED);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return currentListOfForums.size();
    }

    @Override
    public Object getItem(int i) {
        return currentListOfForums.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = serviceInflater.inflate(R.layout.jvcforums_row, parent, false);
            holder.forumTitle = convertView.findViewById(R.id.title_forum_jvcforums_text_row);
            holder.forumImage = convertView.findViewById(R.id.image_forum_jvcforums_text_row);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        convertView.setBackgroundColor(ThemeManager.getColorInt(R.attr.themedDefaultBackgroundColor, parentActivity));
        holder.forumTitle.setText(currentListOfForums.get(position).name);

        return convertView;
    }

    private class ViewHolder {
        public TextView forumTitle;
        public ImageView forumImage;
    }
}
