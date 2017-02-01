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
import com.franckrj.respawnirc.utils.CustomImageGetter;
import com.franckrj.respawnirc.utils.LongClickLinkMovementMethod;
import com.franckrj.respawnirc.utils.LongClickableSpan;
import com.franckrj.respawnirc.utils.Undeprecator;
import com.franckrj.respawnirc.utils.Utils;

public class SelectStickerDialogFragment extends DialogFragment {
    private static final int MAX_NUMBER_OF_ROW = 16;
    private static Spanned[] listOfSpanForTextView = null;

    private TextView mainTextView = null;
    private Html.ImageGetter jvcImageGetter = null;
    private ImageView[] listOfStickersButtons = new ImageView[MAX_NUMBER_OF_ROW];
    private int oldRowNumber = 0;

    private final View.OnClickListener stickerButtonClickedListener = new View.OnClickListener() {
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
                e.printStackTrace();
            }
            selectThisStickerRow(newRowNumber);
        }
    };

    private void selectThisStickerRow(int rowToUse) {
        listOfStickersButtons[oldRowNumber].setBackgroundColor(Undeprecator.resourcesGetColor(getResources(), android.R.color.transparent));
        listOfStickersButtons[rowToUse].setBackgroundColor(Undeprecator.resourcesGetColor(getResources(), R.color.moreDarkerColorForBackground));
        initializeSpanForTextViewIfNeeded(jvcImageGetter, rowToUse);
        mainTextView.setText(replaceUrlSpans(listOfSpanForTextView[rowToUse]));
        oldRowNumber = rowToUse;
    }

    private void initializeSpanForTextViewIfNeeded(Html.ImageGetter withThisImageGetter, int row) {
        if (listOfSpanForTextView == null) {
            listOfSpanForTextView = new Spanned[MAX_NUMBER_OF_ROW];
            for (int i = 0; i < listOfSpanForTextView.length; ++i) {
                listOfSpanForTextView[i] = null;
            }
        }
        if (listOfSpanForTextView[row] == null)
        {
            StringBuilder textForShowAllStickers = new StringBuilder();
            switch (row) {
                case 0:
                    //ours
                    appendAnotherSticker("sticker_1f88", textForShowAllStickers);
                    appendAnotherSticker("sticker_1f89", textForShowAllStickers);
                    appendAnotherSticker("sticker_1f8a", textForShowAllStickers);
                    appendAnotherSticker("sticker_1f8b", textForShowAllStickers);
                    appendAnotherSticker("sticker_1f8c", textForShowAllStickers);
                    appendAnotherSticker("sticker_1f8d", textForShowAllStickers);
                    appendAnotherSticker("sticker_1f8e", textForShowAllStickers);
                    appendAnotherSticker("sticker_1f8f", textForShowAllStickers);
                    appendAnotherSticker("sticker_zu2", textForShowAllStickers);
                    appendAnotherSticker("sticker_zu6", textForShowAllStickers);
                    appendAnotherSticker("sticker_zu7", textForShowAllStickers);
                    appendAnotherSticker("sticker_zu8", textForShowAllStickers);
                    appendAnotherSticker("sticker_zu9", textForShowAllStickers);
                    appendAnotherSticker("sticker_zua", textForShowAllStickers);
                    appendAnotherSticker("sticker_zub", textForShowAllStickers);
                    appendAnotherSticker("sticker_zuc", textForShowAllStickers);
                    break;
                case 1:
                    //lapin
                    appendAnotherSticker("sticker_1jc3_en", textForShowAllStickers);
                    appendAnotherSticker("sticker_1jc3_fr", textForShowAllStickers);
                    appendAnotherSticker("sticker_1jc5", textForShowAllStickers);
                    appendAnotherSticker("sticker_1jcg", textForShowAllStickers);
                    appendAnotherSticker("sticker_1jch", textForShowAllStickers);
                    appendAnotherSticker("sticker_1jcl", textForShowAllStickers);
                    appendAnotherSticker("sticker_1leb", textForShowAllStickers);
                    appendAnotherSticker("sticker_1lej_en", textForShowAllStickers);
                    appendAnotherSticker("sticker_1lej_fr", textForShowAllStickers);
                    appendAnotherSticker("sticker_1leq_en", textForShowAllStickers);
                    appendAnotherSticker("sticker_1leq_fr", textForShowAllStickers);
                    appendAnotherSticker("sticker_1li3", textForShowAllStickers);
                    appendAnotherSticker("sticker_1li4", textForShowAllStickers);
                    appendAnotherSticker("sticker_1li5", textForShowAllStickers);
                    break;
                case 2:
                    //bourge
                    appendAnotherSticker("sticker_1jnc", textForShowAllStickers);
                    appendAnotherSticker("sticker_1jnd", textForShowAllStickers);
                    appendAnotherSticker("sticker_1jne", textForShowAllStickers);
                    appendAnotherSticker("sticker_1jnf", textForShowAllStickers);
                    appendAnotherSticker("sticker_1jng", textForShowAllStickers);
                    appendAnotherSticker("sticker_1jnh", textForShowAllStickers);
                    appendAnotherSticker("sticker_1jni", textForShowAllStickers);
                    appendAnotherSticker("sticker_1jnj", textForShowAllStickers);
                    break;
                case 3:
                    //lama
                    appendAnotherSticker("sticker_1kgu", textForShowAllStickers);
                    appendAnotherSticker("sticker_1kgv", textForShowAllStickers);
                    appendAnotherSticker("sticker_1kgw", textForShowAllStickers);
                    appendAnotherSticker("sticker_1kgx", textForShowAllStickers);
                    appendAnotherSticker("sticker_1kgy", textForShowAllStickers);
                    appendAnotherSticker("sticker_1kgz", textForShowAllStickers);
                    appendAnotherSticker("sticker_1kh0", textForShowAllStickers);
                    appendAnotherSticker("sticker_1kh1", textForShowAllStickers);
                    break;
                case 4:
                    //hap
                    appendAnotherSticker("sticker_1kkg", textForShowAllStickers);
                    appendAnotherSticker("sticker_1kkh", textForShowAllStickers);
                    appendAnotherSticker("sticker_1kki", textForShowAllStickers);
                    appendAnotherSticker("sticker_1kkj", textForShowAllStickers);
                    appendAnotherSticker("sticker_1kkk", textForShowAllStickers);
                    appendAnotherSticker("sticker_1kkl", textForShowAllStickers);
                    appendAnotherSticker("sticker_1kkm", textForShowAllStickers);
                    appendAnotherSticker("sticker_1kkn", textForShowAllStickers);
                    break;
                case 5:
                    //noel
                    appendAnotherSticker("sticker_1kko", textForShowAllStickers);
                    appendAnotherSticker("sticker_1kkp", textForShowAllStickers);
                    appendAnotherSticker("sticker_1kkq", textForShowAllStickers);
                    appendAnotherSticker("sticker_1kkr", textForShowAllStickers);
                    appendAnotherSticker("sticker_1kks", textForShowAllStickers);
                    appendAnotherSticker("sticker_1kkt", textForShowAllStickers);
                    appendAnotherSticker("sticker_1kku", textForShowAllStickers);
                    appendAnotherSticker("sticker_1kkv", textForShowAllStickers);
                    break;
                case 6:
                    //chat
                    appendAnotherSticker("sticker_1kky", textForShowAllStickers);
                    appendAnotherSticker("sticker_1kkz", textForShowAllStickers);
                    appendAnotherSticker("sticker_1kl0", textForShowAllStickers);
                    appendAnotherSticker("sticker_1kl1", textForShowAllStickers);
                    appendAnotherSticker("sticker_1kl2", textForShowAllStickers);
                    appendAnotherSticker("sticker_1kl3", textForShowAllStickers);
                    appendAnotherSticker("sticker_1kl4", textForShowAllStickers);
                    appendAnotherSticker("sticker_1kl5", textForShowAllStickers);
                    appendAnotherSticker("sticker_1kl6", textForShowAllStickers);
                    appendAnotherSticker("sticker_1kl7", textForShowAllStickers);
                    appendAnotherSticker("sticker_1kl8", textForShowAllStickers);
                    appendAnotherSticker("sticker_1kl9", textForShowAllStickers);
                    appendAnotherSticker("sticker_1kla", textForShowAllStickers);
                    appendAnotherSticker("sticker_1klb", textForShowAllStickers);
                    break;
                case 7:
                    //orc
                    appendAnotherSticker("sticker_1lga", textForShowAllStickers);
                    appendAnotherSticker("sticker_1lgb", textForShowAllStickers);
                    appendAnotherSticker("sticker_1lgc", textForShowAllStickers);
                    appendAnotherSticker("sticker_1lgd", textForShowAllStickers);
                    appendAnotherSticker("sticker_1lge", textForShowAllStickers);
                    appendAnotherSticker("sticker_1lgf", textForShowAllStickers);
                    appendAnotherSticker("sticker_1lgg", textForShowAllStickers);
                    appendAnotherSticker("sticker_1lgh", textForShowAllStickers);
                    break;
                case 8:
                    //dom
                    appendAnotherSticker("sticker_1ljj", textForShowAllStickers);
                    appendAnotherSticker("sticker_1ljl", textForShowAllStickers);
                    appendAnotherSticker("sticker_1ljm", textForShowAllStickers);
                    appendAnotherSticker("sticker_1ljn", textForShowAllStickers);
                    appendAnotherSticker("sticker_1ljo", textForShowAllStickers);
                    appendAnotherSticker("sticker_1ljp", textForShowAllStickers);
                    appendAnotherSticker("sticker_1ljq", textForShowAllStickers);
                    appendAnotherSticker("sticker_1ljr", textForShowAllStickers);
                    appendAnotherSticker("sticker_1rzs", textForShowAllStickers);
                    appendAnotherSticker("sticker_1rzt", textForShowAllStickers);
                    appendAnotherSticker("sticker_1rzu", textForShowAllStickers);
                    appendAnotherSticker("sticker_1rzv", textForShowAllStickers);
                    appendAnotherSticker("sticker_1rzw", textForShowAllStickers);
                    break;
                case 9:
                    //aventurier
                    appendAnotherSticker("sticker_1lm9", textForShowAllStickers);
                    appendAnotherSticker("sticker_1lma", textForShowAllStickers);
                    appendAnotherSticker("sticker_1lmb", textForShowAllStickers);
                    appendAnotherSticker("sticker_1lmc", textForShowAllStickers);
                    appendAnotherSticker("sticker_1lmd", textForShowAllStickers);
                    appendAnotherSticker("sticker_1lme", textForShowAllStickers);
                    appendAnotherSticker("sticker_1lmf", textForShowAllStickers);
                    appendAnotherSticker("sticker_1lmg", textForShowAllStickers);
                    break;
                case 10:
                    //saumon
                    appendAnotherSticker("sticker_1lmh", textForShowAllStickers);
                    appendAnotherSticker("sticker_1lmi", textForShowAllStickers);
                    appendAnotherSticker("sticker_1lmj", textForShowAllStickers);
                    appendAnotherSticker("sticker_1lmk", textForShowAllStickers);
                    appendAnotherSticker("sticker_1lml", textForShowAllStickers);
                    appendAnotherSticker("sticker_1lmm", textForShowAllStickers);
                    appendAnotherSticker("sticker_1lmn", textForShowAllStickers);
                    appendAnotherSticker("sticker_1lmo", textForShowAllStickers);
                    appendAnotherSticker("sticker_1lmp", textForShowAllStickers);
                    appendAnotherSticker("sticker_1mqv", textForShowAllStickers);
                    appendAnotherSticker("sticker_1mqw", textForShowAllStickers);
                    appendAnotherSticker("sticker_1mqx", textForShowAllStickers);
                    appendAnotherSticker("sticker_1mqy", textForShowAllStickers);
                    appendAnotherSticker("sticker_1mqz", textForShowAllStickers);
                    appendAnotherSticker("sticker_1mr0", textForShowAllStickers);
                    appendAnotherSticker("sticker_1mr1", textForShowAllStickers);
                    appendAnotherSticker("sticker_1nu6", textForShowAllStickers);
                    appendAnotherSticker("sticker_1nu7", textForShowAllStickers);
                    appendAnotherSticker("sticker_1nu8", textForShowAllStickers);
                    appendAnotherSticker("sticker_1nu9", textForShowAllStickers);
                    appendAnotherSticker("sticker_1nua", textForShowAllStickers);
                    appendAnotherSticker("sticker_1nub", textForShowAllStickers);
                    break;
                case 11:
                    //xmen
                    appendAnotherSticker("sticker_1mid_fr", textForShowAllStickers);
                    appendAnotherSticker("sticker_1mie_fr", textForShowAllStickers);
                    appendAnotherSticker("sticker_1mif", textForShowAllStickers);
                    appendAnotherSticker("sticker_1mig_fr", textForShowAllStickers);
                    appendAnotherSticker("sticker_1mih_fr", textForShowAllStickers);
                    appendAnotherSticker("sticker_1mii_fr", textForShowAllStickers);
                    appendAnotherSticker("sticker_1mij_fr", textForShowAllStickers);
                    appendAnotherSticker("sticker_1mik", textForShowAllStickers);
                    appendAnotherSticker("sticker_1mil", textForShowAllStickers);
                    appendAnotherSticker("sticker_1mim", textForShowAllStickers);
                    appendAnotherSticker("sticker_1min", textForShowAllStickers);
                    appendAnotherSticker("sticker_1mio", textForShowAllStickers);
                    appendAnotherSticker("sticker_1mip", textForShowAllStickers);
                    appendAnotherSticker("sticker_1miq", textForShowAllStickers);
                    appendAnotherSticker("sticker_1mir", textForShowAllStickers);
                    break;
                case 12:
                    //xbox
                    appendAnotherSticker("sticker_1my4", textForShowAllStickers);
                    appendAnotherSticker("sticker_1my5", textForShowAllStickers);
                    appendAnotherSticker("sticker_1my6", textForShowAllStickers);
                    appendAnotherSticker("sticker_1my7", textForShowAllStickers);
                    appendAnotherSticker("sticker_1my8", textForShowAllStickers);
                    appendAnotherSticker("sticker_1my9", textForShowAllStickers);
                    appendAnotherSticker("sticker_1mya", textForShowAllStickers);
                    appendAnotherSticker("sticker_1myb", textForShowAllStickers);
                    appendAnotherSticker("sticker_1myc", textForShowAllStickers);
                    appendAnotherSticker("sticker_1myd", textForShowAllStickers);
                    appendAnotherSticker("sticker_1mye", textForShowAllStickers);
                    appendAnotherSticker("sticker_1myf", textForShowAllStickers);
                    appendAnotherSticker("sticker_1myx", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n28", textForShowAllStickers);
                    break;
                case 13:
                    //foot
                    appendAnotherSticker("sticker_1n1m_de", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n1m_es", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n1m_fr", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n1m_it", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n1n_de", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n1n_es", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n1n_fr", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n1n_it", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n1o_de", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n1o_es", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n1o_fr", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n1o_it", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n1p_de", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n1p_es", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n1p_fr", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n1p_it", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n1q_de", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n1q_es", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n1q_fr", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n1q_it", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n1r_de", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n1r_es", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n1r_fr", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n1r_it", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n1s", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n1t_de", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n1t_es", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n1t_fr", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n1t_it", textForShowAllStickers);
                    break;
                case 14:
                    //store
                    appendAnotherSticker("sticker_1n2c", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n2d", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n2g", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n2h", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n2i", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n2j", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n2k", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n2l", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n2m", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n2n", textForShowAllStickers);
                    appendAnotherSticker("sticker_1n2o", textForShowAllStickers);
                    break;
                case 15:
                    //brice
                    appendAnotherSticker("sticker_1ntp", textForShowAllStickers);
                    appendAnotherSticker("sticker_1ntq", textForShowAllStickers);
                    appendAnotherSticker("sticker_1ntr", textForShowAllStickers);
                    appendAnotherSticker("sticker_1nts", textForShowAllStickers);
                    appendAnotherSticker("sticker_1ntt", textForShowAllStickers);
                    appendAnotherSticker("sticker_1ntu", textForShowAllStickers);
                    appendAnotherSticker("sticker_1ntv", textForShowAllStickers);
                    appendAnotherSticker("sticker_1ntw", textForShowAllStickers);
                    appendAnotherSticker("sticker_1ntx", textForShowAllStickers);
                    appendAnotherSticker("sticker_1nty", textForShowAllStickers);
                    appendAnotherSticker("sticker_1ntz", textForShowAllStickers);
                    appendAnotherSticker("sticker_1nu0", textForShowAllStickers);
            }
            listOfSpanForTextView[row] = Undeprecator.htmlFromHtml(textForShowAllStickers.toString(), withThisImageGetter, null);
        }
    }

    private void appendAnotherSticker(String newStickerToAppend, StringBuilder toThisStringBuilder) {
        toThisStringBuilder.append("<a href=\"").append(newStickerToAppend).append("\"><img src=\"").append(newStickerToAppend).append(".png\"/></a> ");
    }

    private Spannable replaceUrlSpans(Spanned spanToChange) {
        Spannable spannable = new SpannableString(spanToChange);
        URLSpan[] urlSpanArray = spannable.getSpans(0, spannable.length(), URLSpan.class);
        for (final URLSpan urlSpan : urlSpanArray) {
            Utils.replaceSpanByAnotherSpan(spannable, urlSpan, new LongClickableSpan() {
                private String url = urlSpan.getURL();

                @Override
                public void onLongClick(View view) {
                    if (getActivity() instanceof StickerSelected) {
                        String newSticker = "[[sticker:p/" + url.replace("sticker_", "").replace("_", "-") + "]]";
                        ((StickerSelected) getActivity()).getSelectedSticker(newSticker);
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
        View mainView = getActivity().getLayoutInflater().inflate(R.layout.dialog_selectsticker, null);
        mainTextView = (TextView) mainView.findViewById(R.id.showsticker_text_selectsticker);
        listOfStickersButtons[0] = (ImageView) mainView.findViewById(R.id.sticker_1_button_selectsticker);
        listOfStickersButtons[1] = (ImageView) mainView.findViewById(R.id.sticker_2_button_selectsticker);
        listOfStickersButtons[2] = (ImageView) mainView.findViewById(R.id.sticker_3_button_selectsticker);
        listOfStickersButtons[3] = (ImageView) mainView.findViewById(R.id.sticker_4_button_selectsticker);
        listOfStickersButtons[4] = (ImageView) mainView.findViewById(R.id.sticker_5_button_selectsticker);
        listOfStickersButtons[5] = (ImageView) mainView.findViewById(R.id.sticker_6_button_selectsticker);
        listOfStickersButtons[6] = (ImageView) mainView.findViewById(R.id.sticker_7_button_selectsticker);
        listOfStickersButtons[7] = (ImageView) mainView.findViewById(R.id.sticker_8_button_selectsticker);
        listOfStickersButtons[8] = (ImageView) mainView.findViewById(R.id.sticker_9_button_selectsticker);
        listOfStickersButtons[9] = (ImageView) mainView.findViewById(R.id.sticker_10_button_selectsticker);
        listOfStickersButtons[10] = (ImageView) mainView.findViewById(R.id.sticker_11_button_selectsticker);
        listOfStickersButtons[11] = (ImageView) mainView.findViewById(R.id.sticker_12_button_selectsticker);
        listOfStickersButtons[12] = (ImageView) mainView.findViewById(R.id.sticker_13_button_selectsticker);
        listOfStickersButtons[13] = (ImageView) mainView.findViewById(R.id.sticker_14_button_selectsticker);
        listOfStickersButtons[14] = (ImageView) mainView.findViewById(R.id.sticker_15_button_selectsticker);
        listOfStickersButtons[15] = (ImageView) mainView.findViewById(R.id.sticker_16_button_selectsticker);

        for (int i = 0; i < listOfStickersButtons.length; ++i) {
            listOfStickersButtons[i].setTag(i);
            listOfStickersButtons[i].setOnClickListener(stickerButtonClickedListener);
        }

        mainTextView.setMovementMethod(LongClickLinkMovementMethod.getInstance());
        selectThisStickerRow(oldRowNumber);
        builder.setTitle(R.string.selectSticker).setView(mainView)
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

    public interface StickerSelected {
        void getSelectedSticker(String newStickerToAdd);
    }
}
