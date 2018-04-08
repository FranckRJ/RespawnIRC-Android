package com.franckrj.respawnirc.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.utils.IgnoreListManager;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.PrefsManager;
import com.franckrj.respawnirc.utils.Utils;

public class MessageMenuDialogFragment extends DialogFragment {
    public static final String ARG_PSEUDO_MESSAGE = "com.franckrj.respawnirc.messagemenudialogfragment.pseudo_message";
    public static final String ARG_PSEUDO_USER = "com.franckrj.respawnirc.messagemenudialogfragment.pseudo_user";
    public static final String ARG_MESSAGE_ID = "com.franckrj.respawnirc.messagemenudialogfragment.message_id";
    public static final String ARG_LINK_TYPE_FOR_INTERNAL_BROWSER = "com.franckrj.respawnirc.messagemenudialogfragment.link_type_for_internal_browser";
    public static final String ARG_MESSAGE_CONTENT = "com.franckrj.respawnirc.messagemenudialogfragment.message_content";

    private static final int POS_OPEN_CDV = 0;
    private static final int POS_SEND_MP = 1;
    private static final int POS_IGNORE = 2;
    private static final int POS_COPY_PSEUDO = 3;
    private static final int POS_COPY_PERMALINK = 4;
    private static final int POS_DDB = 5;
    private static final int POS_SELECT_TEXT = 6;

    private String pseudoOfMessage;
    private String pseudoOfUser;
    private String idOfMessage;
    private PrefsManager.LinkType linkTypeForInternalBrowser = new PrefsManager.LinkType(PrefsManager.LinkType.NO_LINKS);
    private String messageNotParsed;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle currentArgs = getArguments();
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        if (currentArgs != null) {
            pseudoOfMessage = currentArgs.getString(ARG_PSEUDO_MESSAGE, getString(R.string.waitingText));
            pseudoOfUser = currentArgs.getString(ARG_PSEUDO_USER, "");
            idOfMessage = currentArgs.getString(ARG_MESSAGE_ID, "0");
            linkTypeForInternalBrowser.type = currentArgs.getInt(ARG_LINK_TYPE_FOR_INTERNAL_BROWSER, linkTypeForInternalBrowser.getDefaultType());
            messageNotParsed = currentArgs.getString(ARG_MESSAGE_CONTENT, "");
        } else {
            pseudoOfMessage = getString(R.string.waitingText);
            pseudoOfUser = "";
            idOfMessage = "0";
            linkTypeForInternalBrowser.type = linkTypeForInternalBrowser.getDefaultType();
            messageNotParsed = "";
        }

        builder.setTitle(pseudoOfMessage);
        builder.setItems(R.array.choicesForMessageMenu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (getActivity() != null) {
                    switch (which) {
                        case POS_OPEN_CDV: {
                            String link = "http://www.jeuxvideo.com/profil/" + pseudoOfMessage.toLowerCase() + "?mode=infos";
                            Utils.openCorrespondingBrowser(linkTypeForInternalBrowser, link, getActivity());
                            break;
                        }
                        case POS_SEND_MP: {
                            Utils.openLinkInInternalBrowser("http://www.jeuxvideo.com/messages-prives/nouveau.php?all_dest=" + pseudoOfMessage, getActivity());
                            break;
                        }
                        case POS_IGNORE: {
                            if (!pseudoOfUser.toLowerCase().equals(pseudoOfMessage.toLowerCase())) {
                                if (IgnoreListManager.addPseudoToIgnoredList(pseudoOfMessage)) {
                                    IgnoreListManager.saveListOfIgnoredPseudos();
                                    Toast.makeText(getActivity(), getString(R.string.pseudoIgnored, pseudoOfMessage), Toast.LENGTH_SHORT).show();

                                    if (getActivity() instanceof NewPseudoIgnored) {
                                        ((NewPseudoIgnored) getActivity()).onIgnoreNewPseudo(pseudoOfMessage);
                                    }
                                } else {
                                    Toast.makeText(getActivity(), R.string.pseudoIsAlreadyIgnored, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getActivity(), R.string.errorYouCantIgnoreYourself, Toast.LENGTH_SHORT).show();
                            }
                            break;
                        }
                        case POS_COPY_PSEUDO: {
                            Utils.putStringInClipboard(pseudoOfMessage, getActivity());
                            Toast.makeText(getActivity(), R.string.copyDone, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case POS_COPY_PERMALINK: {
                            Utils.putStringInClipboard("http://www.jeuxvideo.com/" + pseudoOfMessage.toLowerCase() + "/forums/message/" + idOfMessage, getActivity());
                            Toast.makeText(getActivity(), R.string.copyDone, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case POS_DDB: {
                            Utils.openLinkInInternalBrowser("http://www.jeuxvideo.com/" + pseudoOfMessage.toLowerCase() + "/forums/message/" + idOfMessage, getActivity());
                            break;
                        }
                        case POS_SELECT_TEXT: {
                            if (!isStateSaved()) {
                                Bundle argForFrag = new Bundle();
                                SelectTextDialogFragment selectTextDialogFragment = new SelectTextDialogFragment();
                                argForFrag.putString(SelectTextDialogFragment.ARG_TEXT_CONTENT, JVCParser.parseMessageToSimpleMessage(messageNotParsed));
                                argForFrag.putBoolean(SelectTextDialogFragment.ARG_TEXT_IS_HTML, true);
                                selectTextDialogFragment.setArguments(argForFrag);
                                selectTextDialogFragment.show(getActivity().getSupportFragmentManager(), "SelectTextDialogFragment");
                            }
                            break;
                        }
                    }
                }
                dialog.dismiss();
            }
        });

        return builder.create();
    }

    public interface NewPseudoIgnored {
        void onIgnoreNewPseudo(String newPseudoIgnored);
    }
}
