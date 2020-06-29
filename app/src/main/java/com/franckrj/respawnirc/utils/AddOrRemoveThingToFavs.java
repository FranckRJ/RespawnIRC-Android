package com.franckrj.respawnirc.utils;

import com.franckrj.respawnirc.base.AbsWebRequestAsyncTask;

public class AddOrRemoveThingToFavs extends AbsWebRequestAsyncTask<String, Void, String> {
    private final boolean addToFavs;
    private ActionToFavsEnded actionToFavsEndedListener;

    @SuppressWarnings("FieldCanBeLocal")
    private final AbsWebRequestAsyncTask.RequestIsFinished<String> changeFavIsFinishedListener = new AbsWebRequestAsyncTask.RequestIsFinished<String>() {
        @Override
        public void onRequestIsFinished(String reqResult) {
            if (actionToFavsEndedListener != null) {
                if (reqResult != null) {
                    actionToFavsEndedListener.getActionToFavsResult(reqResult, true);
                    return;
                }

                actionToFavsEndedListener.getActionToFavsResult("", false);
            }
        }
    };

    public AddOrRemoveThingToFavs(boolean itsAnAdd, ActionToFavsEnded newListener) {
        addToFavs = itsAnAdd;
        actionToFavsEndedListener = newListener;
        setRequestIsFinishedListener(changeFavIsFinishedListener);
    }

    public boolean getAddToFavs() {
        return addToFavs;
    }

    @Override
    protected String doInBackground(String... params) {
        String forumId;
        String topicId;
        String typeOfAction;
        String ajaxInfos;
        String pageContent;
        String actionToDo;
        WebManager.WebInfos currentWebInfos = initWebInfos("", false);

        switch (params.length) {
            case 3:
                forumId = params[0];
                topicId = "0";
                typeOfAction = "forum";
                ajaxInfos = params[1];
                currentWebInfos.cookiesInAString = params[2];
                break;
            case 4:
                forumId = params[0];
                topicId = params[1];
                typeOfAction = "topic";
                ajaxInfos = params[2];
                currentWebInfos.cookiesInAString = params[3];
                break;
            default:
                return "";
        }

        if (addToFavs) {
            actionToDo = "add";
        } else {
            actionToDo = "delete";
        }

        pageContent = WebManager.sendRequest("https://www.jeuxvideo.com/forums/ajax_forum_prefere.php", "POST", "id_forum=" + forumId + "&id_topic=" + topicId + "&action=" + actionToDo + "&type=" + typeOfAction + "&" + ajaxInfos, currentWebInfos);

        if (!Utils.stringIsEmptyOrNull(pageContent)) {
            return JVCParser.getErrorMessageInJsonMode(pageContent);
        }
        return null;
    }

    public interface ActionToFavsEnded {
        void getActionToFavsResult(String resultInString, boolean itsAnError);
    }
}
