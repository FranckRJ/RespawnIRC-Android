package com.franckrj.respawnirc.jvctopic.jvctopicviewers;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.LeadingMarginSpan;
import android.text.style.LineBackgroundSpan;
import android.text.style.QuoteSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.utils.CustomImageGetter;
import com.franckrj.respawnirc.utils.CustomTagHandler;
import com.franckrj.respawnirc.utils.ImageDownloader;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.LongClickLinkMovementMethod;
import com.franckrj.respawnirc.utils.LongClickableSpan;
import com.franckrj.respawnirc.utils.Undeprecator;
import com.franckrj.respawnirc.utils.Utils;

import java.util.ArrayList;

public class JVCTopicAdapter extends BaseAdapter {
    private ArrayList<JVCParser.MessageInfos> listOfMessages = new ArrayList<>();
    private ArrayList<ContentHolder> listOfContentForMessages = new ArrayList<>();
    private LayoutInflater serviceInflater;
    private Activity parentActivity = null;
    private int currentItemIDSelected = -1;
    private PopupMenu.OnMenuItemClickListener actionWhenItemMenuClicked = null;
    private JVCParser.Settings currentSettings = null;
    private int idOfLayoutToUse = 0;
    private boolean alternateBackgroundColor = false;
    private CustomTagHandler tagHandler = new CustomTagHandler();
    private ImageDownloader downloaderForImage = new ImageDownloader();
    private URLClicked urlCLickedListener = null;
    private PseudoClicked pseudoCLickedListener = null;
    private Html.ImageGetter jvcImageGetter = null;
    private boolean showSurvey = false;
    private String surveyTitle = "";
    private View.OnClickListener onSurveyClickListener = null;

    private final ImageDownloader.DownloadFinished listenerForDownloadFinished = new ImageDownloader.DownloadFinished() {
        @Override
        public void newDownloadFinished(int numberOfDownloadRemaining) {
            if (numberOfDownloadRemaining == 0) {
                notifyDataSetChanged();
            }
        }
    };

