package com.franckrj.respawnirc.jvctopictools;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.utils.JVCParser;

import java.util.ArrayList;

public class JVCTopicsAdapter extends BaseAdapter {
    private ArrayList<JVCParser.TopicInfos> listOfTopics = new ArrayList<>();
    private LayoutInflater serviceInflater;
    private Activity parentActivity = null;
    private boolean alternateBackgroundColor = false;

    public JVCTopicsAdapter(Activity newParentActivity) {
        parentActivity = newParentActivity;
        serviceInflater = (LayoutInflater) parentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public ArrayList<JVCParser.TopicInfos> getAllItems() {
        return listOfTopics;
    }

    public void setAlternateBackgroundColor(boolean newVal) {
        alternateBackgroundColor = newVal;
    }

    public void removeAllItems() {
        listOfTopics.clear();
    }

    public void addItem(JVCParser.TopicInfos item) {
        listOfTopics.add(item);
    }

    public void updateAllItems() {
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return listOfTopics.size();
    }

    @Override
    public JVCParser.TopicInfos getItem(int position) {
        return listOfTopics.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        JVCParser.TopicInfos currentTopic = listOfTopics.get(position);
        if (convertView == null) {
            holder = new ViewHolder();

            convertView = serviceInflater.inflate(R.layout.jvctopics_row, parent, false);
            holder.firstLine = (TextView) convertView.findViewById(R.id.item_one_jvctopics_text_row);
            holder.secondLine = (TextView) convertView.findViewById(R.id.item_two_jvctopics_text_row);
            holder.background = convertView.getBackground();

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.firstLine.setText(currentTopic.topicName);
        holder.secondLine.setText(currentTopic.pseudo + " | " + currentTopic.nbMessagesPosted + " | " + currentTopic.wholeDate);

        if (position % 2 == 0 || !alternateBackgroundColor) {
            convertView.setBackgroundDrawable(holder.background);
        } else {
            convertView.setBackgroundColor(parentActivity.getResources().getColor(R.color.altBackgroundMessageColor));
        }

        return convertView;
    }

    private class ViewHolder {
        private TextView firstLine;
        private TextView secondLine;
        private Drawable background;
    }
}