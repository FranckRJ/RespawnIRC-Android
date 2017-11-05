package com.franckrj.respawnirc.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.util.SimpleArrayMap;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.utils.PrefsManager;
import com.franckrj.respawnirc.utils.ThemeManager;
import com.franckrj.respawnirc.utils.CustomImageGetter;
import com.franckrj.respawnirc.utils.LongClickLinkMovementMethod;
import com.franckrj.respawnirc.utils.LongClickableSpan;
import com.franckrj.respawnirc.utils.Undeprecator;
import com.franckrj.respawnirc.utils.Utils;

import java.util.ArrayList;

public class InsertStuffDialogFragment extends DialogFragment {
    private static Spanned[] listOfSpanForTextView = null;
    private static SimpleArrayMap<String, String> listOfSmileyNamesForFileName = null;
    private static boolean textDecorationRowGeneratedForDarkTheme = false;

    private TextView mainTextView = null;
    private ScrollView scrollViewOfButtons = null;
    private Html.ImageGetter jvcImageGetter = null;
    private ArrayList<ImageView> listOfCategoryButtons = new ArrayList<>();
    private int oldRowNumber = 1;

    private final View.OnClickListener categoryButtonClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int newRowNumber = 0;
            try {
                newRowNumber = (int) v.getTag();
                if (newRowNumber < 0) {
                    newRowNumber = 0;
                } else if (newRowNumber >= listOfCategoryButtons.size()) {
                    newRowNumber = listOfCategoryButtons.size() - 1;
                }
            } catch (Exception e) {
                //rien
            }
            selectThisRow(newRowNumber);
        }
    };

    private void selectThisRow(int rowToUse) {
        listOfCategoryButtons.get(oldRowNumber).setBackgroundColor(Undeprecator.resourcesGetColor(getResources(), android.R.color.transparent));
        listOfCategoryButtons.get(rowToUse).setBackgroundColor(Undeprecator.resourcesGetColor(getResources(), ThemeManager.getColorRes(ThemeManager.ColorName.DARKER_BACKGROUND_COLOR)));
        initializeSpanForTextViewIfNeeded(jvcImageGetter, rowToUse);
        mainTextView.setText(replaceUrlSpans(listOfSpanForTextView[rowToUse]));
        scrollViewOfButtons.requestChildFocus(listOfCategoryButtons.get(rowToUse), listOfCategoryButtons.get(rowToUse));
        oldRowNumber = rowToUse;
        PrefsManager.putInt(PrefsManager.IntPref.Names.LAST_ROW_SELECTED_INSERTSTUFF, oldRowNumber);
        PrefsManager.applyChanges();
    }

    private void initializeListOfSmileyName() {
        if (listOfSmileyNamesForFileName == null) {
            listOfSmileyNamesForFileName = new SimpleArrayMap<>();
            listOfSmileyNamesForFileName.put("1", ":)");
            listOfSmileyNamesForFileName.put("2", ":question:");
            listOfSmileyNamesForFileName.put("3", ":g)");
            listOfSmileyNamesForFileName.put("4", ":d)");
            listOfSmileyNamesForFileName.put("5", ":cd:");
            listOfSmileyNamesForFileName.put("6", ":globe:");
            listOfSmileyNamesForFileName.put("7", ":p)");
            listOfSmileyNamesForFileName.put("8", ":malade:");
            listOfSmileyNamesForFileName.put("9", ":pacg:");
            listOfSmileyNamesForFileName.put("10", ":pacd:");
            listOfSmileyNamesForFileName.put("11", ":noel:");
            listOfSmileyNamesForFileName.put("12", ":o))");
            listOfSmileyNamesForFileName.put("13", ":snif2:");
            listOfSmileyNamesForFileName.put("14", ":-(");
            listOfSmileyNamesForFileName.put("15", ":-((");
            listOfSmileyNamesForFileName.put("16", ":mac:");
            listOfSmileyNamesForFileName.put("17", ":gba:");
            listOfSmileyNamesForFileName.put("18", ":hap:");
            listOfSmileyNamesForFileName.put("19", ":nah:");
            listOfSmileyNamesForFileName.put("20", ":snif:");
            listOfSmileyNamesForFileName.put("21", ":mort:");
            listOfSmileyNamesForFileName.put("22", ":ouch:");
            listOfSmileyNamesForFileName.put("23", ":-)))");
            listOfSmileyNamesForFileName.put("24", ":content:");
            listOfSmileyNamesForFileName.put("25", ":nonnon:");
            listOfSmileyNamesForFileName.put("26", ":cool:");
            listOfSmileyNamesForFileName.put("27", ":sleep:");
            listOfSmileyNamesForFileName.put("28", ":doute:");
            listOfSmileyNamesForFileName.put("29", ":hello:");
            listOfSmileyNamesForFileName.put("30", ":honte:");
            listOfSmileyNamesForFileName.put("31", ":-p");
            listOfSmileyNamesForFileName.put("32", ":lol:");
            listOfSmileyNamesForFileName.put("33", ":non2:");
            listOfSmileyNamesForFileName.put("34", ":monoeil:");
            listOfSmileyNamesForFileName.put("35", ":non:");
            listOfSmileyNamesForFileName.put("36", ":ok:");
            listOfSmileyNamesForFileName.put("37", ":oui:");
            listOfSmileyNamesForFileName.put("38", ":rechercher:");
            listOfSmileyNamesForFileName.put("39", ":rire:");
            listOfSmileyNamesForFileName.put("40", ":-D");
            listOfSmileyNamesForFileName.put("41", ":rire2:");
            listOfSmileyNamesForFileName.put("42", ":salut:");
            listOfSmileyNamesForFileName.put("43", ":sarcastic:");
            listOfSmileyNamesForFileName.put("44", ":up:");
            listOfSmileyNamesForFileName.put("45", ":(");
            listOfSmileyNamesForFileName.put("46", ":-)");
            listOfSmileyNamesForFileName.put("47", ":peur:");
            listOfSmileyNamesForFileName.put("48", ":bye:");
            listOfSmileyNamesForFileName.put("49", ":dpdr:");
            listOfSmileyNamesForFileName.put("50", ":fou:");
            listOfSmileyNamesForFileName.put("51", ":gne:");
            listOfSmileyNamesForFileName.put("52", ":dehors:");
            listOfSmileyNamesForFileName.put("53", ":fier:");
            listOfSmileyNamesForFileName.put("54", ":coeur:");
            listOfSmileyNamesForFileName.put("55", ":rouge:");
            listOfSmileyNamesForFileName.put("56", ":sors:");
            listOfSmileyNamesForFileName.put("57", ":ouch2:");
            listOfSmileyNamesForFileName.put("58", ":merci:");
            listOfSmileyNamesForFileName.put("59", ":svp:");
            listOfSmileyNamesForFileName.put("60", ":ange:");
            listOfSmileyNamesForFileName.put("61", ":diable:");
            listOfSmileyNamesForFileName.put("62", ":gni:");
            listOfSmileyNamesForFileName.put("63", ":spoiler:");
            listOfSmileyNamesForFileName.put("64", ":hs:");
            listOfSmileyNamesForFileName.put("65", ":desole:");
            listOfSmileyNamesForFileName.put("66", ":fete:");
            listOfSmileyNamesForFileName.put("67", ":sournois:");
            listOfSmileyNamesForFileName.put("68", ":hum:");
            listOfSmileyNamesForFileName.put("69", ":bravo:");
            listOfSmileyNamesForFileName.put("70", ":banzai:");
            listOfSmileyNamesForFileName.put("71", ":bave:");
            listOfSmileyNamesForFileName.put("nyu", ":cute:");
        }
    }

    private void initializeSpanForTextViewIfNeeded(Html.ImageGetter withThisImageGetter, int row) {
        if (listOfSpanForTextView == null) {
            listOfSpanForTextView = new Spanned[listOfCategoryButtons.size()];
            for (int i = 0; i < listOfCategoryButtons.size(); ++i) {
                listOfSpanForTextView[i] = null;
            }
        }
        if (listOfSpanForTextView[row] == null || (row == 1 && textDecorationRowGeneratedForDarkTheme != ThemeManager.getThemeUsedIsDark())) {
            StringBuilder textForShowAllStuff = new StringBuilder();
            switch (row) {
                case 0:
                    //smiley
                    initializeListOfSmileyName();
                    for (int i = 1; i <= 71; ++i ) {
                        appendAnotherStuff("smiley_" + String.valueOf(i), textForShowAllStuff, true);
                    }
                    appendAnotherStuff("smiley_cimer", textForShowAllStuff, true);
                    appendAnotherStuff("smiley_ddb", textForShowAllStuff, true);
                    appendAnotherStuff("smiley_fish", textForShowAllStuff, true);
                    appendAnotherStuff("smiley_hapoelparty", textForShowAllStuff, true);
                    appendAnotherStuff("smiley_loveyou", textForShowAllStuff, true);
                    appendAnotherStuff("smiley_nyu", textForShowAllStuff, true);
                    appendAnotherStuff("smiley_objection", textForShowAllStuff, true);
                    appendAnotherStuff("smiley_pf", textForShowAllStuff, true);
                    appendAnotherStuff("smiley_play", textForShowAllStuff, true);
                    appendAnotherStuff("smiley_siffle", textForShowAllStuff, true);
                    break;
                case 1:
                    //textformat
                    textDecorationRowGeneratedForDarkTheme = ThemeManager.getThemeUsedIsDark();
                    if (textDecorationRowGeneratedForDarkTheme) {
                        appendAnotherStuff("textformat_bold_dark", textForShowAllStuff, false);
                        appendAnotherStuff("textformat_italic_dark", textForShowAllStuff, false);
                        appendAnotherStuff("textformat_underline_dark", textForShowAllStuff, false);
                        appendAnotherStuff("textformat_strike_dark", textForShowAllStuff, false);
                        appendAnotherStuff("textformat_ulist_dark", textForShowAllStuff, false);
                        appendAnotherStuff("textformat_olist_dark", textForShowAllStuff, false);
                        appendAnotherStuff("textformat_quote_dark", textForShowAllStuff, false);
                        appendAnotherStuff("textformat_code_dark", textForShowAllStuff, false);
                        appendAnotherStuff("textformat_spoil_dark", textForShowAllStuff, false);
                    } else {
                        appendAnotherStuff("textformat_bold_light", textForShowAllStuff, false);
                        appendAnotherStuff("textformat_italic_light", textForShowAllStuff, false);
                        appendAnotherStuff("textformat_underline_light", textForShowAllStuff, false);
                        appendAnotherStuff("textformat_strike_light", textForShowAllStuff, false);
                        appendAnotherStuff("textformat_ulist_light", textForShowAllStuff, false);
                        appendAnotherStuff("textformat_olist_light", textForShowAllStuff, false);
                        appendAnotherStuff("textformat_quote_light", textForShowAllStuff, false);
                        appendAnotherStuff("textformat_code_light", textForShowAllStuff, false);
                        appendAnotherStuff("textformat_spoil_light", textForShowAllStuff, false);
                    }
                    break;
                case 2:
                    //ours
                    appendAnotherStuff("sticker_1f88", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1f89", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1f8a", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1f8b", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1f8c", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1f8d", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1f8e", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1f8f", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_zu2", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_zu6", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_zu7", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_zu8", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_zu9", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_zua", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_zub", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_zuc_fr", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_zuc_en", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_zuc_es", textForShowAllStuff, false);
                    break;
                case 3:
                    //bourge
                    appendAnotherStuff("sticker_1jnc", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1jnd", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1jne", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1jnf", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1jng", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1jnh", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1jni", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1jnj", textForShowAllStuff, false);
                    break;
                case 4:
                    //lama
                    appendAnotherStuff("sticker_1kgu", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1kgv", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1kgw", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1kgx", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1kgy", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1kgz", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1kh0", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1kh1", textForShowAllStuff, false);
                    break;
                case 5:
                    //hap
                    appendAnotherStuff("sticker_1kkg", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1kkh", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1kki", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1kkj", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1kkk", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1kkl", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1kkm", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1kkn", textForShowAllStuff, false);
                    break;
                case 6:
                    //noel
                    appendAnotherStuff("sticker_1kko", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1kkp", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1kkq", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1kkr", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1kks", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1kkt", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1kku", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1kkv", textForShowAllStuff, false);
                    break;
                case 7:
                    //chat
                    appendAnotherStuff("sticker_1kky", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1kkz", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1kl0", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1kl1", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1kl2", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1kl3", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1kl4", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1kl5", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1kl6", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1kl7", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1kl8", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1kl9", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1kla", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1klb", textForShowAllStuff, false);
                    break;
                case 8:
                    //orc
                    appendAnotherStuff("sticker_1lga", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1lgb", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1lgc", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1lgd", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1lge", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1lgf", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1lgg", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1lgh", textForShowAllStuff, false);
                    break;
                case 9:
                    //dom
                    appendAnotherStuff("sticker_1ljj", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1ljl", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1ljm", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1ljn", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1ljo", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1ljp", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1ljq", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1ljr", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1rzs", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1rzt", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1rzu", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1rzv", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1rzw", textForShowAllStuff, false);
                    break;
                case 10:
                    //aventurier
                    appendAnotherStuff("sticker_1lm9", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1lma", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1lmb", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1lmc", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1lmd", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1lme", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1lmf", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1lmg", textForShowAllStuff, false);
                    break;
                case 11:
                    //saumon
                    appendAnotherStuff("sticker_1lmh", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1lmi", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1lmj", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1lmk", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1lml", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1lmm", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1lmn", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1lmo", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1lmp", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1mqv", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1mqw", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1mqx", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1mqy", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1mqz", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1mr0", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1mr1", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1nu6", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1nu7", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1nu8", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1nu9", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1nua", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1nub", textForShowAllStuff, false);
                    break;
                case 12:
                    //bureau
                    appendAnotherStuff("sticker_1lt7", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1lt8", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1lt9", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1lta", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1ltb", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1ltc", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1ltd", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1lte", textForShowAllStuff, false);
                    break;
                case 13:
                    //foot
                    appendAnotherStuff("sticker_1n1m_de", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n1m_es", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n1m_fr", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n1m_it", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n1n_de", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n1n_es", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n1n_fr", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n1n_it", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n1o_de", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n1o_es", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n1o_fr", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n1o_it", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n1p_de", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n1p_es", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n1p_fr", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n1p_it", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n1q_de", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n1q_es", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n1q_fr", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n1q_it", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n1r_de", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n1r_es", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n1r_fr", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n1r_it", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n1s", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n1t_de", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n1t_es", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n1t_fr", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n1t_it", textForShowAllStuff, false);
                    break;
                case 14:
                    //store
                    appendAnotherStuff("sticker_1n2c", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n2d", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n2g", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n2h", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n2i", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n2j", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n2k", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n2l", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n2m", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n2n", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1n2o", textForShowAllStuff, false);
                    break;
                case 15:
                    //pixel
                    appendAnotherStuff("sticker_1o2k", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1o33", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1o3f", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1o3g", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1o3i", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1o3k", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1o66", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1o67", textForShowAllStuff, false);
                    break;
                case 16:
                    //gym
                    appendAnotherStuff("sticker_1ptd", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1rob", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1ron", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1rpa", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1rpp", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1rpp_fr", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1rpt", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1rpw", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1rpw_fr", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1rpy", textForShowAllStuff, false);
                    appendAnotherStuff("sticker_1rpy_fr", textForShowAllStuff, false);
                    break;
            }
            listOfSpanForTextView[row] = Undeprecator.htmlFromHtml(textForShowAllStuff.toString(), withThisImageGetter, null);
        }
    }

    private void appendAnotherStuff(String newStuffToAppend, StringBuilder toThisStringBuilder, boolean previewHasToBeBig) {
        toThisStringBuilder.append("<a href=\"").append(newStuffToAppend).append("\"><img src=\"");
        if (previewHasToBeBig) {
            toThisStringBuilder.append("big-");
        }
        toThisStringBuilder.append(newStuffToAppend).append(".png\"/></a>&nbsp;");
        if (previewHasToBeBig) {
            toThisStringBuilder.append("&nbsp;");
        }
    }

    private void sendWhichInsertIsNeeded(String thisUrl, StuffInserted toThisListener) {
        if (thisUrl.startsWith("sticker_")) {
            String newSticker = "[[sticker:p/" + thisUrl.replace("sticker_", "").replace("_", "-") + "]]";
            toThisListener.getStringInserted(newSticker, 0);
        } else if (thisUrl.startsWith("smiley_")) {
            if (listOfSmileyNamesForFileName != null) {
                String smileyShortFileName = thisUrl.substring(("smiley_").length());
                String nameOfSmiley = listOfSmileyNamesForFileName.get(smileyShortFileName);
                if (nameOfSmiley != null) {
                    toThisListener.getStringInserted(nameOfSmiley, 0);
                } else {
                    toThisListener.getStringInserted(":" + smileyShortFileName + ":", 0);
                }
            }
        } else if (thisUrl.startsWith("textformat_")) {
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
            }
        }
    }

    private Spannable replaceUrlSpans(Spanned spanToChange) {
        Spannable spannable = new SpannableString(spanToChange);
        URLSpan[] urlSpanArray = spannable.getSpans(0, spannable.length(), URLSpan.class);
        for (final URLSpan urlSpan : urlSpanArray) {
            Utils.replaceSpanByAnotherSpan(spannable, urlSpan, new LongClickableSpan() {
                @Override
                public void onLongClick(View view) {
                    if (getActivity() instanceof StuffInserted) {
                        sendWhichInsertIsNeeded(urlSpan.getURL(), (StuffInserted) getActivity());
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

        Drawable deletedDrawable = Undeprecator.resourcesGetDrawable(getActivity().getResources(), ThemeManager.getDrawableRes(ThemeManager.DrawableName.DELETED_IMAGE));
        deletedDrawable.setBounds(0, 0, deletedDrawable.getIntrinsicWidth(), deletedDrawable.getIntrinsicHeight());

        jvcImageGetter = new CustomImageGetter(getActivity(), deletedDrawable, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        @SuppressLint("InflateParams")
        View mainView = getActivity().getLayoutInflater().inflate(R.layout.dialog_insertstuff, null);
        scrollViewOfButtons = mainView.findViewById(R.id.list_scrollview_insertstuff);
        mainTextView = mainView.findViewById(R.id.showstuff_text_insertstuff);
        listOfCategoryButtons.add((ImageView) mainView.findViewById(R.id.smiley_button_insertstuff));
        listOfCategoryButtons.add((ImageView) mainView.findViewById(R.id.textformat_button_insertstuff));
        listOfCategoryButtons.add((ImageView) mainView.findViewById(R.id.sticker_1_button_insertstuff));
        listOfCategoryButtons.add((ImageView) mainView.findViewById(R.id.sticker_2_button_insertstuff));
        listOfCategoryButtons.add((ImageView) mainView.findViewById(R.id.sticker_3_button_insertstuff));
        listOfCategoryButtons.add((ImageView) mainView.findViewById(R.id.sticker_4_button_insertstuff));
        listOfCategoryButtons.add((ImageView) mainView.findViewById(R.id.sticker_5_button_insertstuff));
        listOfCategoryButtons.add((ImageView) mainView.findViewById(R.id.sticker_6_button_insertstuff));
        listOfCategoryButtons.add((ImageView) mainView.findViewById(R.id.sticker_7_button_insertstuff));
        listOfCategoryButtons.add((ImageView) mainView.findViewById(R.id.sticker_8_button_insertstuff));
        listOfCategoryButtons.add((ImageView) mainView.findViewById(R.id.sticker_9_button_insertstuff));
        listOfCategoryButtons.add((ImageView) mainView.findViewById(R.id.sticker_10_button_insertstuff));
        listOfCategoryButtons.add((ImageView) mainView.findViewById(R.id.sticker_11_button_insertstuff));
        listOfCategoryButtons.add((ImageView) mainView.findViewById(R.id.sticker_12_button_insertstuff));
        listOfCategoryButtons.add((ImageView) mainView.findViewById(R.id.sticker_13_button_insertstuff));
        listOfCategoryButtons.add((ImageView) mainView.findViewById(R.id.sticker_14_button_insertstuff));
        listOfCategoryButtons.add((ImageView) mainView.findViewById(R.id.sticker_15_button_insertstuff));

        for (int i = 0; i < listOfCategoryButtons.size(); ++i) {
            listOfCategoryButtons.get(i).setTag(i);
            listOfCategoryButtons.get(i).setOnClickListener(categoryButtonClickedListener);
        }

        if (PrefsManager.getBool(PrefsManager.BoolPref.Names.SAVE_LAST_ROW_USED_INSERTSTUFF)) {
            oldRowNumber = PrefsManager.getInt(PrefsManager.IntPref.Names.LAST_ROW_SELECTED_INSERTSTUFF);
        }

        if (oldRowNumber >= listOfCategoryButtons.size()) {
            oldRowNumber = listOfCategoryButtons.size() - 1;
        } else if (oldRowNumber < 0) {
            oldRowNumber = 0;
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
