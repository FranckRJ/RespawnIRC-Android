package com.franckrj.respawnirc.utils;

import com.franckrj.respawnirc.base.AbsWebRequestAsyncTask;

public class AddOrRemoveTopicToSubs extends AbsWebRequestAsyncTask<String, Void, String> {
    private final boolean addToSubs;
    private ActionToSubsEnded actionToSubsEndedListener;

    @SuppressWarnings("FieldCanBeLocal")
    private final AbsWebRequestAsyncTask.RequestIsFinished<String> changeSubIsFinishedListener = new AbsWebRequestAsyncTask.RequestIsFinished<String>() {
        @Override
        public void onRequestIsFinished(String reqResult) {
            if (actionToSubsEndedListener != null) {
                if (reqResult == null) {
                    actionToSubsEndedListener.getActionToSubsResult("", true);
                } else if (reqResult.startsWith("respawnirc:error:")) {
                    actionToSubsEndedListener.getActionToSubsResult(reqResult.substring(("respawnirc:error:").length()), true);
                } else {
                    actionToSubsEndedListener.getActionToSubsResult(reqResult, false);
                }
            }
        }
    };

    public AddOrRemoveTopicToSubs(boolean itsAnAdd, ActionToSubsEnded newListener) {
        addToSubs = itsAnAdd;
        actionToSubsEndedListener = newListener;
        setRequestIsFinishedListener(changeSubIsFinishedListener);
    }

    public boolean getAddToSubs() {
        return addToSubs;
    }

    @Override
    protected String doInBackground(String... params) {
        if (params.length > 3) {
            String pageContent;
            String linkToUse;
            String paramsForReq;
            WebManager.WebInfos currentWebInfos = initWebInfos(params[3], false);

            if (addToSubs) {
                linkToUse = "https://www.jeuxvideo.com/abonnements/ajax/ajax_abo_insert.php";
                paramsForReq = "type=topic&ids_liste=" + params[0] + "&" + params[2];
            } else {
                linkToUse = "https://www.jeuxvideo.com/abonnements/ajax/ajax_abo_delete.php";
                paramsForReq = "id=" + params[1] + "&" + params[2];
            }

            pageContent = WebManager.sendRequest(linkToUse, "POST", paramsForReq, currentWebInfos);

            if (!Utils.stringIsEmptyOrNull(pageContent)) {
                String error = JVCParser.getErrorMessageInJsonMode(pageContent);

                if (error != null) {
                    return "respawnirc:error:" + error;
                } else if (addToSubs) {
                    return JVCParser.getSubIdFromJson(pageContent);
                } else {
                    return "";
                }
            }
        }

        return null;
    }

    public interface ActionToSubsEnded {
        void getActionToSubsResult(String resultInString, boolean itsAnError);
    }
}
