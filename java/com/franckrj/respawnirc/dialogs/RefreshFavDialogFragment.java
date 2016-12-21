package com.franckrj.respawnirc.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.WebManager;

import java.util.ArrayList;

public class RefreshFavDialogFragment extends DialogFragment {
    public static final String ARG_PSEUDO = "com.franckrj.respawnirc.refreshfavdialogfragment.pseudo";

    private GetFavsOfPseudo currentTaskGetFavs = null;

    private void stopAllCurrentTask() {
        if (currentTaskGetFavs != null) {
            currentTaskGetFavs.cancel(true);
            currentTaskGetFavs = null;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle currentArgs = getArguments();
        String pseudoToFetch = "";

        if (currentArgs != null) {
            pseudoToFetch = currentArgs.getString(ARG_PSEUDO, "");
        }

        if (pseudoToFetch.isEmpty()) {
            dismiss();
        } else {
            currentTaskGetFavs = new GetFavsOfPseudo();
            currentTaskGetFavs.execute(pseudoToFetch);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View mainView = inflater.inflate(R.layout.dialog_loading, container, false);
        TextView textView = (TextView) mainView.findViewById(R.id.textview_loading_dialog);
        textView.setText(R.string.loadingFavs);

        return mainView;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        stopAllCurrentTask();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        stopAllCurrentTask();
    }

    private class GetFavsOfPseudo extends AsyncTask<String, Void, ArrayList<JVCParser.NameAndLink>> {
        @Override
        protected ArrayList<JVCParser.NameAndLink> doInBackground(String... params) {
            if (params.length > 0) {
                WebManager.WebInfos currentWebInfos = new WebManager.WebInfos();
                String pageContent;
                currentWebInfos.followRedirects = false;
                pageContent = WebManager.sendRequest("http://www.jeuxvideo.com/profil/" + params[0].toLowerCase(), "GET", "mode=favoris", "", currentWebInfos);

                if (pageContent != null) {
                    return JVCParser.getListOfForumsFavs(pageContent);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<JVCParser.NameAndLink> listOfForumsFavs) {
            super.onPostExecute(listOfForumsFavs);

            if (getActivity() instanceof NewForumsFavs) {
                ((NewForumsFavs) getActivity()).getNewForumsFavs(listOfForumsFavs);
            }

            currentTaskGetFavs = null;
            dismiss();
        }
    }

    public interface NewForumsFavs {
        void getNewForumsFavs(ArrayList<JVCParser.NameAndLink> listOfForumsFavs);
    }
}
