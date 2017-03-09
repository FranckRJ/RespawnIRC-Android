package com.franckrj.respawnirc.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.app.DialogFragment;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.utils.ThemeManager;
import com.franckrj.respawnirc.utils.CustomImageGetter;
import com.franckrj.respawnirc.utils.LongClickLinkMovementMethod;
import com.franckrj.respawnirc.utils.LongClickableSpan;
import com.franckrj.respawnirc.utils.Undeprecator;
import com.franckrj.respawnirc.utils.Utils;

public class InsertStuffDialogFragment extends DialogFragment {
    private static final int MAX_NUMBER_OF_ROW = 20;
    private static Spanned[] listOfSpanForTextView = null;
    private static boolean textDecorationRowGeneratedForDarkTheme = false;

    private TextView mainTextView = null;
    private Html.ImageGetter jvcImageGetter = null;
    private ImageView[] listOfCategoryButtons = new ImageView[MAX_NUMBER_OF_ROW];
    private int oldRowNumber = 0;

    private final View.OnClickListener categoryButtonClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int newRowNumber = 0;
            try {
                newRowNumber = (int) v.getTag();
                if (newRowNumber < 0) {
                    newRowNumber = 0;
                } else if (newRowNumber >= MAX_NUMBER_OF_ROW) {
                    newRowNumber = MAX_NUMBER_OF_ROW - 1;
                }
            } catch (Exception e) {
                //rien
            }
            selectThisRow(newRowNumber);
        }
    };

    private void selectThisRow(int rowToUse) {
        listOfCategoryButtons[oldRowNumber].setBackgroundColor(Undeprecator.resourcesGetColor(getResources(), android.R.color.transparent));
        listOfCategoryButtons[rowToUse].setBackgroundColor(Undeprecator.resourcesGetColor(getResources(), ThemeManager.getColorRes(ThemeManager.ColorName.MORE_DARKER_BACKGROUND_COLOR)));
        initializeSpanForTextViewIfNeeded(jvcImageGetter, rowToUse);
        mainTextView.setText(replaceUrlSpans(listOfSpanForTextView[rowToUse]));
        oldRowNumber = rowToUse;
    }

    private void initializeSpanForTextViewIfNeeded(Html.ImageGetter withThisImageGetter, int row) {
        if (listOfSpanForTextView == null) {
            listOfSpanForTextView = new Spanned[MAX_NUMBER_OF_ROW];
            for (int i = 0; i < MAX_NUMBER_OF_ROW; ++i) {
                listOfSpanForTextView[i] = null;
            }
        }
        if (listOfSpanForTextView[row] == null || (row == 0 && textDecorationRowGeneratedForDarkTheme != ThemeManager.getThemeUsedIsDark())) {
            StringBuilder textForShowAllStuff = new StringBuilder();
            switch (row) {
                case 0:
                    //textformat
                    textDecorationRowGeneratedForDarkTheme = ThemeManager.getThemeUsedIsDark();
                    if (textDecorationRowGeneratedForDarkTheme) {
                        appendAnotherStuff("textformat_bold_dark", textForShowAllStuff);
                        appendAnotherStuff("textformat_italic_dark", textForShowAllStuff);
                        appendAnotherStuff("textformat_underline_dark", textForShowAllStuff);
                        appendAnotherStuff("textformat_strike_dark", textForShowAllStuff);
                        appendAnotherStuff("textformat_ulist_dark", textForShowAllStuff);
                        appendAnotherStuff("textformat_olist_dark", textForShowAllStuff);
                        appendAnotherStuff("textformat_quote_dark", textForShowAllStuff);
                        appendAnotherStuff("textformat_code_dark", textForShowAllStuff);
                        appendAnotherStuff("textformat_spoil_dark", textForShowAllStuff);
                    } else {
                        appendAnotherStuff("textformat_bold_light", textForShowAllStuff);
                        appendAnotherStuff("textformat_italic_light", textForShowAllStuff);
                        appendAnotherStuff("textformat_underline_light", textForShowAllStuff);
                        appendAnotherStuff("textformat_strike_light", textForShowAllStuff);
                        appendAnotherStuff("textformat_ulist_light", textForShowAllStuff);
                        appendAnotherStuff("textformat_olist_light", textForShowAllStuff);
                        appendAnotherStuff("textformat_quote_light", textForShowAllStuff);
                        appendAnotherStuff("textformat_code_light", textForShowAllStuff);
                        appendAnotherStuff("textformat_spoil_light", textForShowAllStuff);
                    }
                    break;
                case 1:
                    //ours
                    appendAnotherStuff("sticker_1f88", textForShowAllStuff);
                    appendAnotherStuff("sticker_1f89", textForShowAllStuff);
                    appendAnotherStuff("sticker_1f8a", textForShowAllStuff);
                    appendAnotherStuff("sticker_1f8b", textForShowAllStuff);
                    appendAnotherStuff("sticker_1f8c", textForShowAllStuff);
                    appendAnotherStuff("sticker_1f8d", textForShowAllStuff);
                    appendAnotherStuff("sticker_1f8e", textForShowAllStuff);
                    appendAnotherStuff("sticker_1f8f", textForShowAllStuff);
                    appendAnotherStuff("sticker_zu2", textForShowAllStuff);
                    appendAnotherStuff("sticker_zu6", textForShowAllStuff);
                    appendAnotherStuff("sticker_zu7", textForShowAllStuff);
                    appendAnotherStuff("sticker_zu8", textForShowAllStuff);
                    appendAnotherStuff("sticker_zu9", textForShowAllStuff);
                    appendAnotherStuff("sticker_zua", textForShowAllStuff);
                    appendAnotherStuff("sticker_zub", textForShowAllStuff);
                    appendAnotherStuff("sticker_zuc", textForShowAllStuff);
                    break;
                case 2:
                    //lapin
                    appendAnotherStuff("sticker_1jc3_en", textForShowAllStuff);
                    appendAnotherStuff("sticker_1jc3_fr", textForShowAllStuff);
                    appendAnotherStuff("sticker_1jc5", textForShowAllStuff);
                    appendAnotherStuff("sticker_1jcg", textForShowAllStuff);
                    appendAnotherStuff("sticker_1jch", textForShowAllStuff);
                    appendAnotherStuff("sticker_1jcl", textForShowAllStuff);
                    appendAnotherStuff("sticker_1leb", textForShowAllStuff);
                    appendAnotherStuff("sticker_1lej_en", textForShowAllStuff);
                    appendAnotherStuff("sticker_1lej_fr", textForShowAllStuff);
                    appendAnotherStuff("sticker_1leq_en", textForShowAllStuff);
                    appendAnotherStuff("sticker_1leq_fr", textForShowAllStuff);
                    appendAnotherStuff("sticker_1li3", textForShowAllStuff);
                    appendAnotherStuff("sticker_1li4", textForShowAllStuff);
                    appendAnotherStuff("sticker_1li5", textForShowAllStuff);
                    break;
                case 3:
                    //bourge
                    appendAnotherStuff("sticker_1jnc", textForShowAllStuff);
                    appendAnotherStuff("sticker_1jnd", textForShowAllStuff);
                    appendAnotherStuff("sticker_1jne", textForShowAllStuff);
                    appendAnotherStuff("sticker_1jnf", textForShowAllStuff);
                    appendAnotherStuff("sticker_1jng", textForShowAllStuff);
                    appendAnotherStuff("sticker_1jnh", textForShowAllStuff);
                    appendAnotherStuff("sticker_1jni", textForShowAllStuff);
                    appendAnotherStuff("sticker_1jnj", textForShowAllStuff);
                    break;
                case 4:
                    //lama
                    appendAnotherStuff("sticker_1kgu", textForShowAllStuff);
                    appendAnotherStuff("sticker_1kgv", textForShowAllStuff);
                    appendAnotherStuff("sticker_1kgw", textForShowAllStuff);
                    appendAnotherStuff("sticker_1kgx", textForShowAllStuff);
                    appendAnotherStuff("sticker_1kgy", textForShowAllStuff);
                    appendAnotherStuff("sticker_1kgz", textForShowAllStuff);
                    appendAnotherStuff("sticker_1kh0", textForShowAllStuff);
                    appendAnotherStuff("sticker_1kh1", textForShowAllStuff);
                    break;
                case 5:
                    //hap
                    appendAnotherStuff("sticker_1kkg", textForShowAllStuff);
                    appendAnotherStuff("sticker_1kkh", textForShowAllStuff);
                    appendAnotherStuff("sticker_1kki", textForShowAllStuff);
                    appendAnotherStuff("sticker_1kkj", textForShowAllStuff);
                    appendAnotherStuff("sticker_1kkk", textForShowAllStuff);
                    appendAnotherStuff("sticker_1kkl", textForShowAllStuff);
                    appendAnotherStuff("sticker_1kkm", textForShowAllStuff);
                    appendAnotherStuff("sticker_1kkn", textForShowAllStuff);
                    break;
                case 6:
                    //noel
                    appendAnotherStuff("sticker_1kko", textForShowAllStuff);
                    appendAnotherStuff("sticker_1kkp", textForShowAllStuff);
                    appendAnotherStuff("sticker_1kkq", textForShowAllStuff);
                    appendAnotherStuff("sticker_1kkr", textForShowAllStuff);
                    appendAnotherStuff("sticker_1kks", textForShowAllStuff);
                    appendAnotherStuff("sticker_1kkt", textForShowAllStuff);
                    appendAnotherStuff("sticker_1kku", textForShowAllStuff);
                    appendAnotherStuff("sticker_1kkv", textForShowAllStuff);
                    break;
                case 7:
                    //chat
                    appendAnotherStuff("sticker_1kky", textForShowAllStuff);
                    appendAnotherStuff("sticker_1kkz", textForShowAllStuff);
                    appendAnotherStuff("sticker_1kl0", textForShowAllStuff);
                    appendAnotherStuff("sticker_1kl1", textForShowAllStuff);
                    appendAnotherStuff("sticker_1kl2", textForShowAllStuff);
                    appendAnotherStuff("sticker_1kl3", textForShowAllStuff);
                    appendAnotherStuff("sticker_1kl4", textForShowAllStuff);
                    appendAnotherStuff("sticker_1kl5", textForShowAllStuff);
                    appendAnotherStuff("sticker_1kl6", textForShowAllStuff);
                    appendAnotherStuff("sticker_1kl7", textForShowAllStuff);
                    appendAnotherStuff("sticker_1kl8", textForShowAllStuff);
                    appendAnotherStuff("sticker_1kl9", textForShowAllStuff);
                    appendAnotherStuff("sticker_1kla", textForShowAllStuff);
                    appendAnotherStuff("sticker_1klb", textForShowAllStuff);
                    break;
                case 8:
                    //orc
                    appendAnotherStuff("sticker_1lga", textForShowAllStuff);
                    appendAnotherStuff("sticker_1lgb", textForShowAllStuff);
                    appendAnotherStuff("sticker_1lgc", textForShowAllStuff);
                    appendAnotherStuff("sticker_1lgd", textForShowAllStuff);
                    appendAnotherStuff("sticker_1lge", textForShowAllStuff);
                    appendAnotherStuff("sticker_1lgf", textForShowAllStuff);
                    appendAnotherStuff("sticker_1lgg", textForShowAllStuff);
                    appendAnotherStuff("sticker_1lgh", textForShowAllStuff);
                    break;
                case 9:
                    //dom
                    appendAnotherStuff("sticker_1ljj", textForShowAllStuff);
                    appendAnotherStuff("sticker_1ljl", textForShowAllStuff);
                    appendAnotherStuff("sticker_1ljm", textForShowAllStuff);
                    appendAnotherStuff("sticker_1ljn", textForShowAllStuff);
                    appendAnotherStuff("sticker_1ljo", textForShowAllStuff);
                    appendAnotherStuff("sticker_1ljp", textForShowAllStuff);
                    appendAnotherStuff("sticker_1ljq", textForShowAllStuff);
                    appendAnotherStuff("sticker_1ljr", textForShowAllStuff);
                    appendAnotherStuff("sticker_1rzs", textForShowAllStuff);
                    appendAnotherStuff("sticker_1rzt", textForShowAllStuff);
                    appendAnotherStuff("sticker_1rzu", textForShowAllStuff);
                    appendAnotherStuff("sticker_1rzv", textForShowAllStuff);
                    appendAnotherStuff("sticker_1rzw", textForShowAllStuff);
                    break;
                case 10:
                    //aventurier
                    appendAnotherStuff("sticker_1lm9", textForShowAllStuff);
                    appendAnotherStuff("sticker_1lma", textForShowAllStuff);
                    appendAnotherStuff("sticker_1lmb", textForShowAllStuff);
                    appendAnotherStuff("sticker_1lmc", textForShowAllStuff);
                    appendAnotherStuff("sticker_1lmd", textForShowAllStuff);
                    appendAnotherStuff("sticker_1lme", textForShowAllStuff);
                    appendAnotherStuff("sticker_1lmf", textForShowAllStuff);
                    appendAnotherStuff("sticker_1lmg", textForShowAllStuff);
                    break;
                case 11:
                    //saumon
                    appendAnotherStuff("sticker_1lmh", textForShowAllStuff);
                    appendAnotherStuff("sticker_1lmi", textForShowAllStuff);
                    appendAnotherStuff("sticker_1lmj", textForShowAllStuff);
                    appendAnotherStuff("sticker_1lmk", textForShowAllStuff);
                    appendAnotherStuff("sticker_1lml", textForShowAllStuff);
                    appendAnotherStuff("sticker_1lmm", textForShowAllStuff);
                    appendAnotherStuff("sticker_1lmn", textForShowAllStuff);
                    appendAnotherStuff("sticker_1lmo", textForShowAllStuff);
                    appendAnotherStuff("sticker_1lmp", textForShowAllStuff);
                    appendAnotherStuff("sticker_1mqv", textForShowAllStuff);
                    appendAnotherStuff("sticker_1mqw", textForShowAllStuff);
                    appendAnotherStuff("sticker_1mqx", textForShowAllStuff);
                    appendAnotherStuff("sticker_1mqy", textForShowAllStuff);
                    appendAnotherStuff("sticker_1mqz", textForShowAllStuff);
                    appendAnotherStuff("sticker_1mr0", textForShowAllStuff);
                    appendAnotherStuff("sticker_1mr1", textForShowAllStuff);
                    appendAnotherStuff("sticker_1nu6", textForShowAllStuff);
                    appendAnotherStuff("sticker_1nu7", textForShowAllStuff);
                    appendAnotherStuff("sticker_1nu8", textForShowAllStuff);
                    appendAnotherStuff("sticker_1nu9", textForShowAllStuff);
                    appendAnotherStuff("sticker_1nua", textForShowAllStuff);
                    appendAnotherStuff("sticker_1nub", textForShowAllStuff);
                    break;
                case 12:
                    //bureau
                    appendAnotherStuff("sticker_1lt7", textForShowAllStuff);
                    appendAnotherStuff("sticker_1lt8", textForShowAllStuff);
                    appendAnotherStuff("sticker_1lt9", textForShowAllStuff);
                    appendAnotherStuff("sticker_1lta", textForShowAllStuff);
                    appendAnotherStuff("sticker_1ltb", textForShowAllStuff);
                    appendAnotherStuff("sticker_1ltc", textForShowAllStuff);
                    appendAnotherStuff("sticker_1ltd", textForShowAllStuff);
                    appendAnotherStuff("sticker_1lte", textForShowAllStuff);
                    break;
                case 13:
                    //xmen
                    appendAnotherStuff("sticker_1mid_fr", textForShowAllStuff);
                    appendAnotherStuff("sticker_1mie_fr", textForShowAllStuff);
                    appendAnotherStuff("sticker_1mif", textForShowAllStuff);
                    appendAnotherStuff("sticker_1mig_fr", textForShowAllStuff);
                    appendAnotherStuff("sticker_1mih_fr", textForShowAllStuff);
                    appendAnotherStuff("sticker_1mii_fr", textForShowAllStuff);
                    appendAnotherStuff("sticker_1mij_fr", textForShowAllStuff);
                    appendAnotherStuff("sticker_1mik", textForShowAllStuff);
                    appendAnotherStuff("sticker_1mil", textForShowAllStuff);
                    appendAnotherStuff("sticker_1mim", textForShowAllStuff);
                    appendAnotherStuff("sticker_1min", textForShowAllStuff);
                    appendAnotherStuff("sticker_1mio", textForShowAllStuff);
                    appendAnotherStuff("sticker_1mip", textForShowAllStuff);
                    appendAnotherStuff("sticker_1miq", textForShowAllStuff);
                    appendAnotherStuff("sticker_1mir", textForShowAllStuff);
                    break;
                case 14:
                    //xbox
                    appendAnotherStuff("sticker_1my4", textForShowAllStuff);
                    appendAnotherStuff("sticker_1my5", textForShowAllStuff);
                    appendAnotherStuff("sticker_1my6", textForShowAllStuff);
                    appendAnotherStuff("sticker_1my7", textForShowAllStuff);
                    appendAnotherStuff("sticker_1my8", textForShowAllStuff);
                    appendAnotherStuff("sticker_1my9", textForShowAllStuff);
                    appendAnotherStuff("sticker_1mya", textForShowAllStuff);
                    appendAnotherStuff("sticker_1myb", textForShowAllStuff);
                    appendAnotherStuff("sticker_1myc", textForShowAllStuff);
                    appendAnotherStuff("sticker_1myd", textForShowAllStuff);
                    appendAnotherStuff("sticker_1mye", textForShowAllStuff);
                    appendAnotherStuff("sticker_1myf", textForShowAllStuff);
                    appendAnotherStuff("sticker_1myx", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n28", textForShowAllStuff);
                    break;
                case 15:
                    //foot
                    appendAnotherStuff("sticker_1n1m_de", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n1m_es", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n1m_fr", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n1m_it", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n1n_de", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n1n_es", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n1n_fr", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n1n_it", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n1o_de", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n1o_es", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n1o_fr", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n1o_it", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n1p_de", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n1p_es", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n1p_fr", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n1p_it", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n1q_de", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n1q_es", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n1q_fr", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n1q_it", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n1r_de", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n1r_es", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n1r_fr", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n1r_it", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n1s", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n1t_de", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n1t_es", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n1t_fr", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n1t_it", textForShowAllStuff);
                    break;
                case 16:
                    //store
                    appendAnotherStuff("sticker_1n2c", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n2d", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n2g", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n2h", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n2i", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n2j", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n2k", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n2l", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n2m", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n2n", textForShowAllStuff);
                    appendAnotherStuff("sticker_1n2o", textForShowAllStuff);
                    break;
                case 17:
                    //brice
                    appendAnotherStuff("sticker_1ntp", textForShowAllStuff);
                    appendAnotherStuff("sticker_1ntq", textForShowAllStuff);
                    appendAnotherStuff("sticker_1ntr", textForShowAllStuff);
                    appendAnotherStuff("sticker_1nts", textForShowAllStuff);
                    appendAnotherStuff("sticker_1ntt", textForShowAllStuff);
                    appendAnotherStuff("sticker_1ntu", textForShowAllStuff);
                    appendAnotherStuff("sticker_1ntv", textForShowAllStuff);
                    appendAnotherStuff("sticker_1ntw", textForShowAllStuff);
                    appendAnotherStuff("sticker_1ntx", textForShowAllStuff);
                    appendAnotherStuff("sticker_1nty", textForShowAllStuff);
                    appendAnotherStuff("sticker_1ntz", textForShowAllStuff);
                    appendAnotherStuff("sticker_1nu0", textForShowAllStuff);
                    break;
                case 18:
                    //pixel
                    appendAnotherStuff("sticker_1o2k", textForShowAllStuff);
                    appendAnotherStuff("sticker_1o33", textForShowAllStuff);
                    appendAnotherStuff("sticker_1o3f", textForShowAllStuff);
                    appendAnotherStuff("sticker_1o3g", textForShowAllStuff);
                    appendAnotherStuff("sticker_1o3i", textForShowAllStuff);
                    appendAnotherStuff("sticker_1o3k", textForShowAllStuff);
                    appendAnotherStuff("sticker_1o66", textForShowAllStuff);
                    appendAnotherStuff("sticker_1o67", textForShowAllStuff);
                    break;
                case 19:
                    //gym
                    appendAnotherStuff("sticker_1ptd", textForShowAllStuff);
                    appendAnotherStuff("sticker_1rob", textForShowAllStuff);
                    appendAnotherStuff("sticker_1ron", textForShowAllStuff);
                    appendAnotherStuff("sticker_1rpa", textForShowAllStuff);
                    appendAnotherStuff("sticker_1rpp", textForShowAllStuff);
                    appendAnotherStuff("sticker_1rpp_fr", textForShowAllStuff);
                    appendAnotherStuff("sticker_1rpt", textForShowAllStuff);
                    appendAnotherStuff("sticker_1rpw", textForShowAllStuff);
                    appendAnotherStuff("sticker_1rpw_fr", textForShowAllStuff);
                    appendAnotherStuff("sticker_1rpy", textForShowAllStuff);
                    appendAnotherStuff("sticker_1rpy_fr", textForShowAllStuff);
                    break;
            }
            listOfSpanForTextView[row] = Undeprecator.htmlFromHtml(textForShowAllStuff.toString(), withThisImageGetter, null);
        }
    }

    private void appendAnotherStuff(String newStuffToAppend, StringBuilder toThisStringBuilder) {
        toThisStringBuilder.append("<a href=\"").append(newStuffToAppend).append("\"><img src=\"").append(newStuffToAppend).append(".png\"/></a> ");
    }

    private void sendWhichInsertIsNeeded(String thisUrl, StuffInserted toThisListener) {
        if (thisUrl.startsWith("textformat_bold")) {
            toThisListener.getStringInserted("\'\'\'\'\'\'", 3);
        } else if (thisUrl.startsWith("textformat_italic")) {
            toThisListener.getStringInserted("\'\'\'\'", 2);
        } else if (thisUrl.startsWith("textformat_underline")) {
            toThisListener.getStringInserted("<u></u>", 4);
        } else if (thisUrl.startsWith("textformat_strike")) {
            toThisListener.getStringInserted("<s></s>", 4);
        } else if (thisUrl.startsWith("textformat_ulist")) {
            toThisListener.getStringInserted("* ", 0);
        } else if (thisUrl.startsWith("textformat_olist")) {
            toThisListener.getStringInserted("# ", 0);
        } else if (thisUrl.startsWith("textformat_quote")) {
            toThisListener.getStringInserted("> ", 0);
        } else if (thisUrl.startsWith("textformat_code")) {
            toThisListener.getStringInserted("<code></code>", 7);
        } else if (thisUrl.startsWith("textformat_spoil")) {
            toThisListener.getStringInserted("<spoil></spoil>", 8);
        } else {
            toThisListener.getStringInserted("", 0);
        }
    }

    private Spannable replaceUrlSpans(Spanned spanToChange) {
        Spannable spannable = new SpannableString(spanToChange);
        URLSpan[] urlSpanArray = spannable.getSpans(0, spannable.length(), URLSpan.class);
        for (final URLSpan urlSpan : urlSpanArray) {
            Utils.replaceSpanByAnotherSpan(spannable, urlSpan, new LongClickableSpan() {
                private String url = urlSpan.getURL();

                @Override
                public void onLongClick(View view) {
                    if (getActivity() instanceof StuffInserted) {
                        if (url.startsWith("sticker_")) {
                            String newSticker = "[[sticker:p/" + url.replace("sticker_", "").replace("_", "-") + "]]";
                            ((StuffInserted) getActivity()).getStringInserted(newSticker, 0);
                        } else {
                            sendWhichInsertIsNeeded(url, (StuffInserted) getActivity());
                        }
                    }
                }

                @Override
                public void onClick(View view) {
                    onLongClick(view);
                    dismiss();
                }
            });
        }
        return spannable;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        Drawable deletedDrawable = Undeprecator.resourcesGetDrawable(getActivity().getResources(), R.drawable.image_deleted);
        deletedDrawable.setBounds(0, 0, deletedDrawable.getIntrinsicWidth(), deletedDrawable.getIntrinsicHeight());

        jvcImageGetter = new CustomImageGetter(getActivity(), deletedDrawable, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View mainView = getActivity().getLayoutInflater().inflate(R.layout.dialog_insertstuff, null);
        mainTextView = (TextView) mainView.findViewById(R.id.showstuff_text_insertstuff);
        listOfCategoryButtons[0] = (ImageView) mainView.findViewById(R.id.textformat_button_insertstuff);
        listOfCategoryButtons[1] = (ImageView) mainView.findViewById(R.id.sticker_1_button_insertstuff);
        listOfCategoryButtons[2] = (ImageView) mainView.findViewById(R.id.sticker_2_button_insertstuff);
        listOfCategoryButtons[3] = (ImageView) mainView.findViewById(R.id.sticker_3_button_insertstuff);
        listOfCategoryButtons[4] = (ImageView) mainView.findViewById(R.id.sticker_4_button_insertstuff);
        listOfCategoryButtons[5] = (ImageView) mainView.findViewById(R.id.sticker_5_button_insertstuff);
        listOfCategoryButtons[6] = (ImageView) mainView.findViewById(R.id.sticker_6_button_insertstuff);
        listOfCategoryButtons[7] = (ImageView) mainView.findViewById(R.id.sticker_7_button_insertstuff);
        listOfCategoryButtons[8] = (ImageView) mainView.findViewById(R.id.sticker_8_button_insertstuff);
        listOfCategoryButtons[9] = (ImageView) mainView.findViewById(R.id.sticker_9_button_insertstuff);
        listOfCategoryButtons[10] = (ImageView) mainView.findViewById(R.id.sticker_10_button_insertstuff);
        listOfCategoryButtons[11] = (ImageView) mainView.findViewById(R.id.sticker_11_button_insertstuff);
        listOfCategoryButtons[12] = (ImageView) mainView.findViewById(R.id.sticker_12_button_insertstuff);
        listOfCategoryButtons[13] = (ImageView) mainView.findViewById(R.id.sticker_13_button_insertstuff);
        listOfCategoryButtons[14] = (ImageView) mainView.findViewById(R.id.sticker_14_button_insertstuff);
        listOfCategoryButtons[15] = (ImageView) mainView.findViewById(R.id.sticker_15_button_insertstuff);
        listOfCategoryButtons[16] = (ImageView) mainView.findViewById(R.id.sticker_16_button_insertstuff);
        listOfCategoryButtons[17] = (ImageView) mainView.findViewById(R.id.sticker_17_button_insertstuff);
        listOfCategoryButtons[18] = (ImageView) mainView.findViewById(R.id.sticker_18_button_insertstuff);
        listOfCategoryButtons[19] = (ImageView) mainView.findViewById(R.id.sticker_19_button_insertstuff);

        for (int i = 0; i < MAX_NUMBER_OF_ROW; ++i) {
            listOfCategoryButtons[i].setTag(i);
            listOfCategoryButtons[i].setOnClickListener(categoryButtonClickedListener);
        }

        mainTextView.setMovementMethod(LongClickLinkMovementMethod.getInstance());
        selectThisRow(oldRowNumber);
        builder.setTitle(R.string.insertStuff).setView(mainView)
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
        return builder.create();
    }

    @Override
    public void onPause() {
        dismiss();
        super.onPause();
    }

    public interface StuffInserted {
        void getStringInserted(String newStringToAdd, int posOfCenterFromEnd);
    }
}
