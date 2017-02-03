package com.franckrj.respawnirc.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.utils.Utils;

public class MessageContextMenuDialogFragment extends DialogFragment {
    public static final String ARG_PSEUDO = "com.franckrj.respawnirc.messagecontextmenudialogfragment.pseudo";
    public static final String ARG_MESSAGE_ID = "com.franckrj.respawnirc.messagecontextmenudialogfragment.message_id";
    public static final String ARG_USE_INTERNAL_BROWSER = "com.franckrj.respawnirc.messagecontextmenudialogfragment.use_internal_browser";

    private static final int POS_OPEN_CDV = 0;
    private static final int POS_COPY_PSEUDO = 1;
    private static final int POS_COPY_PERMALINK = 2;

    private String pseudoOfMessage;
    private String idOfMessage;
    private boolean useInternalBrowser;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        Bundle currentArgs = getArguments();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (currentArgs != null) {
            pseudoOfMessage = currentArgs.getString(ARG_PSEUDO, getString(R.string.waitingText));
            idOfMessage = currentArgs.getString(ARG_MESSAGE_ID, "0");
            useInternalBrowser = currentArgs.getBoolean(ARG_USE_INTERNAL_BROWSER, false);
        } else {
            pseudoOfMessage = getString(R.string.waitingText);
            idOfMessage = "0";
            useInternalBrowser = false;
        }

        builder.setTitle(pseudoOfMessage);
        builder.setItems(R.array.choicesForMessageContextMenu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case POS_OPEN_CDV: {
                        String link = "http://www.jeuxvideo.com/profil/" + pseudoOfMessage.toLowerCase() + "?mode=infos";
                        if (useInternalBrowser) {
                            Utils.openLinkInInternalNavigator(link, getActivity());
                        } else {
                            Utils.openLinkInExternalNavigator(link, getActivity());
                        }
                        break;
                    }
                    case POS_COPY_PSEUDO: {
                        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(pseudoOfMessage, pseudoOfMessage);
                        clipboard.setPrimaryClip(clip);
                        break;
                    }
                    case POS_COPY_PERMALINK: {
                        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        String link = "http://www.jeuxvideo.com/" + pseudoOfMessage.toLowerCase() + "/forums/message/" + idOfMessage;
                        ClipData clip = ClipData.newPlainText(link, link);
                        clipboard.setPrimaryClip(clip);
                        break;
                    }
                }
                dialog.dismiss();
            }
        });

        return builder.create();
    }
}
