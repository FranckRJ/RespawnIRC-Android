package com.pijon.respawnirc;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;

/*TODO: Désactiver l'highlight quand on clique sur un élément. (désactivé par défaut quand un lien est présent) (fixé depuis l'ajout des boutons ?)*/
class JVCMessagesAdapter extends BaseAdapter {
    private ArrayList<JVCParser.MessageInfos> listOfMessages = new ArrayList<>();
    private LayoutInflater serviceInflater;
    private Activity parentActivity = null;
    private int currentItemIDSelected = -1;
    private PopupMenu.OnMenuItemClickListener actionWhenItemMenuClicked = null;
    private String currentPseudoOfUser = "";

    private View.OnClickListener menuButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View buttonView) {
            PopupMenu popup = new PopupMenu(parentActivity, buttonView);
            MenuInflater inflater = popup.getMenuInflater();
            JVCParser.MessageInfos itemSelected;

            currentItemIDSelected = (int) buttonView.getTag();
            itemSelected = getItem(currentItemIDSelected);
            popup.setOnMenuItemClickListener(actionWhenItemMenuClicked);

            if (itemSelected.pseudo.toLowerCase().equals(currentPseudoOfUser.toLowerCase())) {
                inflater.inflate(R.menu.menu_message_user, popup.getMenu());
            } else {
                inflater.inflate(R.menu.menu_message_others, popup.getMenu());
            }

            if (itemSelected.containSpoil) {
                if (itemSelected.showSpoil) {
                    inflater.inflate(R.menu.menu_message_hide_spoil, popup.getMenu());
                } else {
                    inflater.inflate(R.menu.menu_message_show_spoil, popup.getMenu());
                }
            }

            popup.show();
        }
    };

    private class ViewHolder {
        TextView firstLine;
        TextView secondLine;
        Button showMenuButton;
    }

    JVCMessagesAdapter(Activity newParentActivity) {
        parentActivity = newParentActivity;
        serviceInflater = (LayoutInflater) parentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    int getCurrentItemIDSelected() {
        return currentItemIDSelected;
    }

    void setActionWhenItemMenuClicked(PopupMenu.OnMenuItemClickListener newAction) {
        actionWhenItemMenuClicked = newAction;
    }

    void setCurrentPseudoOfUser(String newPseudoOfUser) {
        currentPseudoOfUser = newPseudoOfUser;
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
            holder.showMenuButton = (Button) convertView.findViewById(R.id.menu_overflow_row);

            holder.showMenuButton.setOnClickListener(menuButtonClicked);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.showMenuButton.setTag(position);
        holder.firstLine.setText(Html.fromHtml(JVCParser.createMessageFirstLineFromInfos(getItem(position), currentPseudoOfUser)));
        holder.secondLine.setText(Html.fromHtml(JVCParser.createMessageSecondLineFromInfos(getItem(position))));
        return convertView;
    }

}