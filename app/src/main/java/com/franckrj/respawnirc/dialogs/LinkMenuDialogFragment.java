package com.franckrj.respawnirc.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.widget.Toast;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.utils.Utils;

public class LinkMenuDialogFragment extends DialogFragment {
    public static final String ARG_URL = "com.franckrj.respawnirc.linkmenudialogfragment.url";

    private static final int POS_OPEN_IN_WEB_BROWSER = 0;
    private static final int POS_OPEN_IN_INTERN_BROWSER = 1;
    private static final int POS_COPY = 2;
    private static final int POS_SHARE_URL = 3;

    private String urlOfLink;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle currentArgs = getArguments();
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        if (currentArgs != null) {
            urlOfLink = currentArgs.getString(ARG_URL, getString(R.string.waitingText));
        } else {
            urlOfLink = getString(R.string.waitingText);
        }

        builder.setTitle(urlOfLink);
        builder.setItems(R.array.choicesForLinkMenu, (dialog, which) -> {
            if (getActivity() != null) {
                switch (which) {
                    case POS_OPEN_IN_WEB_BROWSER:
                        Utils.openLinkInExternalBrowser(urlOfLink, getActivity());
                        break;
                    case POS_OPEN_IN_INTERN_BROWSER:
                        Utils.openLinkInInternalBrowser(urlOfLink, getActivity());
                        break;
                    case POS_COPY:
                        Utils.putStringInClipboard(urlOfLink, getActivity());
                        Toast.makeText(getActivity(), R.string.copyDone, Toast.LENGTH_SHORT).show();
                        break;
                    case POS_SHARE_URL:
                        Utils.shareThisLink(urlOfLink, getActivity());
                        break;
                }
            }
            dialog.dismiss();
        });

        return builder.create();
    }
}
