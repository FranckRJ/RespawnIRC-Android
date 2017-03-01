package com.franckrj.respawnirc.jvctopic.jvctopicviewers;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.v7.widget.CardView;
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
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.utils.ThemeManager;
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
    private boolean showSignatures = false;
    private boolean showAvatars = false;
    private String surveyTitle = "";
    private View.OnClickListener onSurveyClickListener = null;
    private float multiplierOfLineSizeForFirstLine = 0;

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

            if (currentSettings.hideUglyImages && itemSelected.containUglyImages) {
                if (itemSelected.showUglyImages) {
                    popup.getMenu().add(Menu.NONE, R.id.menu_hide_ugly_images_message, Menu.NONE, R.string.hideUglyImagesMessage);
                } else {
                    popup.getMenu().add(Menu.NONE, R.id.menu_show_ugly_images_message, Menu.NONE, R.string.showUglyImagesMessage);
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

    public void setShowSignatures(boolean newVal) {
        showSignatures = newVal;
    }

    public void setShowAvatars(boolean newVal) {
        showAvatars = newVal;
    }

    public void setMultiplierOfLineSizeForFirstLine(float newVal) {
        multiplierOfLineSizeForFirstLine = newVal;
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
        final int sizeOfListOfMessages = listOfMessages.size();
        for (int i = 0; i < sizeOfListOfMessages; ++i) {
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
        holder.firstLineContent = new SpannableString(Undeprecator.htmlFromHtml(JVCParser.createMessageFirstLineFromInfos(item, currentSettings)));
        holder.secondLineContent = replaceQuoteAndUrlSpans(Undeprecator.htmlFromHtml(JVCParser.createMessageSecondLineFromInfos(item, currentSettings), jvcImageGetter, tagHandler));

        if (showAvatars && !item.avatarLink.isEmpty()) {
            holder.firstImageDrawable = downloaderForImage.getDrawableFromLink(item.avatarLink);
        } else {
            holder.firstImageDrawable = null;
        }

        if (!showSignatures || item.signatureNotParsed.isEmpty()) {
            holder.thirdLineContent = null;
        } else {
            holder.thirdLineContent = replaceQuoteAndUrlSpans(Undeprecator.htmlFromHtml(JVCParser.createSignatureFromInfos(item, currentSettings), jvcImageGetter, tagHandler));
        }

        return holder;
    }

    private Spannable replaceQuoteAndUrlSpans(Spanned spanToChange) {
        Spannable spannable = new SpannableString(spanToChange);
        QuoteSpan[] quoteSpanArray = spannable.getSpans(0, spannable.length(), QuoteSpan.class);
        for (QuoteSpan quoteSpan : quoteSpanArray) {
            Utils.replaceSpanByAnotherSpan(spannable, quoteSpan, new CustomQuoteSpan(Undeprecator.resourcesGetColor(parentActivity.getResources(), ThemeManager.getColorRes(ThemeManager.ColorName.COLOR_QUOTE_BACKGROUND)),
                    Undeprecator.resourcesGetColor(parentActivity.getResources(), ThemeManager.getColorRes(ThemeManager.ColorName.COLOR_PRIMARY)),
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

    private void setColorBackgroundOfThisItem(View backrgoundView, @ColorRes int colorID) {
        if (backrgoundView instanceof CardView) {
            CardView currentBackgroundView = (CardView) backrgoundView;
            currentBackgroundView.setCardBackgroundColor(Undeprecator.resourcesGetColor(parentActivity.getResources(), colorID));
        } else {
            backrgoundView.setBackgroundColor(Undeprecator.resourcesGetColor(parentActivity.getResources(), colorID));
        }
    }

    public JVCParser.MessageInfos getItem(int position) {
        position = position - (showSurvey ? 1 : 0);
        if (position < 0) {
            return new JVCParser.MessageInfos();
        }
        return listOfMessages.get(position);
    }

    @Override
    public int getCount() {
        return listOfMessages.size() + (showSurvey ? 1 : 0);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CustomViewHolder viewHolder;

        if (convertView == null) {
            convertView = serviceInflater.inflate(idOfLayoutToUse, parent, false);
            viewHolder = new CustomViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (CustomViewHolder) convertView.getTag();
        }

        if (position == 0 && showSurvey) {
            String advertiseForSurveyToShow = parentActivity.getString(R.string.titleForSurvey) + " <b>" + surveyTitle + "</b><br><small>" + parentActivity.getString(R.string.clickHereToSee) + "</small>";

            viewHolder.showMenuButton.setVisibility(View.GONE);
            viewHolder.secondLine.setVisibility(View.GONE);
            viewHolder.thirdLine.setVisibility(View.GONE);
            viewHolder.separator.setVisibility(View.GONE);
            if (viewHolder.firstImage != null) {
                viewHolder.firstImage.setVisibility(View.GONE);
            }

            viewHolder.firstLine.setText(Undeprecator.htmlFromHtml(advertiseForSurveyToShow));
            convertView.setOnClickListener(onSurveyClickListener);
            viewHolder.firstLine.setOnClickListener(onSurveyClickListener);
            setColorBackgroundOfThisItem(convertView, ThemeManager.getColorRes(ThemeManager.ColorName.ALT_BACKGROUND_COLOR));
        } else {
            final int realPosition = position - (showSurvey ? 1 : 0);
            final ContentHolder currentContent = listOfContentForMessages.get(realPosition);

            viewHolder.showMenuButton.setTag(position);
            viewHolder.showMenuButton.setVisibility(View.VISIBLE);
            viewHolder.secondLine.setVisibility(View.VISIBLE);
            viewHolder.firstLine.setText(currentContent.firstLineContent);
            viewHolder.secondLine.setText(currentContent.secondLineContent);

            if (viewHolder.firstImage != null) {
                if (currentContent.firstImageDrawable != null) {
                    viewHolder.firstImage.setVisibility(View.VISIBLE);
                    viewHolder.firstImage.setImageDrawable(null);
                    viewHolder.firstImage.setImageDrawable(currentContent.firstImageDrawable);
                } else {
                    viewHolder.firstImage.setVisibility(View.GONE);
                }
            }

            if (currentContent.thirdLineContent != null) {
                viewHolder.thirdLine.setVisibility(View.VISIBLE);
                viewHolder.separator.setVisibility(View.VISIBLE);
                viewHolder.thirdLine.setText(currentContent.thirdLineContent);
            } else {
                viewHolder.thirdLine.setVisibility(View.GONE);
                viewHolder.separator.setVisibility(View.GONE);
            }

            convertView.setOnClickListener(null);
            viewHolder.firstLine.setOnClickListener(new View.OnClickListener() {
                int messageNumberInList = realPosition;
                @Override
                public void onClick(View v) {
                    if (pseudoCLickedListener != null) {
                        pseudoCLickedListener.getMessageOfPseudoClicked(listOfMessages.get(messageNumberInList));
                    }
                }
            });

            if (realPosition % 2 == 0 || !alternateBackgroundColor) {
                setColorBackgroundOfThisItem(convertView, ThemeManager.getColorRes(ThemeManager.ColorName.DEFAULT_BACKGROUND_COLOR));
            } else {
                setColorBackgroundOfThisItem(convertView, ThemeManager.getColorRes(ThemeManager.ColorName.ALT_BACKGROUND_COLOR));
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

    private class CustomViewHolder {
        public final TextView firstLine;
        public final ImageView firstImage;
        public final TextView secondLine;
        public final TextView thirdLine;
        public final View separator;
        public final ImageButton showMenuButton;

        public CustomViewHolder(View itemView) {
            firstLine = (TextView) itemView.findViewById(R.id.item_one_jvcmessages_text_row);
            firstImage = (ImageView) itemView.findViewById(R.id.image_one_jvcmessages_text_row);
            secondLine = (TextView) itemView.findViewById(R.id.item_two_jvcmessages_text_row);
            thirdLine = (TextView) itemView.findViewById(R.id.item_three_jvcmessages_text_row);
            separator = itemView.findViewById(R.id.item_separator_jvcmessages_text_row);
            showMenuButton = (ImageButton) itemView.findViewById(R.id.menu_overflow_row);

            if (multiplierOfLineSizeForFirstLine != 0) {
                firstLine.setLineSpacing(0, multiplierOfLineSizeForFirstLine);
            }
            secondLine.setMovementMethod(LongClickLinkMovementMethod.getInstance());
            thirdLine.setMovementMethod(LongClickLinkMovementMethod.getInstance());
            showMenuButton.setOnClickListener(menuButtonClicked);
        }
    }

    private class ContentHolder {
        private Spannable firstLineContent;
        private Spannable secondLineContent;
        private Spannable thirdLineContent;
        private Drawable firstImageDrawable;
    }

    public interface URLClicked {
        void getClickedURL(String link, boolean itsLongClick);
    }

    public interface PseudoClicked {
        void getMessageOfPseudoClicked(JVCParser.MessageInfos messageClicked);
    }
}
