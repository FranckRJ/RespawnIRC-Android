package com.franckrj.respawnirc.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.utils.Undeprecator;
import com.franckrj.respawnirc.utils.Utils;

public class SelectTextDialogFragment extends DialogFragment {
    public static final String ARG_TEXT_CONTENT = "com.franckrj.respawnirc.selecttextdialogfragment.text_content";
    public static final String ARG_TEXT_IS_HTML = "com.franckrj.respawnirc.selecttextdialogfragment.text_is_html";

    private TextView textShowed = null;
    private View topLine = null;
    private View bottomLine = null;

    private void updateLineShowedFromThiScrollView(NestedScrollView scrollView, int scrollY) {
        if (scrollY == 0) {
            topLine.setVisibility(View.INVISIBLE);
        } else {
            topLine.setVisibility(View.VISIBLE);
        }

        if (scrollY == (scrollView.getChildAt(0).getMeasuredHeight() - scrollView.getMeasuredHeight())) {
            bottomLine.setVisibility(View.INVISIBLE);
        } else {
            bottomLine.setVisibility(View.VISIBLE);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle currentArgs = getArguments();
        String textContent = "";
        boolean textIsHtml = false;
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        if (currentArgs != null) {
            textContent = currentArgs.getString(ARG_TEXT_CONTENT, "");
            textIsHtml = currentArgs.getBoolean(ARG_TEXT_IS_HTML, false);
        }

        @SuppressLint("InflateParams")
        final View mainView = requireActivity().getLayoutInflater().inflate(R.layout.dialog_selecttext, null);
        final NestedScrollView mainScrollView = mainView.findViewById(R.id.scrollview_selecttext);
        topLine = mainView.findViewById(R.id.line_top_selecttext);
        bottomLine = mainView.findViewById(R.id.line_bottom_selecttext);
        textShowed = mainView.findViewById(R.id.text_selecttext);

        mainScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView scrollView, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                updateLineShowedFromThiScrollView(scrollView, scrollY);
            }
        });

        if (textIsHtml) {
            textShowed.setText(Undeprecator.htmlFromHtml(textContent));
        } else {
            textShowed.setText(textContent);
        }

        builder.setTitle(R.string.selectText).setView(mainView)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.copy, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (getActivity() != null) {
                            final int selStart = textShowed.getSelectionStart();
                            final int selEnd = textShowed.getSelectionEnd();
                            final int min = Math.max(0, Math.min(selStart, selEnd));
                            final int max = Math.max(0, Math.max(selStart, selEnd));
                            if (min != max) {
                                Utils.putStringInClipboard(textShowed.getText().subSequence(min, max).toString(), getActivity());
                                Toast.makeText(getActivity(), R.string.copyDone, Toast.LENGTH_SHORT).show();
                            } else {
                                Utils.putStringInClipboard(textShowed.getText().toString(), getActivity());
                                Toast.makeText(getActivity(), R.string.allTextCopied, Toast.LENGTH_SHORT).show();
                            }
                        }
                        dialog.dismiss();
                    }
                });

        mainScrollView.post(new Runnable() {
            @Override
            public void run() {
                updateLineShowedFromThiScrollView(mainScrollView, mainScrollView.getScrollY());
            }
        });

        return builder.create();
    }
}
