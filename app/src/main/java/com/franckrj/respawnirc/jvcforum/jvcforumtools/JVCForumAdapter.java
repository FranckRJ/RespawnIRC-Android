package com.franckrj.respawnirc.jvcforum.jvcforumtools;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.utils.CustomSpannableFactory;
import com.franckrj.respawnirc.utils.ThemeManager;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.Undeprecator;
import com.franckrj.respawnirc.utils.Utils;

import java.util.ArrayList;

public class JVCForumAdapter extends BaseAdapter {
    private ArrayList<JVCParser.TopicInfos> listOfTopics = new ArrayList<>();
    private ArrayList<ContentHolder> listOfContentForTopics = new ArrayList<>();
    private CustomSpannableFactory spannableFactory = new CustomSpannableFactory();
    private LayoutInflater serviceInflater;
    private boolean alternateBackgroundColor = false;
    private @ColorInt int topicNameColor = 0;
    private @ColorInt int pseudoModoColor = 0;
    private @ColorInt int pseudoAdminColor = 0;
    private @ColorInt int altBackgroundColor = 0;
    private @ColorInt int defaultBackgroundColor = 0;
    private int topicTitleSizeInSp = 14;
    private int topicInfosSizeInSp = 14;
    private Drawable iconMarqueOn;
    private Drawable iconMarqueOff;
    private Drawable iconDossier2;
    private Drawable iconLock;
    private Drawable iconResolu;
    private Drawable iconGhost;
    private Drawable iconDossier1;

    public JVCForumAdapter(Activity parentActivity) {
        serviceInflater = (LayoutInflater) parentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        iconMarqueOn = parentActivity.getDrawable(R.drawable.icon_topic_marque_on);
        iconMarqueOff = parentActivity.getDrawable(R.drawable.icon_topic_marque_off);
        iconDossier2 = parentActivity.getDrawable(R.drawable.icon_topic_dossier2);
        iconLock = ThemeManager.getDrawable(R.attr.themedTopicLockIcon, parentActivity);
        iconResolu = parentActivity.getDrawable(R.drawable.icon_topic_resolu);
        iconGhost = parentActivity.getDrawable(R.drawable.icon_topic_ghost);
        iconDossier1 = parentActivity.getDrawable(R.drawable.icon_topic_dossier1);
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

    @ColorInt
    public int getTopicNameColor() {
        return topicNameColor;
    }

    @ColorInt
    public int getAltBackgroundColor() {
        return altBackgroundColor;
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

    public void setTopicNameColor(@ColorInt int newColor) {
        topicNameColor = newColor;
    }

    public void setPseudoModoColor(@ColorInt int newColor) {
        pseudoModoColor = newColor;
    }

    public void setPseudoAdminColor(@ColorInt int newColor) {
        pseudoAdminColor = newColor;
    }

    public void setAltBackgroundColor(@ColorInt int newColor) {
        altBackgroundColor = newColor;
    }

    public void setDefaultBackgroundColor(@ColorInt int newColor) {
        defaultBackgroundColor = newColor;
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

        if (item.type.equals("message")) {
            thisHolder.titleLineContent = new SpannableString(Utils.applyEmojiCompatIfPossible(Undeprecator.htmlFromHtml("<b>" + item.htmlName + "</b>")));
        } else {
            thisHolder.titleLineContent = new SpannableString(Utils.applyEmojiCompatIfPossible(Undeprecator.htmlFromHtml("<b><font color=\"" + Utils.colorToString(topicNameColor) +
                                                                                               "\">" + item.htmlName + "</font> (" + item.nbOfMessages + ")</b>")));
        }

        switch (item.authorType) {
            case "modo":
                textForAuthor = "<small><font color=\"" + Utils.colorToString(pseudoModoColor) + "\">" + item.author + "</font></small>";
                break;
            case "admin":
            case "staff":
                textForAuthor = "<small><font color=\"" + Utils.colorToString(pseudoAdminColor) + "\">" + item.author + "</font></small>";
                break;
            default:
                textForAuthor = "<small>" + item.author + "</small>";
                break;
        }

        thisHolder.authorLineContent = new SpannableString(Undeprecator.htmlFromHtml(textForAuthor));
        thisHolder.dateLineContent = new SpannableString(Undeprecator.htmlFromHtml("<small>" + item.wholeDate + "</small>"));

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
        JVCParser.TopicInfos currentTopicInfos = listOfTopics.get(position);
        ContentHolder currentTopicContent = listOfContentForTopics.get(position);

        if (convertView == null) {
            holder = new ViewHolder();

            convertView = serviceInflater.inflate(R.layout.jvctopics_row, parent, false);
            holder.titleLine = convertView.findViewById(R.id.title_text_jvctopics_row);
            holder.authorLine = convertView.findViewById(R.id.author_text_jvctopics_row);
            holder.dateLine = convertView.findViewById(R.id.date_text_jvctopics_row);
            holder.topicIcon = convertView.findViewById(R.id.topic_icon_jvctopics_row);

            holder.titleLine.setSpannableFactory(spannableFactory);
            holder.authorLine.setSpannableFactory(spannableFactory);
            holder.dateLine.setSpannableFactory(spannableFactory);
            holder.titleLine.setTextSize(TypedValue.COMPLEX_UNIT_SP, topicTitleSizeInSp);
            holder.authorLine.setTextSize(TypedValue.COMPLEX_UNIT_SP, topicInfosSizeInSp);
            holder.dateLine.setTextSize(TypedValue.COMPLEX_UNIT_SP, topicInfosSizeInSp);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.titleLine.setText(currentTopicContent.titleLineContent, TextView.BufferType.SPANNABLE);
        holder.authorLine.setText(currentTopicContent.authorLineContent, TextView.BufferType.SPANNABLE);
        holder.dateLine.setText(currentTopicContent.dateLineContent, TextView.BufferType.SPANNABLE);

        holder.topicIcon.setVisibility(View.VISIBLE);
        switch (currentTopicInfos.type) {
            case "topic-pin-on":
                holder.topicIcon.setImageDrawable(iconMarqueOn);
                break;
            case "topic-pin-off":
                holder.topicIcon.setImageDrawable(iconMarqueOff);
                break;
            case "topic-folder2":
                holder.topicIcon.setImageDrawable(iconDossier2);
                break;
            case "topic-lock":
                holder.topicIcon.setImageDrawable(iconLock);
                break;
            case "topic-resolved":
                holder.topicIcon.setImageDrawable(iconResolu);
                break;
            case "topic-removed":
                holder.topicIcon.setImageDrawable(iconGhost);
                break;
            case "topic-message":
                holder.topicIcon.setVisibility(View.INVISIBLE);
                break;
            case "topic-folder1":
            default:
                holder.topicIcon.setImageDrawable(iconDossier1);
                break;
        }

        if (position % 2 == 0 && alternateBackgroundColor) {
            convertView.setBackgroundColor(altBackgroundColor);
        } else {
            convertView.setBackgroundColor(defaultBackgroundColor);
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
        public Spannable titleLineContent;
        public Spannable authorLineContent;
        public Spannable dateLineContent;
    }
}
