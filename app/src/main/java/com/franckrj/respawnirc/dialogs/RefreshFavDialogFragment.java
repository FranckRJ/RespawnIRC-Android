package com.franckrj.respawnirc.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.base.AbsWebRequestAsyncTask;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.WebManager;

import java.util.ArrayList;

public class RefreshFavDialogFragment extends DialogFragment {
    public static final String ARG_PSEUDO = "com.franckrj.respawnirc.refreshfavdialogfragment.pseudo";
    public static final String ARG_FAV_TYPE = "com.franckrj.respawnirc.refreshfavdialogfragment.fav_type";
    public static final String ARG_COOKIE_LIST = "com.franckrj.respawnirc.refreshfavdialogfragment.cookielist";
    public static final int FAV_FORUM = 0;
    public static final int FAV_TOPIC = 1;

    private GetFavsOfPseudo currentTaskGetFavs = null;

    private final AbsWebRequestAsyncTask.RequestIsFinished<ArrayList<JVCParser.NameAndLink>> getFavsIsFinishedListener = new AbsWebRequestAsyncTask.RequestIsFinished<ArrayList<JVCParser.NameAndLink>>() {
        @Override
        public void onRequestIsFinished(ArrayList<JVCParser.NameAndLink> reqResult) {
            if (getActivity() instanceof NewFavsAvailable) {
                ((NewFavsAvailable) getActivity()).getNewFavs(reqResult, currentTaskGetFavs.getTypeOfFav());
            }

            currentTaskGetFavs = null;
            dismiss();
        }
    };

    private void stopAllCurrentTask() {
        if (currentTaskGetFavs != null) {
            currentTaskGetFavs.clearListenersAndCancel();
            currentTaskGetFavs = null;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle currentArgs = getArguments();
        String pseudoToFetch = "";
        String currentCookieList = "";
        int typeOfFav = FAV_FORUM;

        if (currentArgs != null) {
            pseudoToFetch = currentArgs.getString(ARG_PSEUDO, "");
            typeOfFav = currentArgs.getInt(ARG_FAV_TYPE, FAV_FORUM);
            currentCookieList = currentArgs.getString(ARG_COOKIE_LIST, "");
        }

        if (pseudoToFetch.isEmpty()) {
            dismiss();
        } else {
            currentTaskGetFavs = new GetFavsOfPseudo(typeOfFav);
            currentTaskGetFavs.setRequestIsFinishedListener(getFavsIsFinishedListener);
            currentTaskGetFavs.execute(pseudoToFetch, currentCookieList);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        return dialog;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View mainView = inflater.inflate(R.layout.dialog_loading, container, false);
        TextView textView = mainView.findViewById(R.id.textview_loading_dialog);
        textView.setText(R.string.loadingFavs);

        return mainView;
    }

    @Override
    public void onPause() {
        stopAllCurrentTask();
        super.onPause();
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        stopAllCurrentTask();
        super.onDismiss(dialogInterface);
    }

    private static class GetFavsOfPseudo extends AbsWebRequestAsyncTask<String, Void, ArrayList<JVCParser.NameAndLink>> {
        private final int typeOfFav;

        public GetFavsOfPseudo(int newTypeOfFav) {
            typeOfFav = newTypeOfFav;
        }

        public int getTypeOfFav() {
            return typeOfFav;
        }

        @Override
        protected ArrayList<JVCParser.NameAndLink> doInBackground(String... params) {
            if (params.length > 1) {
                WebManager.WebInfos currentWebInfos = initWebInfos(params[1], false);
                String pageContent;
                pageContent = WebManager.sendRequest("http://www.jeuxvideo.com/profil/" + params[0].toLowerCase(), "GET", "mode=favoris", currentWebInfos);

                if (pageContent != null) {
                    if (typeOfFav == FAV_FORUM) {
                        return JVCParser.getListOfForumsFavs(pageContent);
                    } else {
                        return JVCParser.getListOfTopicsFavs(pageContent);
                    }
                }
            }
            return null;
        }
    }

    public interface NewFavsAvailable {
        void getNewFavs(ArrayList<JVCParser.NameAndLink> listOfFavs, int typeOfFav);
    }
}
