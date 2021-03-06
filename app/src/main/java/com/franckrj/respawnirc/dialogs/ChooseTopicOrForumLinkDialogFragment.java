package com.franckrj.respawnirc.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.franckrj.respawnirc.R;

public class ChooseTopicOrForumLinkDialogFragment extends DialogFragment {
    EditText linkEdit = null;

    private final TextView.OnEditorActionListener actionInEditTextListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                topicOrForumLinkChoosed();
                return true;
            }
            return false;
        }
    };

    private void topicOrForumLinkChoosed() {
        if (!linkEdit.getText().toString().isEmpty()) {
            Activity currentActivity = getActivity();

            if (currentActivity instanceof NewTopicOrForumSelected) {
                ((NewTopicOrForumSelected) currentActivity).newTopicOrForumAvailable(linkEdit.getText().toString());
            }
        }
        dismiss();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        @SuppressLint("InflateParams")
        View mainView = requireActivity().getLayoutInflater().inflate(R.layout.dialog_choosetopicorforumlink, null);
        linkEdit = mainView.findViewById(R.id.link_edit_choosetopicorforumlink);
        linkEdit.setOnEditorActionListener(actionInEditTextListener);
        builder.setTitle(R.string.openTopicOrForumLink).setView(mainView)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        topicOrForumLinkChoosed();
                    }
                });
        return builder.create();
    }

    public interface NewTopicOrForumSelected {
        void newTopicOrForumAvailable(String newTopicOrForumLink);
    }
}
