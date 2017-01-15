package com.franckrj.respawnirc.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.WebNavigatorActivity;

public class LinkContextMenuDialogFragment extends DialogFragment {
    public static final String ARG_URL = "com.franckrj.respawnirc.linkcontextmenudialogfragment.url";

    private static final int POS_OPEN_IN_WEB_BROWSER = 0;
    private static final int POS_OPEN_IN_INTERN_BROWSER = 1;
    private static final int POS_COPY = 2;

    private String urlOfLink;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        Bundle currentArgs = getArguments();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (currentArgs != null) {
            urlOfLink = currentArgs.getString(ARG_URL, getString(R.string.waitingText));
        } else {
            urlOfLink = getString(R.string.waitingText);
        }

        builder.setTitle(urlOfLink);
        builder.setItems(R.array.choicesForLinkContextMenu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case POS_OPEN_IN_WEB_BROWSER:
                        try {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlOfLink));
                            startActivity(browserIntent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case POS_OPEN_IN_INTERN_BROWSER:
                        Intent newNavigatorIntent = new Intent(getActivity(), WebNavigatorActivity.class);
                        newNavigatorIntent.putExtra(WebNavigatorActivity.EXTRA_URL_LOAD, urlOfLink);
                        startActivity(newNavigatorIntent);
                        break;
                    case POS_COPY:
                        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(urlOfLink, urlOfLink);
                        clipboard.setPrimaryClip(clip);
                        break;
                }
                dialog.dismiss();
            }
        });

        return builder.create();
    }
}
