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

    public void setDefaultDrawable(Drawable newDrawable) {
        defaultDrawable = newDrawable;
    }

    public Drawable getDrawableFromLink(String link) {
        DrawableWrapper tmpDrawable = listOfDrawable.get(link);

        if (tmpDrawable == null) {
            ImageGetterAsyncTask getterForImage;
            tmpDrawable = new DrawableWrapper(defaultDrawable);
            tmpDrawable.setBounds(0, 0, defaultDrawable.getIntrinsicWidth(), defaultDrawable.getIntrinsicHeight());
            getterForImage = new ImageGetterAsyncTask(tmpDrawable);
            getterForImage.execute(link);
            listOfDrawable.put(link, tmpDrawable);
        }

        return tmpDrawable;
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
                wrapperForDrawable.setWrappedDrawable(result);
                wrapperForDrawable.setBounds(0, 0, Double.valueOf(result.getIntrinsicWidth() * 1.5).intValue(), Double.valueOf(result.getIntrinsicHeight() * 1.5).intValue());
            }
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
}
