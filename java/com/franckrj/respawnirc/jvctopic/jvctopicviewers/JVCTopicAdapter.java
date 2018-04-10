package com.franckrj.respawnirc.jvctopic.jvctopicviewers;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.v7.widget.CardView;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.LineBackgroundSpan;
import android.text.style.QuoteSpan;
import android.text.style.URLSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.utils.CustomSpannableFactory;
import com.franckrj.respawnirc.utils.HoldingStringSpan;
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
    private int currentItemIdSelected = -1;
    private MenuItemClickedInMessage actionWhenItemMenuClicked = null;
    private JVCParser.Settings currentSettings;
    private int idOfLayoutToUse = 0;
    private boolean alternateBackgroundColor = false;
    private CustomSpannableFactory spannableFactory = new CustomSpannableFactory();
    private CustomTagHandler tagHandler = new CustomTagHandler();
    private ImageDownloader downloaderForImage = new ImageDownloader();
    private URLClicked urlCLickedListener = null;
    private PseudoClicked pseudoCLickedListener = null;
    private CustomImageGetter jvcImageGetter;
    private boolean showSurvey = false;
    private boolean showSignatures = false;
    private boolean showAvatars = false;
    private boolean showSpoilDefault = false;
    private boolean fastRefreshOfImages = false;
    private boolean colorDeletedMessages = true;
    private String surveyTitle = "";
    private View.OnClickListener onSurveyClickListener = null;
    private float multiplierOfLineSizeForInfoLineIfAvatarIsShowed = 0;
    private int avatarSize = -1;
    private int messageFontSizeInSp = 14;
    private int messageInfosFontSizeInSp = 14;
    private int messageSignatureFontSizeInSp = 14;
    private @ColorInt int quoteBackgroundColor = 0;
    private @ColorInt int quoteStripeColor = 0;
    private int quoteStripeSize = 0;
    private int quoteGapSize = 0;
    private String baseTitleForSurvey = "";
    private String baseSubTitleForSurvey = "";
    private @ColorInt int surveyMessageBackgroundColor = 0;
    private @ColorInt int deletedMessageBackgroundColor = 0;
    private @ColorInt int defaultMessageBackgroundColor = 0;
    private @ColorInt int altMessageBackgroundColor = 0;

    @SuppressWarnings("FieldCanBeLocal")
    private final ImageDownloader.DownloadFinished listenerForDownloadFinished = new ImageDownloader.DownloadFinished() {
        @Override
        public void newDownloadFinished(int numberOfDownloadRemaining) {
            if (numberOfDownloadRemaining == 0 || fastRefreshOfImages) {
                notifyDataSetChanged();
            }
        }
    };

    private final PopupMenu.OnMenuItemClickListener menuItemInPopupMenuClickedListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            //noinspection SimplifiableIfStatement
            if (actionWhenItemMenuClicked != null) {
                return actionWhenItemMenuClicked.onMenuItemClickedInMessage(item, getItem(currentItemIdSelected));
            } else {
                return false;
            }
        }
    };

    private final View.OnClickListener menuButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View buttonView) {
            PopupMenu popup = new PopupMenu(buttonView.getContext(), buttonView);
            Menu menu = popup.getMenu();
            JVCParser.MessageInfos itemSelected;

            currentItemIdSelected = (int) buttonView.getTag();
            itemSelected = getItem(currentItemIdSelected);
            popup.setOnMenuItemClickListener(menuItemInPopupMenuClickedListener);

            if (!itemSelected.pseudoIsBlacklisted) {
                menu.add(Menu.NONE, R.id.menu_quote_message, Menu.NONE, R.string.quoteMessage);

                if (itemSelected.userCanEditMessage) {
                    menu.add(Menu.NONE, R.id.menu_edit_message, Menu.NONE, R.string.editMessage);
                }

                if (itemSelected.userCanDeleteOrRestoreMessage) {
                    if (itemSelected.messageIsDeleted) {
                        menu.add(Menu.NONE, R.id.menu_restore_message, Menu.NONE, R.string.restore);
                    } else {
                        menu.add(Menu.NONE, R.id.menu_delete_message, Menu.NONE, R.string.delete);
                    }
                }

                if (itemSelected.userCanKickOrDekickAuthor) {
                    if (itemSelected.authorIsKicked) {
                        menu.add(Menu.NONE, R.id.menu_dekick_author_message, Menu.NONE, R.string.dekick);
                    } else {
                        menu.add(Menu.NONE, R.id.menu_kick_author_message, Menu.NONE, R.string.kick);
                    }
                }

                if (itemSelected.numberOfOverlyQuote > currentSettings.maxNumberOfOverlyQuotes) {
                    if (itemSelected.showOverlyQuote) {
                        menu.add(Menu.NONE, R.id.menu_hide_quote_message, Menu.NONE, R.string.hideQuoteMessage);
                    } else {
                        menu.add(Menu.NONE, R.id.menu_show_quote_message, Menu.NONE, R.string.showQuoteMessage);
                    }
                }

                if (itemSelected.messageContentContainSpoil || (showSignatures && itemSelected.signatureContainSpoil)) {
                    if (itemSelected.listOfSpoilIdToShow.isEmpty()) {
                        menu.add(Menu.NONE, R.id.menu_show_spoil_message, Menu.NONE, R.string.showSpoilMessage);
                    } else {
                        menu.add(Menu.NONE, R.id.menu_hide_spoil_message, Menu.NONE, R.string.hideSpoilMessage);
                    }
                }

                if (currentSettings.hideUglyImages && itemSelected.containUglyImages) {
                    if (itemSelected.showUglyImages) {
                        menu.add(Menu.NONE, R.id.menu_hide_ugly_images_message, Menu.NONE, R.string.hideUglyImagesMessage);
                    } else {
                        menu.add(Menu.NONE, R.id.menu_show_ugly_images_message, Menu.NONE, R.string.showUglyImagesMessage);
                    }
                }
            } else {
                menu.add(Menu.NONE, R.id.menu_show_blacklisted_message, Menu.NONE, R.string.showBlacklistedMessage);
            }

            popup.show();
        }
    };

    public JVCTopicAdapter(Activity parentActivity, JVCParser.Settings newSettings) {
        Resources res;

        currentSettings = newSettings;
        serviceInflater = (LayoutInflater) parentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        res = parentActivity.getResources();

        jvcImageGetter = new CustomImageGetter(parentActivity, ThemeManager.getDrawable(R.attr.themedDeletedImage, parentActivity), downloaderForImage);
        downloaderForImage.setParentActivity(parentActivity);
        downloaderForImage.setListenerForDownloadFinished(listenerForDownloadFinished);
        downloaderForImage.setImagesCacheDir(parentActivity.getCacheDir());
        downloaderForImage.setDefaultDrawable(ThemeManager.getDrawable(R.attr.themedDownloadImage, parentActivity));
        downloaderForImage.setDeletedDrawable(ThemeManager.getDrawable(R.attr.themedDeletedImage, parentActivity));
        downloaderForImage.setImagesSize(res.getDimensionPixelSize(R.dimen.miniNoelshackWidthDefault), res.getDimensionPixelSize(R.dimen.miniNoelshackHeightDefault), true);
    }

    //pas d'intérêt que tout le monde puisse accéder aux messages, seul le .isEmpty() est important sur cette liste.
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

    public void setActionWhenItemMenuClicked(MenuItemClickedInMessage newAction) {
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

    public void setShowSpoilDefault(boolean newVal) {
        showSpoilDefault = newVal;
    }

    public void setFastRefreshOfImages(boolean newVal) {
        fastRefreshOfImages = newVal;
    }

    public void setColorDeletedMessages(boolean newVal) {
        colorDeletedMessages = newVal;
    }

    public void setMultiplierOfLineSizeForInfoLineIfAvatarIsShowed(float newVal) {
        multiplierOfLineSizeForInfoLineIfAvatarIsShowed = newVal;
    }

    public void setOnSurveyClickListener(View.OnClickListener newListener) {
        onSurveyClickListener = newListener;
    }

    public void setAvatarSize(int newSize) {
        avatarSize = newSize;
    }

    public void setStickerSize(int newSize) {
        jvcImageGetter.setStickerSize(newSize);
    }

    public void setMiniNoeslahckSizeByWidth(int newWidth) {
        int newHeight = Utils.roundToInt(newWidth * 0.75);
        downloaderForImage.setImagesSize(newWidth, newHeight, true);
    }

    public void setMessageFontSizeInSp(int newVal) {
        messageFontSizeInSp = newVal;
    }

    public void setMessageInfosFontSizeInSp(int newVal) {
        messageInfosFontSizeInSp = newVal;
    }

    public void setMessageSignatureFontSizeInSp(int newVal) {
        messageSignatureFontSizeInSp = newVal;
    }

    public void setQuoteBackgroundColor(@ColorInt int newColor) {
        quoteBackgroundColor = newColor;
    }

    public void setQuoteStripeColor(@ColorInt int newColor) {
        quoteStripeColor = newColor;
    }

    public void setQuoteStripeSize(int newSize) {
        quoteStripeSize = newSize;
    }

    public void setQuoteGapSize(int newSize) {
        quoteGapSize = newSize;
    }

    public void setBaseTitleForSurvey(String newString) {
        baseTitleForSurvey = newString;
    }

    public void setBaseSubTitleForSurvey(String newString) {
        baseSubTitleForSurvey = newString;
    }

    public void setSurveyMessageBackgroundColor(@ColorInt int newColor) {
        surveyMessageBackgroundColor = newColor;
    }

    public void setDeletedMessageBackgroundColor(@ColorInt int newColor) {
        deletedMessageBackgroundColor = newColor;
    }

    public void setDefaultMessageBackgroundColor(@ColorInt int newColor) {
        defaultMessageBackgroundColor = newColor;
    }

    public void setAltMessageBackgroundColor(@ColorInt int newColor) {
        altMessageBackgroundColor = newColor;
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

    public void removeItemsWithThisPseudo(String pseudoToUse) {
        for (int i = 0; i < listOfMessages.size(); ) {
            if (listOfMessages.get(i).pseudo.toLowerCase().equals(pseudoToUse.toLowerCase())) {
                listOfMessages.remove(i);
                listOfContentForMessages.remove(i);
            } else {
                ++i;
            }
        }
    }

    public void blacklistItemsWithThisPseudo(String pseudoToUse) {
        for (int i = 0; i < listOfMessages.size(); ++i) {
            if (listOfMessages.get(i).pseudo.toLowerCase().equals(pseudoToUse.toLowerCase())) {
                listOfMessages.get(i).pseudoIsBlacklisted = true;
                updateHolderWithNewItem(listOfContentForMessages.get(i), listOfMessages.get(i), false);
            }
        }
    }

    public void addItem(JVCParser.MessageInfos item, boolean isANewItem) {
        listOfContentForMessages.add(updateHolderWithNewItem(new ContentHolder(), item, isANewItem));
        listOfMessages.add(item);
    }

    public void updateThisItem(JVCParser.MessageInfos item, boolean isANewItem) {
        final int sizeOfListOfMessages = listOfMessages.size();
        for (int i = 0; i < sizeOfListOfMessages; ++i) {
            if (listOfMessages.get(i).id == item.id) {
                updateHolderWithNewItem(listOfContentForMessages.get(i), item, isANewItem);
                listOfMessages.set(i, item);
                break;
            }
        }
    }

    private ContentHolder updateHolderWithNewItem(ContentHolder holder, JVCParser.MessageInfos item, boolean isARealNewItem) {
        if (isARealNewItem && showSpoilDefault) {
            item.listOfSpoilIdToShow.add(-1);
        }

        holder.infoLineContent = new SpannableString(Undeprecator.htmlFromHtml(JVCParser.createMessageInfoLineFromInfos(item, currentSettings)));
        if (!item.pseudoIsBlacklisted) {
            holder.messageLineContent = replaceNeededSpansAndEmojis(Undeprecator.htmlFromHtml(JVCParser.createMessageMessageLineFromInfos(item, currentSettings), jvcImageGetter, tagHandler), item);
        } else {
            holder.messageLineContent = null;
        }

        if (showAvatars && !item.avatarLink.isEmpty() && !item.pseudoIsBlacklisted) {
            holder.avatarImageDrawable = downloaderForImage.getDrawableFromLink(item.avatarLink);
        } else {
            holder.avatarImageDrawable = null;
        }

        if (!showSignatures || item.signatureNotParsed.isEmpty() || item.pseudoIsBlacklisted) {
            holder.signatureLineContent = null;
        } else {
            holder.signatureLineContent = replaceNeededSpansAndEmojis(Undeprecator.htmlFromHtml(JVCParser.createSignatureFromInfos(item, currentSettings), jvcImageGetter, tagHandler), item);
        }

        holder.messageIsDeleted = item.messageIsDeleted;

        return holder;
    }

    private Spannable replaceNeededSpansAndEmojis(Spanned spanToChange, final JVCParser.MessageInfos infosOfMessage) {
        Spannable spannable = new SpannableString(Utils.applyEmojiCompatIfPossible(spanToChange));

        QuoteSpan[] quoteSpanArray = spannable.getSpans(0, spannable.length(), QuoteSpan.class);
        for (QuoteSpan quoteSpan : quoteSpanArray) {
            Utils.replaceSpanByAnotherSpan(spannable, quoteSpan, new CustomQuoteSpan(quoteBackgroundColor, quoteStripeColor, quoteStripeSize, quoteGapSize));
        }

        URLSpan[] urlSpanArray = spannable.getSpans(0, spannable.length(), URLSpan.class);
        for (final URLSpan urlSpan : urlSpanArray) {
            Utils.replaceSpanByAnotherSpan(spannable, urlSpan, new LongClickableSpan() {
                @Override
                public void onClick(View view) {
                    if (urlCLickedListener != null) {
                        urlCLickedListener.getClickedURL(urlSpan.getURL(), false);
                    }
                }

                @Override
                public void onLongClick(View v) {
                    if (urlCLickedListener != null) {
                        urlCLickedListener.getClickedURL(urlSpan.getURL(), true);
                    }
                }
            });
        }

        HoldingStringSpan[] holdingStringSpanArray = spannable.getSpans(0, spannable.length(), HoldingStringSpan.class);
        for (final HoldingStringSpan holdingStringSpan : holdingStringSpanArray) {
            Utils.replaceSpanByAnotherSpan(spannable, holdingStringSpan, new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    updateListOfSpoidIdToShow(infosOfMessage, holdingStringSpan.getString());
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    //rien
                }
            });
        }

        return spannable;
    }

    private void updateListOfSpoidIdToShow(JVCParser.MessageInfos infosOfMessage, String instructionForUpdate) {
        boolean openSpoil = instructionForUpdate.startsWith("o");
        int spoilId;

        try {
            spoilId = Integer.parseInt(instructionForUpdate.substring(1));
        } catch (Exception e) {
            spoilId = -1;
        }

        if (spoilId >= 0) {
            if (openSpoil) {
                infosOfMessage.listOfSpoilIdToShow.add(spoilId);
            } else {
                infosOfMessage.listOfSpoilIdToShow.remove(spoilId);
            }
            updateThisItem(infosOfMessage, false);
            notifyDataSetChanged();
        }
    }

    private void setColorBackgroundOfThisItem(View backrgoundView, @ColorInt int colorValue) {
        if (backrgoundView instanceof CardView) {
            CardView currentBackgroundView = (CardView) backrgoundView;
            currentBackgroundView.setCardBackgroundColor(colorValue);
        } else {
            backrgoundView.setBackgroundColor(colorValue);
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
            String advertiseForSurveyToShow = baseTitleForSurvey + " <b>" + surveyTitle + "</b><br><small>" + baseSubTitleForSurvey + "</small>";

            viewHolder.showMenuButton.setVisibility(View.GONE);
            viewHolder.messageLine.setVisibility(View.GONE);
            viewHolder.signatureLine.setVisibility(View.GONE);
            viewHolder.separator.setVisibility(View.GONE);
            if (viewHolder.avatarImage != null) {
                viewHolder.avatarImage.setVisibility(View.GONE);
                viewHolder.avatarImage.setOnClickListener(null);
            }

            viewHolder.infoLine.setText(Utils.applyEmojiCompatIfPossible(Undeprecator.htmlFromHtml(advertiseForSurveyToShow)), TextView.BufferType.SPANNABLE);
            convertView.setOnClickListener(onSurveyClickListener);
            viewHolder.infoLine.setOnClickListener(onSurveyClickListener);
            setColorBackgroundOfThisItem(convertView, surveyMessageBackgroundColor);
        } else {
            final int realPosition = position - (showSurvey ? 1 : 0);
            final ContentHolder currentContent = listOfContentForMessages.get(realPosition);

            viewHolder.showMenuButton.setTag(position);
            viewHolder.showMenuButton.setVisibility(View.VISIBLE);
            viewHolder.infoLine.setText(currentContent.infoLineContent, TextView.BufferType.SPANNABLE);

            convertView.setOnClickListener(null);
            if (currentContent.messageLineContent != null) {
                View.OnClickListener infoClickedListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (pseudoCLickedListener != null) {
                            pseudoCLickedListener.getMessageOfPseudoClicked(listOfMessages.get(realPosition));
                        }
                    }
                };

                viewHolder.messageLine.setVisibility(View.VISIBLE);
                viewHolder.messageLine.setText(currentContent.messageLineContent, TextView.BufferType.SPANNABLE);

                viewHolder.infoLine.setOnClickListener(infoClickedListener);
                if (viewHolder.avatarImage != null) {
                    viewHolder.avatarImage.setOnClickListener(infoClickedListener);
                }
            } else {
                viewHolder.messageLine.setVisibility(View.GONE);
                viewHolder.infoLine.setOnClickListener(null);
                if (viewHolder.avatarImage != null) {
                    viewHolder.avatarImage.setOnClickListener(null);
                }
            }

            viewHolder.infoLine.setLineSpacing(0, 1);

            if (viewHolder.avatarImage != null) {
                if (currentContent.avatarImageDrawable != null) {
                    ViewGroup.LayoutParams avatarLayoutParams = viewHolder.avatarImage.getLayoutParams();
                    
                    viewHolder.avatarImage.setVisibility(View.VISIBLE);
                    viewHolder.avatarImage.setImageDrawable(null);
                    viewHolder.avatarImage.setImageDrawable(currentContent.avatarImageDrawable);
                    
                    if (avatarLayoutParams.width != avatarSize && avatarSize >= 0) {
                        avatarLayoutParams.height = avatarSize;
                        avatarLayoutParams.width = avatarSize;
                        /*Ligne nécessaire ?*/
                        //viewHolder.avatarImage.requestLayout();
                    }

                    if (multiplierOfLineSizeForInfoLineIfAvatarIsShowed != 0) {
                        viewHolder.infoLine.setLineSpacing(0, multiplierOfLineSizeForInfoLineIfAvatarIsShowed);
                    }
                } else {
                    viewHolder.avatarImage.setVisibility(View.GONE);
                }
            }

            if (currentContent.signatureLineContent != null) {
                viewHolder.signatureLine.setVisibility(View.VISIBLE);
                viewHolder.separator.setVisibility(View.VISIBLE);
                viewHolder.signatureLine.setText(currentContent.signatureLineContent, TextView.BufferType.SPANNABLE);
            } else {
                viewHolder.signatureLine.setVisibility(View.GONE);
                viewHolder.separator.setVisibility(View.GONE);
            }

            if (colorDeletedMessages && currentContent.messageIsDeleted) {
                setColorBackgroundOfThisItem(convertView, deletedMessageBackgroundColor);
            } else if (realPosition % 2 == 0 || !alternateBackgroundColor) {
                setColorBackgroundOfThisItem(convertView, defaultMessageBackgroundColor);
            } else {
                setColorBackgroundOfThisItem(convertView, altMessageBackgroundColor);
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
            return Utils.roundToInt(stripeWidth + gap);
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
        public final TextView infoLine;
        public final ImageView avatarImage;
        public final TextView messageLine;
        public final TextView signatureLine;
        public final View separator;
        public final ImageButton showMenuButton;

        public CustomViewHolder(View itemView) {
            infoLine = itemView.findViewById(R.id.infos_text_jvcmessages_row);
            avatarImage = itemView.findViewById(R.id.avatar_image_jvcmessages_row);
            messageLine = itemView.findViewById(R.id.message_text_jvcmessages_row);
            signatureLine = itemView.findViewById(R.id.signature_text_jvcmessages_row);
            separator = itemView.findViewById(R.id.separator_view_jvcmessages_row);
            showMenuButton = itemView.findViewById(R.id.menuoverflow_image_jvcmessages_row);

            infoLine.setSpannableFactory(spannableFactory);
            messageLine.setSpannableFactory(spannableFactory);
            signatureLine.setSpannableFactory(spannableFactory);
            infoLine.setTextSize(TypedValue.COMPLEX_UNIT_SP, messageInfosFontSizeInSp);
            messageLine.setTextSize(TypedValue.COMPLEX_UNIT_SP, messageFontSizeInSp);
            signatureLine.setTextSize(TypedValue.COMPLEX_UNIT_SP, messageSignatureFontSizeInSp);
            messageLine.setMovementMethod(LongClickLinkMovementMethod.getInstance());
            signatureLine.setMovementMethod(LongClickLinkMovementMethod.getInstance());
            showMenuButton.setOnClickListener(menuButtonClicked);
        }
    }

    private class ContentHolder {
        public Spannable infoLineContent;
        public Spannable messageLineContent;
        public Spannable signatureLineContent;
        public Drawable avatarImageDrawable;
        public boolean messageIsDeleted;
    }

    public interface URLClicked {
        void getClickedURL(String link, boolean itsLongClick);
    }

    public interface PseudoClicked {
        void getMessageOfPseudoClicked(JVCParser.MessageInfos messageClicked);
    }

    public interface MenuItemClickedInMessage {
        boolean onMenuItemClickedInMessage(MenuItem item, JVCParser.MessageInfos fromThisMessage);
    }
}
