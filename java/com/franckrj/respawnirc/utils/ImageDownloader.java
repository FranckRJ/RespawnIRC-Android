package com.franckrj.respawnirc.utils;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.graphics.drawable.DrawableWrapper;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

public class ImageDownloader {
    HashMap<String, DrawableWrapper> listOfDrawable = new HashMap<>();
    Drawable defaultDrawable = null;
    Drawable deletedDrawable = null;
    int numberOfFilesDownloading = 0;
    DownloadFinished listenerForDownloadFinished = null;
    int imagesWidth = 0;
    int imagesHeight = 0;

    public void setImagesSize(int newWidth, int newHeight) {
        imagesWidth = newWidth;
        imagesHeight = newHeight;
    }

    public void setListenerForDownloadFinished(DownloadFinished newListener) {
        listenerForDownloadFinished = newListener;
    }

    public void setDefaultDrawable(Drawable newDrawable) {
        defaultDrawable = newDrawable;
    }

    public void setDeletedDrawable(Drawable newDrawable) {
        deletedDrawable = newDrawable;
    }

    public Drawable getDrawableFromLink(String link) {
        DrawableWrapper drawable = listOfDrawable.get(link);

        if (drawable == null) {
            drawable = new DrawableWrapper(defaultDrawable);
            drawable.setBounds(0, 0, imagesWidth, imagesHeight);
            startDownloadOfThisFileInThisWrapper(link, drawable);
            listOfDrawable.put(link, drawable);
        }

        return drawable;
    }

    private void startDownloadOfThisFileInThisWrapper(String linkToFile, DrawableWrapper thisWrapper) {
        ImageGetterAsyncTask getterForImage = new ImageGetterAsyncTask(thisWrapper);
        getterForImage.execute(linkToFile);
        ++numberOfFilesDownloading;
    }

    private void downloadOfAFileEnded() {
        --numberOfFilesDownloading;
        if (listenerForDownloadFinished != null) {
            listenerForDownloadFinished.newDownloadFinished(numberOfFilesDownloading);
        }
    }

    private class ImageGetterAsyncTask extends AsyncTask<String, Void, Drawable> {
        DrawableWrapper wrapperForDrawable;

        public ImageGetterAsyncTask(DrawableWrapper newWrapper) {
            wrapperForDrawable = newWrapper;
        }

        @Override
        protected Drawable doInBackground(String... params) {
            String source = params[0];
            return fetchDrawable(source);
        }

        @Override
        protected void onPostExecute(Drawable result) {
            if (result != null) {
                result.setBounds(0, 0, imagesWidth, imagesHeight);
                wrapperForDrawable.setWrappedDrawable(result);
            } else {
                wrapperForDrawable.setWrappedDrawable(deletedDrawable);
            }
            downloadOfAFileEnded();
        }

        public Drawable fetchDrawable(String urlString) {
            try {
                InputStream stream = (InputStream) new URL(urlString).getContent();
                return Drawable.createFromStream(stream, "src");
            } catch (Exception e) {
                return null;
            }
        }
    }

    public interface DownloadFinished {
        void newDownloadFinished(int numberOfDownloadRemaining);
    }
}
