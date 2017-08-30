package com.franckrj.respawnirc.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.util.SimpleArrayMap;
import android.support.v7.graphics.drawable.DrawableWrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class ImageDownloader {
    private SimpleArrayMap<String, DrawableWrapper> listOfDrawable = new SimpleArrayMap<>();
    private ArrayList<ImageGetterAsyncTask> listOfCurrentsTasks = new ArrayList<>();
    private Drawable defaultDrawable = null;
    private Drawable deletedDrawable = null;
    private int numberOfFilesDownloading = 0;
    private DownloadFinished listenerForDownloadFinished = null;
    private int imagesWidth = 0;
    private int imagesHeight = 0;
    private File imagesCacheDir = null;
    private Activity parentActivity = null;
    private boolean scaleLargeImages = false;

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

    public void setDefaultDrawable(Drawable newDrawable, boolean setBoundsToImageSize) {
        defaultDrawable = newDrawable;

        if (setBoundsToImageSize) {
            defaultDrawable.setBounds(0, 0, imagesWidth, imagesHeight);
        }
    }

    public void setDeletedDrawable(Drawable newDrawable, boolean setBoundsToImageSize) {
        deletedDrawable = newDrawable;

        if (setBoundsToImageSize) {
            deletedDrawable.setBounds(0, 0, imagesWidth, imagesHeight);
        }
    }

    public void setScaleLargeImages(boolean newVal) {
        scaleLargeImages = newVal;
    }

    public Drawable getDrawableFromLink(String link) {
        DrawableWrapper drawable = listOfDrawable.get(link);

        if (drawable == null) {
            try {
                File newFile = new File(imagesCacheDir, imageLinkToFileName(link));
                if (newFile.exists()) {
                    FileInputStream inputStream = new FileInputStream(newFile);
                    drawable = new DrawableWrapper(new BitmapDrawable(parentActivity.getResources(), inputStream));
                }
            } catch (Exception e) {
                drawable = null;
            }

            if (drawable == null) {
                drawable = new DrawableWrapper(defaultDrawable);
                startDownloadOfThisFileInThisWrapper(link, drawable);
            }

            drawable.setBounds(0, 0, imagesWidth, imagesHeight);
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

    private String imageLinkToFileName(String link) {
        if (link.startsWith("http://image.noelshack.com/minis/")) {
            return "nlshck_mini_" + link.substring(("http://image.noelshack.com/minis/").length()).replace("/", "_");
        } else if (link.startsWith("http://image.noelshack.com/fichiers/")) {
            return "nlshck_big_" + link.substring(("http://image.noelshack.com/fichiers/").length()).replace("/", "_");
        } else if (link.startsWith("http://image.jeuxvideo.com/avatar")) {
            return "vtr_" + link.substring(("http://image.jeuxvideo.com/avatar").length()).replace("/", "_");
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
        final int numberOfCurrentsTasks = listOfCurrentsTasks.size();
        --numberOfFilesDownloading;

        for (int i = 0; i < numberOfCurrentsTasks; ++i) {
            if (listOfCurrentsTasks.get(i) == taskEnded) {
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
            fileName = imageLinkToFileName(link);
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
                newDrawable.setBounds(0, 0, imagesWidth, imagesHeight);
                wrapperForDrawable.setWrappedDrawable(newDrawable);

                if (!fileName.isEmpty() && !imagesCacheDir.getPath().isEmpty()) {
                    try {
                        File newFile = new File(imagesCacheDir, fileName);
                        FileOutputStream outputStream = new FileOutputStream(newFile);

                        result.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                        outputStream.flush();
                        outputStream.close();
                    } catch (Exception e) {
                        //rien
                    }
                }
            } else {
                wrapperForDrawable.setWrappedDrawable(deletedDrawable);
            }
            downloadOfAFileEnded(this);
        }

        public Bitmap fetchDrawable(String urlString) {
            try {
                Bitmap bitmapToReturn;
                BitmapFactory.Options currentOptions = new BitmapFactory.Options();
                InputStream stream = (InputStream) new URL(urlString).getContent();

                if (scaleLargeImages) {
                    int maxOfScreenSize = Math.max(imagesWidth, imagesHeight);
                    currentOptions.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(stream, null, currentOptions);
                    stream.close();
                    stream = (InputStream) new URL(urlString).getContent();
                    currentOptions.inSampleSize = calculateInSampleSize(currentOptions, maxOfScreenSize, maxOfScreenSize);
                    currentOptions.inJustDecodeBounds = false;
                }

                bitmapToReturn = BitmapFactory.decodeStream(stream, null, currentOptions);
                stream.close();
                return bitmapToReturn;
            } catch (Exception e) {
                return null;
            }
        }

        public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
            int height = options.outHeight;
            int width = options.outWidth;
            int inSampleSize = 1;

            while (width / 2 > reqWidth * 0.9 || height / 2 > reqHeight * 0.9) {
                inSampleSize *= 2;
                width /= 2;
                height /=2;
            }

            return inSampleSize;
        }
    }

    public interface DownloadFinished {
        void newDownloadFinished(int numberOfDownloadRemaining);
    }
}