    private final View.OnClickListener menuButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View buttonView) {
            PopupMenu popup = new PopupMenu(parentActivity, buttonView);
            MenuInflater inflater = popup.getMenuInflater();
            JVCParser.MessageInfos itemSelected;

            currentItemIDSelected = (int) buttonView.getTag();
            itemSelected = getItem(currentItemIDSelected);
            popup.setOnMenuItemClickListener(actionWhenItemMenuClicked);

            if (itemSelected.pseudo.toLowerCase().equals(currentSettings.pseudoOfUser.toLowerCase())) {
                inflater.inflate(R.menu.menu_message_user, popup.getMenu());
            } else {
                inflater.inflate(R.menu.menu_message_others, popup.getMenu());
            }

            if (itemSelected.numberOfOverlyQuote > currentSettings.maxNumberOfOverlyQuotes) {
                if (itemSelected.showOverlyQuote) {
                    popup.getMenu().add(Menu.NONE, R.id.menu_hide_quote_message, Menu.NONE, R.string.hideQuoteMessage);
                } else {
                    popup.getMenu().add(Menu.NONE, R.id.menu_show_quote_message, Menu.NONE, R.string.showQuoteMessage);
                }
            }

            if (itemSelected.containSpoil) {
                if (itemSelected.showSpoil) {
                    popup.getMenu().add(Menu.NONE, R.id.menu_hide_spoil_message, Menu.NONE, R.string.hideSpoilMessage);
                } else {
                    popup.getMenu().add(Menu.NONE, R.id.menu_show_spoil_message, Menu.NONE, R.string.showSpoilMessage);
                }
            }

            popup.show();
        }
    };

    public JVCTopicAdapter(Activity newParentActivity, JVCParser.Settings newSettings) {
        Drawable deletedDrawable;
        Resources res;

        parentActivity = newParentActivity;
        currentSettings = newSettings;
        serviceInflater = (LayoutInflater) parentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        res = parentActivity.getResources();
        deletedDrawable = Undeprecator.resourcesGetDrawable(res, R.drawable.image_deleted);
        deletedDrawable.setBounds(0, 0, deletedDrawable.getIntrinsicWidth(), deletedDrawable.getIntrinsicHeight());

        jvcImageGetter = new CustomImageGetter(parentActivity, deletedDrawable, downloaderForImage);
        downloaderForImage.setParentActivity(parentActivity);
        downloaderForImage.setListenerForDownloadFinished(listenerForDownloadFinished);
        downloaderForImage.setImagesCacheDir(parentActivity.getCacheDir());
        downloaderForImage.setImagesSize(res.getDimensionPixelSize(R.dimen.imagesWidth), res.getDimensionPixelSize(R.dimen.imagesHeight));
        downloaderForImage.setDefaultDrawable(Undeprecator.resourcesGetDrawable(res, R.drawable.image_file_download));
        downloaderForImage.setDeletedDrawable(deletedDrawable);
    }

    public int getCurrentItemIDSelected() {
        return currentItemIDSelected;
    }

    public ArrayList<JVCParser.MessageInfos> getAllItems() {
        return listOfMessages;
    }

    public boolean getShowSurvey() {
        return showSurvey;
    }

    public void setUrlCLickedListener(URLClicked newListener) {
        urlCLickedListener = newListener;
    }

    public void setPseudoClickedListener(PseudoClicked newListener) {
        pseudoCLickedListener = newListener;
    }

    public void setActionWhenItemMenuClicked(PopupMenu.OnMenuItemClickListener newAction) {
        actionWhenItemMenuClicked = newAction;
    }

    public void setIdOfLayoutToUse(int newIdToUse) {
        idOfLayoutToUse = newIdToUse;
    }

    public void setAlternateBackgroundColor(boolean newVal) {
        alternateBackgroundColor = newVal;
    }

    public void setOnSurveyClickListener(View.OnClickListener newListener) {
        onSurveyClickListener = newListener;
    }

    public void enableSurvey(String newSurveyTitle) {
        showSurvey = true;
        surveyTitle = newSurveyTitle;
    }

    public void disableSurvey() {
        showSurvey = false;
    }

    public void removeAllItems() {
        listOfMessages.clear();
        listOfContentForMessages.clear();
        downloaderForImage.clearMemoryCache();
    }

    public void removeFirstItem() {
        listOfMessages.remove(0);
        listOfContentForMessages.remove(0);
    }

    public void addItem(JVCParser.MessageInfos item) {
        listOfContentForMessages.add(updateHolderWithNewItem(new ContentHolder(), item));
        listOfMessages.add(item);
    }

    public void updateThisItem(JVCParser.MessageInfos item) {
        for (int i = 0; i < listOfMessages.size(); ++i) {
            if (listOfMessages.get(i).id == item.id) {
                updateHolderWithNewItem(listOfContentForMessages.get(i), item);
                listOfMessages.set(i, item);
                break;
            }
        }
    }

    public void updateAllItems() {
        notifyDataSetChanged();
    }

    private ContentHolder updateHolderWithNewItem(ContentHolder holder, JVCParser.MessageInfos item) {
        holder.firstLineContent = replaceQuoteAndUrlSpans(Undeprecator.htmlFromHtml(JVCParser.createMessageFirstLineFromInfos(item, currentSettings), jvcImageGetter, tagHandler));
        holder.secondLineContent = replaceQuoteAndUrlSpans(Undeprecator.htmlFromHtml(JVCParser.createMessageSecondLineFromInfos(item, currentSettings), jvcImageGetter, tagHandler));
        return holder;
    }

    private Spannable replaceQuoteAndUrlSpans(Spanned spanToChange) {
        Spannable spannable = new SpannableString(spanToChange);
        QuoteSpan[] quoteSpanArray = spannable.getSpans(0, spannable.length(), QuoteSpan.class);
        for (QuoteSpan quoteSpan : quoteSpanArray) {
            Utils.replaceSpanByAnotherSpan(spannable, quoteSpan, new CustomQuoteSpan(Undeprecator.resourcesGetColor(parentActivity.getResources(), R.color.colorQuoteBackground),
                    Undeprecator.resourcesGetColor(parentActivity.getResources(), R.color.colorPrimary),
                    parentActivity.getResources().getDimensionPixelSize(R.dimen.quoteStripSize),
                    parentActivity.getResources().getDimensionPixelSize(R.dimen.quoteStripGap)));
        }
        URLSpan[] urlSpanArray = spannable.getSpans(0, spannable.length(), URLSpan.class);
        for (final URLSpan urlSpan : urlSpanArray) {
            Utils.replaceSpanByAnotherSpan(spannable, urlSpan, new LongClickableSpan() {
                private String url = urlSpan.getURL();
                @Override
                public void onClick(View view) {
                    if (urlCLickedListener != null) {
                        urlCLickedListener.getClickedURL(url, false);
                    }
                }
                @Override
                public void onLongClick(View v) {
                    if (urlCLickedListener != null) {
                        urlCLickedListener.getClickedURL(url, true);
                    }
                }
            });
        }
        return spannable;
    }

    @Override
    public int getCount() {
        return listOfMessages.size() + (showSurvey ? 1 : 0);
    }

    @Override
    public JVCParser.MessageInfos getItem(int position) {
        position = position - (showSurvey ? 1 : 0);
        if (position < 0) {
            return new JVCParser.MessageInfos();
        }
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

            convertView = serviceInflater.inflate(idOfLayoutToUse, parent, false);
            holder.firstLine = (TextView) convertView.findViewById(R.id.item_one_jvcmessages_text_row);
            holder.secondLine = (TextView) convertView.findViewById(R.id.item_two_jvcmessages_text_row);
            holder.showMenuButton = (ImageButton) convertView.findViewById(R.id.menu_overflow_row);

            holder.secondLine.setMovementMethod(LongClickLinkMovementMethod.getInstance());
            holder.showMenuButton.setOnClickListener(menuButtonClicked);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (position == 0 && showSurvey) {
            String advertiseForSurveyToShow = parentActivity.getString(R.string.titleForSurvey) + " <b>" + surveyTitle + "</b><br><small>" + parentActivity.getString(R.string.clickHereToSee) + "</small>";
            holder.showMenuButton.setVisibility(View.GONE);
            holder.secondLine.setVisibility(View.GONE);
            holder.firstLine.setText(Undeprecator.htmlFromHtml(advertiseForSurveyToShow));
            convertView.setOnClickListener(onSurveyClickListener);
            holder.firstLine.setOnClickListener(onSurveyClickListener);
            convertView.setBackgroundColor(Undeprecator.resourcesGetColor(parentActivity.getResources(), R.color.altBackgroundMessageColor));
        } else {
            final int realPosition = position - (showSurvey ? 1 : 0);
            holder.showMenuButton.setTag(position);
            holder.showMenuButton.setVisibility(View.VISIBLE);
            holder.secondLine.setVisibility(View.VISIBLE);
            holder.firstLine.setText(listOfContentForMessages.get(realPosition).firstLineContent);
            holder.secondLine.setText(listOfContentForMessages.get(realPosition).secondLineContent);
            convertView.setOnClickListener(null);
            holder.firstLine.setOnClickListener(new View.OnClickListener() {
                int messageNumberInList = realPosition;
                @Override
                public void onClick(View v) {
                    if (pseudoCLickedListener != null) {
                        pseudoCLickedListener.getMessageOfPseudoClicked(listOfMessages.get(messageNumberInList));
                    }
                }
            });

            if (position % 2 == 0 || !alternateBackgroundColor) {
                convertView.setBackgroundColor(Undeprecator.resourcesGetColor(parentActivity.getResources(), R.color.defaultColorForBackground));
            } else {
                convertView.setBackgroundColor(Undeprecator.resourcesGetColor(parentActivity.getResources(), R.color.altBackgroundMessageColor));
            }
        }

        return convertView;
    }

    public class CustomQuoteSpan implements LeadingMarginSpan, LineBackgroundSpan {
        private final int backgroundColor;
        private final int stripeColor;
        private final float stripeWidth;
        private final float gap;

        public CustomQuoteSpan(int newBackgroundColor, int newStripeColor, float newStripeWidth, float newGap) {
            backgroundColor = newBackgroundColor;
            stripeColor = newStripeColor;
            stripeWidth = newStripeWidth;
            gap = newGap;
        }

        @Override
        public int getLeadingMargin(boolean first) {
            return (int) (stripeWidth + gap);
        }

        @Override
        public void drawLeadingMargin(Canvas thisCanvas, Paint thisPaint, int pos, int dir, int top, int baseline, int bottom,
                                      CharSequence text, int start, int end, boolean first, Layout thisLayout) {
            Paint.Style paintStyle = thisPaint.getStyle();
            int paintColor = thisPaint.getColor();

            thisPaint.setStyle(Paint.Style.FILL);
            thisPaint.setColor(stripeColor);

            thisCanvas.drawRect(pos, top, pos + dir * stripeWidth, bottom, thisPaint);

            thisPaint.setStyle(paintStyle);
            thisPaint.setColor(paintColor);
        }

        @Override
        public void drawBackground(Canvas thisCanvas, Paint thisPaint, int left, int right, int top, int baseline, int bottom,
                                   CharSequence text, int start, int end, int lnum) {
            int paintColor = thisPaint.getColor();
            thisPaint.setColor(backgroundColor);
            thisCanvas.drawRect(left, top, right, bottom, thisPaint);
            thisPaint.setColor(paintColor);
        }
    }

    private class ViewHolder {
        private TextView firstLine;
        private TextView secondLine;
        private ImageButton showMenuButton;
    }

    private class ContentHolder {
        private Spannable firstLineContent;
        private Spannable secondLineContent;
    }

    public interface URLClicked {
        void getClickedURL(String link, boolean itsLongClick);
    }

    public interface PseudoClicked {
        void getMessageOfPseudoClicked(JVCParser.MessageInfos messageClicked);
    }
}
