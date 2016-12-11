package com.franckrj.respawnirc.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.EditText;

import com.franckrj.respawnirc.R;

public class ChooseTopicOrForumLinkDialogFragment extends DialogFragment {
    EditText linkEdit = null;
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View mainView = getActivity().getLayoutInflater().inflate(R.layout.dialog_choosetopicorforumlink, null);
        linkEdit = (EditText) mainView.findViewById(R.id.link_edit_choosetopicorforumlink);
        builder.setTitle(R.string.chooseTopicOrForumLink).setView(mainView)
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    if (!linkEdit.getText().toString().isEmpty()) {
                        Activity currentActivity = getActivity();

                        if (currentActivity instanceof NewTopicOrForumSelected) {
                            ((NewTopicOrForumSelected) currentActivity).newTopicOrForumAvailable(linkEdit.getText().toString());
                        }
                    }
                    dialog.dismiss();
                }
            });
        return builder.create();
    }

    public interface NewTopicOrForumSelected {
        void newTopicOrForumAvailable(String newTopicOrForumLink);
    }
}