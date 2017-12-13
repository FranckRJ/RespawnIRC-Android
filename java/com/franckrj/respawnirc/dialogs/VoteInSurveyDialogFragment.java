package com.franckrj.respawnirc.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.franckrj.respawnirc.R;

public class VoteInSurveyDialogFragment extends DialogFragment {
    public static final String ARG_SURVEY_REPLYS = "com.franckrj.respawnirc.ARG_SURVEY_REPLYS";

    private static final String SAVE_ID_ITEM_SELECTED = "saveIdItemSelected";

    private int idOfLastSelectedItem = -1;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle currentArgs = getArguments();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String[] listOfReplys = null;

        if (currentArgs != null) {
            listOfReplys = currentArgs.getStringArray(ARG_SURVEY_REPLYS);
        }

        builder.setTitle(R.string.vote);
        builder.setSingleChoiceItems(listOfReplys, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                idOfLastSelectedItem = which;
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (getActivity() instanceof VoteInSurveyRegistered) {
                    ((VoteInSurveyRegistered) getActivity()).getRegisteredVote(idOfLastSelectedItem);
                }
                dialog.dismiss();
            }
        });

        return builder.create();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            idOfLastSelectedItem = savedInstanceState.getInt(SAVE_ID_ITEM_SELECTED, -1);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVE_ID_ITEM_SELECTED, idOfLastSelectedItem);
    }

    public interface VoteInSurveyRegistered {
        void getRegisteredVote(int voteIndex);
    }
}
