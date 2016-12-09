package com.franckrj.respawnirc.jvcmsgviewers;

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
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.utils.CustomTagHandler;
import com.franckrj.respawnirc.utils.ImageDownloader;
import com.franckrj.respawnirc.utils.JVCParser;

import java.util.ArrayList;

public class JVCMessagesAdapter extends BaseAdapter {
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

    private final Html.ImageGetter jvcImageGetter = new Html.ImageGetter() {
        @Override
        public Drawable getDrawable(String source) {
            if (!source.startsWith("http")) {
                Drawable drawable;
                Resources res = parentActivity.getResources();
                int resID = res.getIdentifier(source.substring(0, source.lastIndexOf(".")), "drawable", parentActivity.getPackageName());

                try {
                    drawable = res.getDrawable(resID);
                } catch (Exception e) {
                    e.printStackTrace();
                    drawable = res.getDrawable(R.drawable.image_deleted);
                }
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

                return drawable;
            } else {
                return downloaderForImage.getDrawableFromLink(source);
            }
        }
    };

    private final ImageDownloader.DownloadFinished listenerForDownloadFinished = new ImageDownloader.DownloadFinished() {
        @Override
        public void newDownloadFinished(int numberOfDownloadRemaining) {
            if (numberOfDownloadRemaining == 0) {
                notifyDataSetInvalidated();
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
                    inflater.inflate(R.menu.menu_message_hide_quote, popup.getMenu());
                } else {
                    inflater.inflate(R.menu.menu_message_show_quote, popup.getMenu());
                }
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

    public JVCMessagesAdapter(Activity newParentActivity, JVCParser.Settings newSettings) {
        Resources res;

        parentActivity = newParentActivity;
        currentSettings = newSettings;
        serviceInflater = (LayoutInflater) parentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        res = parentActivity.getResources();
        downloaderForImage.setListenerForDownloadFinished(listenerForDownloadFinished);
        downloaderForImage.setImagesSize(res.getDimensionPixelSize(R.dimen.imagesWidth), res.getDimensionPixelSize(R.dimen.imagesHeight));
        downloaderForImage.setDefaultDrawable(res.getDrawable(R.drawable.image_file_download));
    }

    public int getCurrentItemIDSelected() {
        return currentItemIDSelected;
    }

    public ArrayList<JVCParser.MessageInfos> getAllItems() {
        return listOfMessages;
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

    public void removeAllItems() {
        listOfMessages.clear();
        listOfContentForMessages.clear();
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
        holder.firstLineContent = replaceQuoteSpans(Html.fromHtml(JVCParser.createMessageFirstLineFromInfos(item, currentSettings), jvcImageGetter, tagHandler));
        holder.secondLineContent = replaceQuoteSpans(Html.fromHtml(JVCParser.createMessageSecondLineFromInfos(item, currentSettings), jvcImageGetter, tagHandler));
        return holder;
    }

    private Spannable replaceQuoteSpans(Spanned spanToChange) {
        Spannable spannable = new SpannableString(spanToChange);
        QuoteSpan[] quoteSpanArray = spannable.getSpans(0, spannable.length(), QuoteSpan.class);
        for (QuoteSpan quoteSpan : quoteSpanArray) {
            int start = spannable.getSpanStart(quoteSpan);
            int end = spannable.getSpanEnd(quoteSpan);
            int flags = spannable.getSpanFlags(quoteSpan);
            spannable.removeSpan(quoteSpan);
            spannable.setSpan(new CustomQuoteSpan(parentActivity.getResources().getColor(R.color.colorQuoteBackground),
                            parentActivity.getResources().getColor(R.color.colorPrimary),
                            parentActivity.getResources().getDimensionPixelSize(R.dimen.quoteStripSize),
                            parentActivity.getResources().getDimensionPixelSize(R.dimen.quoteStripGap)), start, end, flags);
        }
        return spannable;
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

            convertView = serviceInflater.inflate(idOfLayoutToUse, parent, false);
            holder.firstLine = (TextView) convertView.findViewById(R.id.item_one_jvcmessages_text_row);
            holder.secondLine = (TextView) convertView.findViewById(R.id.item_two_jvcmessages_text_row);
            holder.showMenuButton = (ImageButton) convertView.findViewById(R.id.menu_overflow_row);
            holder.background = convertView.getBackground();

            holder.showMenuButton.setOnClickListener(menuButtonClicked);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.showMenuButton.setTag(position);
        holder.firstLine.setText(listOfContentForMessages.get(position).firstLineContent);
        holder.secondLine.setText(listOfContentForMessages.get(position).secondLineContent);

        if (position % 2 == 0 || !alternateBackgroundColor) {
            convertView.setBackgroundDrawable(holder.background);
        } else {
            convertView.setBackgroundColor(parentActivity.getResources().getColor(R.color.altBackgroundMessageColor));
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
        private Drawable background;
    }

    private class ContentHolder {
        private Spannable firstLineContent;
        private Spannable secondLineContent;
    }
}