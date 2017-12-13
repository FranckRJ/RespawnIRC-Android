package com.franckrj.respawnirc.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.util.SimpleArrayMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class ImageDownloader implements ImageGetterAsyncTask.RequestStatusChanged {
    private SimpleArrayMap<String, DrawableWrapper> listOfDrawable = new SimpleArrayMap<>();
    private ArrayList<ImageGetterAsyncTask> listOfCurrentsTasks = new ArrayList<>();
    private Drawable defaultDrawable = null;
    private Drawable deletedDrawable = null;
    private int numberOfFilesDownloading = 0;
    private DownloadFinished listenerForDownloadFinished = null;
    private CurrentProgress listenerForCurrentProgress = null;
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

    public void setImagesSize(int newWidth, int newHeight, boolean boundDefaultsDrawableToNewSize) {
        imagesWidth = newWidth;
        imagesHeight = newHeight;

        if (boundDefaultsDrawableToNewSize) {
            defaultDrawable.setBounds(0, 0, imagesWidth, imagesHeight);
            deletedDrawable.setBounds(0, 0, imagesWidth, imagesHeight);
        }
    }

    public void setListenerForDownloadFinished(DownloadFinished newListener) {
        listenerForDownloadFinished = newListener;
    }

    public void setListenerForCurrentProgress(CurrentProgress newListener) {
        listenerForCurrentProgress = newListener;
    }

    public void setDefaultDrawable(Drawable newDrawable) {
        defaultDrawable = newDrawable;
    }

    public void setDeletedDrawable(Drawable newDrawable) {
        deletedDrawable = newDrawable;
    }

    public void setScaleLargeImages(boolean newVal) {
        scaleLargeImages = newVal;
    }

    public Drawable getDrawableFromLink(String link) {
        DrawableWrapper drawable = listOfDrawable.get(link);

        if (drawable == null) {
            try {
                File newFile = new File(imagesCacheDir, Utils.imageLinkToFileName(link));
                if (newFile.exists()) {
                    drawable = new DrawableWrapper(new BitmapDrawable(parentActivity.getResources(), loadBitmapFromCache(newFile.getPath())));
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
            taskIterator.setRequestStatusChangedListener(null);
            taskIterator.cancel(false);
        }
        listOfCurrentsTasks.clear();
    }

    private void startDownloadOfThisFileInThisWrapper(String linkToFile, DrawableWrapper thisWrapper) {
        ImageGetterAsyncTask getterForImage = new ImageGetterAsyncTask(thisWrapper, linkToFile, imagesCacheDir.getPath(), scaleLargeImages);
        getterForImage.setRequestStatusChangedListener(this);
        listOfCurrentsTasks.add(getterForImage);
        getterForImage.execute();
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

    public Bitmap loadBitmapFromCache(String fileName) {
        try {
            Bitmap bitmapLoaded;
            BitmapFactory.Options currentOptions = new BitmapFactory.Options();
            InputStream stream = new FileInputStream(fileName);

            if (scaleLargeImages) {
                currentOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(stream, null, currentOptions);
                stream.close();
                stream = new FileInputStream(fileName);
                currentOptions.inSampleSize = calculateInSampleSize(currentOptions, imagesWidth, imagesHeight);
                currentOptions.inJustDecodeBounds = false;
            }

            bitmapLoaded = BitmapFactory.decodeStream(stream, null, currentOptions);
            stream.close();

            return bitmapLoaded;
        } catch (Exception e) {
            return null;
        }
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
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

    @Override
    public void onRequestProgress(Integer currentProgressInPercent, Integer fileSize, ImageGetterAsyncTask taskThatProgress) {
        if (listenerForCurrentProgress != null) {
            listenerForCurrentProgress.newCurrentProgress(currentProgressInPercent, fileSize, taskThatProgress.getFileDownloadPath());
        }
    }

    @Override
    public void onRequestFinished(String resultFileName, ImageGetterAsyncTask taskThatIsFinished) {
        if (!resultFileName.isEmpty()) {
            try {
                BitmapDrawable drawableToUse = new BitmapDrawable(parentActivity.getResources(), loadBitmapFromCache(resultFileName));
                drawableToUse.setBounds(0, 0, imagesWidth, imagesHeight);
                taskThatIsFinished.getWrapperForDrawable().setWrappedDrawable(drawableToUse);
            } catch (Exception e) {
                taskThatIsFinished.getWrapperForDrawable().setWrappedDrawable(deletedDrawable);
            }
        } else {
            taskThatIsFinished.getWrapperForDrawable().setWrappedDrawable(deletedDrawable);
        }
        downloadOfAFileEnded(taskThatIsFinished);
    }

    public interface DownloadFinished {
        void newDownloadFinished(int numberOfDownloadRemaining);
    }

    public interface CurrentProgress {
        void newCurrentProgress(int progressInPercent, int sizeOfFile, String fileLink);
    }
}


