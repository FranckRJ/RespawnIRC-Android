package com.pijon.respawnirc;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

//http://stackoverflow.com/questions/16427360/add-menu-on-every-listview-item

class JVCMessagesAdapter extends BaseAdapter {
    private ArrayList<String> listOfMessages = new ArrayList<>();
    private LayoutInflater serviceInflater;

    JVCMessagesAdapter(Activity parentActivity) {
        serviceInflater = (LayoutInflater) parentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    void removeAllItems() {
        listOfMessages.clear();
    }

    void removeFirstItem() {
        listOfMessages.remove(0);
    }

    void addItem(final String item) {
        listOfMessages.add(item);
    }

    void updateAllItems() {
        notifyDataSetChanged();
    }

    ArrayList<String> getAllItems() {
        return listOfMessages;
    }

    @Override
    public int getCount() {
        return listOfMessages.size();
    }

    @Override
    public String getItem(int position) {
        return listOfMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView holder;
        if (convertView == null) {
            convertView = serviceInflater.inflate(R.layout.jvcmessages_row, null);
            holder = (TextView)convertView.findViewById(R.id.item_jvcmessages_text_row);
            convertView.setTag(holder);
        } else {
            holder = (TextView) convertView.getTag();
        }
        holder.setText(Html.fromHtml(getItem(position)));
        return convertView;
    }

}