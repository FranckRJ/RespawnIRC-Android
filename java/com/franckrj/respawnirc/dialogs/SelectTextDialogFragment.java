package com.franckrj.respawnirc.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        Bundle currentArgs = getArguments();
        String textContent = "";
        boolean textIsHtml = false;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (currentArgs != null) {
            textContent = currentArgs.getString(ARG_TEXT_CONTENT, "");
            textIsHtml = currentArgs.getBoolean(ARG_TEXT_IS_HTML, false);
        }

        View mainView = getActivity().getLayoutInflater().inflate(R.layout.dialog_selecttext, null);
        textShowed = mainView.findViewById(R.id.text_selecttext);

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
                        dialog.dismiss();
                    }
                });

        return builder.create();
    }
}
