package com.franckrj.respawnirc.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.utils.Utils;

public class SelectTextDialogFragment extends DialogFragment {
    public static final String ARG_TEXT_CONTENT = "com.franckrj.respawnirc.selecttextdialogfragment.text_content";

    private TextView textShowed = null;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        Bundle currentArgs = getArguments();
        String textContent = "";
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (currentArgs != null) {
            textContent = currentArgs.getString(ARG_TEXT_CONTENT, "");
        }

        View mainView = getActivity().getLayoutInflater().inflate(R.layout.dialog_selecttext, null);
        textShowed = (TextView) mainView.findViewById(R.id.text_selecttext);
        textShowed.setText(textContent);

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
                        final int selStart = textShowed.getSelectionStart();
                        final int selEnd = textShowed.getSelectionEnd();
                        final int min = Math.max(0, Math.min(selStart, selEnd));
                        final int max = Math.max(0, Math.max(selStart, selEnd));
                        if (min != max) {
                            Utils.putStringInClipboard(textShowed.getText().subSequence(min, max).toString(), getActivity());
                        }
                        dialog.dismiss();
                    }
                });

        return builder.create();
    }
}
