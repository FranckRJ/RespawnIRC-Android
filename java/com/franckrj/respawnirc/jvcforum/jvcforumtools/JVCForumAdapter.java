package com.franckrj.respawnirc.jvcforum.jvcforumtools;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spanned;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.utils.ThemeManager;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.Undeprecator;
import com.franckrj.respawnirc.utils.Utils;

import java.util.ArrayList;

public class JVCForumAdapter extends BaseAdapter {
    private ArrayList<JVCParser.TopicInfos> listOfTopics = new ArrayList<>();
    private ArrayList<ContentHolder> listOfContentForTopics = new ArrayList<>();
    private LayoutInflater serviceInflater = null;
    private Activity parentActivity = null;
    private boolean alternateBackgroundColor = false;
    private int topicTitleSizeInSp = 14;
    private int topicInfosSizeInSp = 14;
    private Drawable iconMarqueOn = null;
    private Drawable iconMarqueOff = null;
    private Drawable iconDossier2 = null;
    private Drawable iconLock = null;
    private Drawable iconResolu = null;
    private Drawable iconGhost = null;
    private Drawable iconDossier1 = null;

    public JVCForumAdapter(Activity newParentActivity) {
        parentActivity = newParentActivity;
        serviceInflater = (LayoutInflater) parentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        iconMarqueOn = Undeprecator.resourcesGetDrawable(parentActivity.getResources(), R.drawable.icon_topic_marque_on);
        iconMarqueOff = Undeprecator.resourcesGetDrawable(parentActivity.getResources(), R.drawable.icon_topic_marque_off);
        iconDossier2 = Undeprecator.resourcesGetDrawable(parentActivity.getResources(), R.drawable.icon_topic_dossier2);
        iconLock = ThemeManager.getDrawable(R.attr.themedTopicLockIcon, parentActivity);
        iconResolu = Undeprecator.resourcesGetDrawable(parentActivity.getResources(), R.drawable.icon_topic_resolu);
        iconGhost = Undeprecator.resourcesGetDrawable(parentActivity.getResources(), R.drawable.icon_topic_ghost);
        iconDossier1 = Undeprecator.resourcesGetDrawable(parentActivity.getResources(), R.drawable.icon_topic_dossier1);
    }

    public ArrayList<JVCParser.TopicInfos> getAllItems() {
        return listOfTopics;
    }

    public boolean getAlternateBackgroundColor() {
        return alternateBackgroundColor;
    }

    public int getTopicTitleSizeInSp() {
        return topicTitleSizeInSp;
    }

    public int getTopicInfosSizeInSp() {
        return topicInfosSizeInSp;
    }

    public void setAlternateBackgroundColor(boolean newVal) {
        alternateBackgroundColor = newVal;
    }

    public void setTopicTitleSizeInSp(int newVal) {
        topicTitleSizeInSp = newVal;
    }

    public void setTopicInfosSizeInSp(int newVal) {
        topicInfosSizeInSp = newVal;
    }

    public void removeAllItems() {
        listOfTopics.clear();
        listOfContentForTopics.clear();
    }

    public void addItem(JVCParser.TopicInfos item) {
        listOfTopics.add(item);
        listOfContentForTopics.add(createHolderForItem(item));
    }

    public void recreateAllItems() {
        listOfContentForTopics.clear();

        for (JVCParser.TopicInfos thisItem : listOfTopics) {
            listOfContentForTopics.add(createHolderForItem(thisItem));
        }
    }

    private ContentHolder createHolderForItem(JVCParser.TopicInfos item) {
        String textForAuthor;
        ContentHolder thisHolder = new ContentHolder();

        thisHolder.titleLineContent = Undeprecator.htmlFromHtml("<b><font color=\"" + Utils.colorToString(ThemeManager.getColorInt(R.attr.themedTopicNameColor, parentActivity)) +
                                                                "\">" + item.htmlName + "</font> (" + item.nbOfMessages + ")</b>");

        switch (item.authorType) {
            case "modo":
                textForAuthor = "<small><font color=\"" + Utils.colorToString(ThemeManager.getColorInt(R.attr.themedPseudoModoColor, parentActivity)) + "\">" + item.author + "</font></small>";
                break;
            case "admin":
            case "staff":
                textForAuthor = "<small><font color=\"" + Utils.colorToString(ThemeManager.getColorInt(R.attr.themedPseudoAdminColor, parentActivity)) + "\">" + item.author + "</font></small>";
                break;
            default:
                textForAuthor = "<small>" + item.author + "</small>";
                break;
        }

        thisHolder.authorLineContent = Undeprecator.htmlFromHtml(textForAuthor);
        thisHolder.dateLineContent = Undeprecator.htmlFromHtml("<small>" + item.wholeDate + "</small>");

        return thisHolder;
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
        ContentHolder currentTopicContent = listOfContentForTopics.get(position);
        JVCParser.TopicInfos currentTopicInfos = listOfTopics.get(position);

        if (convertView == null) {
            holder = new ViewHolder();

            convertView = serviceInflater.inflate(R.layout.jvctopics_row, parent, false);
            holder.titleLine = convertView.findViewById(R.id.title_text_jvctopics_row);
            holder.authorLine = convertView.findViewById(R.id.author_text_jvctopics_row);
            holder.dateLine = convertView.findViewById(R.id.date_text_jvctopics_row);
            holder.topicIcon = convertView.findViewById(R.id.topic_icon_jvctopics_row);

            holder.titleLine.setTextSize(TypedValue.COMPLEX_UNIT_SP, topicTitleSizeInSp);
            holder.authorLine.setTextSize(TypedValue.COMPLEX_UNIT_SP, topicInfosSizeInSp);
            holder.dateLine.setTextSize(TypedValue.COMPLEX_UNIT_SP, topicInfosSizeInSp);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.titleLine.setText(currentTopicContent.titleLineContent);
        holder.authorLine.setText(currentTopicContent.authorLineContent);
        holder.dateLine.setText(currentTopicContent.dateLineContent);

        switch (currentTopicInfos.type) {
            case "marque-on":
                holder.topicIcon.setImageDrawable(iconMarqueOn);
                break;
            case "marque-off":
                holder.topicIcon.setImageDrawable(iconMarqueOff);
                break;
            case "dossier2":
                holder.topicIcon.setImageDrawable(iconDossier2);
                break;
            case "lock":
                holder.topicIcon.setImageDrawable(iconLock);
                break;
            case "resolu":
                holder.topicIcon.setImageDrawable(iconResolu);
                break;
            case "ghost":
                holder.topicIcon.setImageDrawable(iconGhost);
                break;
            case "dossier1":
            default:
                holder.topicIcon.setImageDrawable(iconDossier1);
                break;
        }

        if (position % 2 == 0 && alternateBackgroundColor) {
            convertView.setBackgroundColor(ThemeManager.getColorInt(R.attr.themedAltBackgroundColor, parentActivity));
        } else {
            convertView.setBackgroundColor(ThemeManager.getColorInt(R.attr.themedDefaultBackgroundColor, parentActivity));
        }

        holder.titleLine.invalidate();
        holder.titleLine.requestLayout();
        convertView.invalidate();
        convertView.requestLayout();
        return convertView;
    }

    private class ViewHolder {
        public TextView titleLine;
        public TextView authorLine;
        public TextView dateLine;
        public ImageView topicIcon;
    }

    private class ContentHolder {
        public Spanned titleLineContent;
        public Spanned authorLineContent;
        public Spanned dateLineContent;
    }
}
