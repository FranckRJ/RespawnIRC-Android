package com.franckrj.respawnirc.base;

import android.os.AsyncTask;

import com.franckrj.respawnirc.utils.WebManager;

import java.util.concurrent.Callable;

public abstract class AbsWebRequestAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    protected WebManager.WebInfos initWebInfos(String pCookiesInAString, boolean pFollowRedirects) {
        WebManager.WebInfos currentWebInfos = new WebManager.WebInfos();

        currentWebInfos.cookiesInAString = pCookiesInAString;
        currentWebInfos.followRedirects = pFollowRedirects;
        currentWebInfos.isCancelled = new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return AbsWebRequestAsyncTask.this.isCancelled();
            }
        };

        return currentWebInfos;
    }
}
