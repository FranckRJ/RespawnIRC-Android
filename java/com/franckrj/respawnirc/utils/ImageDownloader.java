package com.franckrj.respawnirc.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.graphics.drawable.DrawableWrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class ImageDownloader {
    HashMap<String, DrawableWrapper> listOfDrawable = new HashMap<>();
    ArrayList<ImageGetterAsyncTask> listOfCurrentsTasks = new ArrayList<>();
    Drawable defaultDrawable = null;
    Drawable deletedDrawable = null;
    int numberOfFilesDownloading = 0;
    DownloadFinished listenerForDownloadFinished = null;
    int imagesWidth = 0;
    int imagesHeight = 0;
    File imagesCacheDir = null;
    Activity parentActivity = null;

    public int getNumberOfFilesDownloading() {
        return numberOfFilesDownloading;
    }

    public void setParentActivity(Activity newParentActivity) {
        parentActivity = newParentActivity;
    }

    public void setImagesCacheDir(File newCacheDir) {
        imagesCacheDir = newCacheDir;
    }

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
            try {
                File newFile = new File(imagesCacheDir, noelshackLinkToFileName(link));
                if (newFile.exists()) {
                    FileInputStream inputStream = new FileInputStream(newFile);
                    drawable = new DrawableWrapper(new BitmapDrawable(parentActivity.getResources(), inputStream));
                }
            } catch (Exception e) {
                e.printStackTrace();
                drawable = null;
            }

            if (drawable == null) {
                drawable = new DrawableWrapper(defaultDrawable);
                startDownloadOfThisFileInThisWrapper(link, drawable);
            }


            if (imagesWidth != 0 || imagesHeight != 0) {
                drawable.setBounds(0, 0, imagesWidth, imagesHeight);
            } else {
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            }
            listOfDrawable.put(link, drawable);
        }

        return drawable;
    }

    public void clearMemoryCache() {
        listOfDrawable.clear();
    }

    public void stopAllCurrentTasks() {
        for (ImageGetterAsyncTask taskIterator : listOfCurrentsTasks) {
            taskIterator.cancel(true);
        }
        listOfCurrentsTasks.clear();
    }

    private String noelshackLinkToFileName(String link) {
        if (link.startsWith("http://image.noelshack.com/minis/")) {
            return "nlshck_mini_" + link.substring(("http://image.noelshack.com/minis/").length()).replace("/", "_");
        } else if (link.startsWith("http://image.noelshack.com/fichiers/")) {
            return "nlshck_big_" + link.substring(("http://image.noelshack.com/fichiers/").length()).replace("/", "_");
        } else {
            return "";
        }
    }

    private void startDownloadOfThisFileInThisWrapper(String linkToFile, DrawableWrapper thisWrapper) {
        ImageGetterAsyncTask getterForImage = new ImageGetterAsyncTask(thisWrapper, linkToFile);
        listOfCurrentsTasks.add(getterForImage);
        getterForImage.execute(linkToFile);
        ++numberOfFilesDownloading;
    }

    private void downloadOfAFileEnded(ImageGetterAsyncTask taskEnded) {
        --numberOfFilesDownloading;

        for (int i = 0; i < listOfCurrentsTasks.size(); ++i) {
            if (taskEnded == listOfCurrentsTasks.get(i)) {
                listOfCurrentsTasks.remove(i);
                break;
            }
        }

        if (listenerForDownloadFinished != null) {
            listenerForDownloadFinished.newDownloadFinished(numberOfFilesDownloading);
        }
    }

    private class ImageGetterAsyncTask extends AsyncTask<String, Void, Bitmap> {
        DrawableWrapper wrapperForDrawable;
        String fileName;

        public ImageGetterAsyncTask(DrawableWrapper newWrapper, String link) {
            wrapperForDrawable = newWrapper;
            fileName = noelshackLinkToFileName(link);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String source = params[0];
            return fetchDrawable(source);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                BitmapDrawable newDrawable = new BitmapDrawable(parentActivity.getResources(), result);
                if (imagesWidth != 0 || imagesHeight != 0) {
                    newDrawable.setBounds(0, 0, imagesWidth, imagesHeight);
                } else {
                    newDrawable.setBounds(0, 0, newDrawable.getIntrinsicWidth(), newDrawable.getIntrinsicHeight());
                }
                wrapperForDrawable.setWrappedDrawable(newDrawable);

                if (!fileName.isEmpty() && !imagesCacheDir.getPath().isEmpty()) {
                    try {
                        File newFile = new File(imagesCacheDir, fileName);
                        FileOutputStream outputStream = new FileOutputStream(newFile);

                        result.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                        outputStream.flush();
                        outputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                wrapperForDrawable.setWrappedDrawable(deletedDrawable);
            }
            downloadOfAFileEnded(this);
        }

        public Bitmap fetchDrawable(String urlString) {
            try {
                InputStream stream = (InputStream) new URL(urlString).getContent();
                return BitmapFactory.decodeStream(stream);
            } catch (Exception e) {
                return null;
            }
        }
    }

    public interface DownloadFinished {
        void newDownloadFinished(int numberOfDownloadRemaining);
    }
}
