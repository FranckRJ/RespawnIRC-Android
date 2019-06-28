package com.franckrj.respawnirc.base;

import android.os.AsyncTask;

import com.franckrj.respawnirc.utils.WebManager;

import java.util.concurrent.Callable;

public abstract class AbsWebRequestAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    private RequestIsFinished<Result> requestIsFinishedListener = null;
    private RequestIsStarted requestIsStartedListener = null;

    public void setRequestIsFinishedListener(RequestIsFinished<Result> newRequestIsFinishedListener) {
        requestIsFinishedListener = newRequestIsFinishedListener;
    }

    public void setRequestIsStartedListener(RequestIsStarted newRequestIsStartedListener) {
        requestIsStartedListener = newRequestIsStartedListener;
    }

    public void clearListenersAndCancel() {
        requestIsStartedListener = null;
        requestIsFinishedListener = null;
        cancel(false);
    }

    protected WebManager.WebInfos initWebInfos(String newCookiesInAString, boolean newFollowRedirects) {
        WebManager.WebInfos currentWebInfos = new WebManager.WebInfos();

        currentWebInfos.cookiesInAString = newCookiesInAString;
        currentWebInfos.followRedirects = newFollowRedirects;
        currentWebInfos.isCancelled = new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return AbsWebRequestAsyncTask.this.isCancelled();
            }
        };

        return currentWebInfos;
    }

    @Override
    protected final void onPreExecute() {
        if (requestIsStartedListener != null) {
            requestIsStartedListener.onRequestIsStarted();
        }
    }

    @Override
    protected final void onPostExecute(Result reqResult) {
        if (requestIsFinishedListener != null) {
            requestIsFinishedListener.onRequestIsFinished(reqResult);
        }
        requestIsStartedListener = null;
        requestIsFinishedListener = null;
    }

    public interface RequestIsStarted {
        void onRequestIsStarted();
    }

    public interface RequestIsFinished<Result> {
        void onRequestIsFinished(Result reqResult);
    }
}
