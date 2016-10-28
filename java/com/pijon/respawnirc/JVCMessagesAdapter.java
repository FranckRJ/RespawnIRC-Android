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

/*TODO: Désactiver l'highlight quand on clique sur un élément. (désactivé par défaut quand un lien est présent)*/
class JVCMessagesAdapter extends BaseAdapter {
    private ArrayList<JVCParser.MessageInfos> listOfMessages = new ArrayList<>();
    private LayoutInflater serviceInflater;

    class ViewHolder {
        TextView firstLine;
        TextView secondLine;
    }

    JVCMessagesAdapter(Activity parentActivity) {
        serviceInflater = (LayoutInflater) parentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    void removeAllItems() {
        listOfMessages.clear();
    }

    void removeFirstItem() {
        listOfMessages.remove(0);
    }

    void addItem(final JVCParser.MessageInfos item) {
        listOfMessages.add(item);
    }

    void updateAllItems() {
        notifyDataSetChanged();
    }

    ArrayList<JVCParser.MessageInfos> getAllItems() {
        return listOfMessages;
    }

    @Override
    public int getCount() {
        return listOfMessages.size();
    }

    @Override
    public JVCParser.MessageInfos getItem(int position) {
        return listOfMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = serviceInflater.inflate(R.layout.jvcmessages_row, null);
            holder.firstLine = (TextView) convertView.findViewById(R.id.item_one_jvcmessages_text_row);
            holder.secondLine = (TextView) convertView.findViewById(R.id.item_two_jvcmessages_text_row);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.firstLine.setText(Html.fromHtml(JVCParser.createMessageFirstLineFromInfos(getItem(position))));
        holder.secondLine.setText(Html.fromHtml(JVCParser.createMessageSecondLineFromInfos(getItem(position))));
        return convertView;
    }

}