package com.franckrj.respawnirc.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import androidx.collection.SimpleArrayMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class ImageDownloader implements ImageGetterAsyncTask.RequestStatusChanged {
    private SimpleArrayMap<String, DrawableWrapper> listOfDrawable = new SimpleArrayMap<>();
    private ArrayList<ImageGetterAsyncTask> listOfCurrentsTasks = new ArrayList<>();
    private Drawable defaultDrawable = null;
    private Drawable deletedDrawable = null;
    private Drawable defaultDrawableResized = null;
    private Drawable deletedDrawableResized = null;
    private int numberOfFilesDownloading = 0;
    private DownloadFinished listenerForDownloadFinished = null;
    private CurrentProgress listenerForCurrentProgress = null;
    private int imagesWidth = 0;
    private int imagesHeight = 0;
    private File imagesCacheDir = null;
    private Activity parentActivity = null;
    private boolean updateProgress = false;
    private boolean optimisedScale = false;

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

        if (defaultDrawableResized != null) {
            defaultDrawableResized.setBounds(0, 0, imagesWidth, imagesHeight);
        }
        if (deletedDrawableResized != null) {
            deletedDrawableResized.setBounds(0, 0, imagesWidth, imagesHeight);
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
        defaultDrawable.setBounds(0, 0, defaultDrawable.getIntrinsicWidth(), defaultDrawable.getIntrinsicHeight());
    }

    public void setDeletedDrawable(Drawable newDrawable) {
        deletedDrawable = newDrawable;
        deletedDrawable.setBounds(0, 0, deletedDrawable.getIntrinsicWidth(), deletedDrawable.getIntrinsicHeight());
    }

    public void setDefaultDrawableResized(Drawable newDrawable) {
        defaultDrawableResized = newDrawable;
        defaultDrawableResized.setBounds(0, 0, imagesWidth, imagesHeight);
        if (defaultDrawable == null) {
            defaultDrawable = defaultDrawableResized;
        }
    }

    public void setDeletedDrawableResized(Drawable newDrawable) {
        deletedDrawableResized = newDrawable;
        deletedDrawableResized.setBounds(0, 0, imagesWidth, imagesHeight);
        if (deletedDrawable == null) {
            deletedDrawable = deletedDrawableResized;
        }
    }

    public void setUpdateProgress(boolean newVal) {
        updateProgress = newVal;
    }

    public void setOptimisedScale(boolean newVal) {
        optimisedScale = newVal;
    }

    public Drawable getDrawableFromLink(String link, boolean setToDefaultSize, boolean scaleToSize, boolean setToDefaultAspectRatio) {
        DrawableWrapper drawable = listOfDrawable.get(link);

        if (drawable == null) {
            try {
                File newFile = new File(imagesCacheDir, Utils.imageLinkToFileName(link));
                if (newFile.exists()) {
                    BitmapDrawable tmpDrawable = new BitmapDrawable(parentActivity.getResources(), loadBitmapFromCache(newFile.getPath(), scaleToSize));
                    if (setToDefaultAspectRatio) {
                        drawable = new DrawableWrapper(buildInsetDrawableForAspectRatio(tmpDrawable, setToDefaultSize));
                    } else {
                        drawable = new DrawableWrapper(tmpDrawable);
                    }
                }
            } catch (Exception e) {
                //noinspection ConstantConditions
                drawable = null;
            }

            if (drawable == null) {
                drawable = new DrawableWrapper(setToDefaultSize ? defaultDrawableResized : defaultDrawable);
                startDownloadOfThisFileInThisWrapper(link, drawable, setToDefaultSize, scaleToSize, setToDefaultAspectRatio);
            }

            if (setToDefaultSize) {
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
            taskIterator.setRequestStatusChangedListener(null);
            taskIterator.cancel(false);
        }
        listOfCurrentsTasks.clear();
    }

    private void startDownloadOfThisFileInThisWrapper(String linkToFile, DrawableWrapper thisWrapper, boolean setToDefaultSize, boolean scaleToSize, boolean setToDefaultAspectRatio) {
        ImageGetterAsyncTask getterForImage = new ImageGetterAsyncTask(thisWrapper, linkToFile, imagesCacheDir.getPath(), updateProgress, setToDefaultSize, scaleToSize, setToDefaultAspectRatio);
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

    public Bitmap loadBitmapFromCache(String fileName, boolean scaleToSize) {
        try {
            Bitmap bitmapLoaded;
            BitmapFactory.Options currentOptions = new BitmapFactory.Options();
            InputStream stream = new FileInputStream(fileName);

            if (scaleToSize) {
                currentOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(stream, null, currentOptions);
                stream.close();
                stream = new FileInputStream(fileName);
                currentOptions.inSampleSize = calculateInSampleSize(currentOptions, imagesWidth, imagesHeight, optimisedScale);
                currentOptions.inJustDecodeBounds = false;
            }

            bitmapLoaded = BitmapFactory.decodeStream(stream, null, currentOptions);
            stream.close();

            return bitmapLoaded;
        } catch (Exception e) {
            return null;
        }
    }

    public Drawable buildInsetDrawableForAspectRatio(Drawable drawableToChange, boolean isSetToDefaultSize) {
        int drawableWidth = drawableToChange.getIntrinsicWidth();
        int drawableHeight = drawableToChange.getIntrinsicHeight();

        if ((drawableHeight / (double)drawableWidth) < ((imagesHeight / (double)imagesWidth) - 0.005)) {
            int verticalMargin = Utils.roundToInt(drawableWidth * (imagesHeight / (double)imagesWidth)) - drawableHeight;

            if (isSetToDefaultSize) {
                verticalMargin *= (imagesHeight / (double)(verticalMargin + drawableHeight));
            }

            return (new InsetDrawable(drawableToChange, 0, verticalMargin / 2, 0, verticalMargin / 2));
        } else if ((drawableHeight / (double)drawableWidth) > ((imagesHeight / (double)imagesWidth) + 0.005)) {
            int horizontalMargin = Utils.roundToInt(drawableHeight / (imagesHeight / (double)imagesWidth)) - drawableWidth;

            if (isSetToDefaultSize) {
                horizontalMargin *= (imagesWidth / (double)(horizontalMargin + drawableWidth));
            }

            return (new InsetDrawable(drawableToChange, horizontalMargin / 2, 0, horizontalMargin / 2, 0));
        } else {
            return drawableToChange;
        }
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight, boolean scaleWithLoss) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        while (width / 2 > reqWidth * (scaleWithLoss ? 0.9 : 1) || height / 2 > reqHeight * (scaleWithLoss ? 0.9 : 1)) {
            inSampleSize *= 2;
            width /= 2;
            height /= 2;
        }

        return inSampleSize;
    }

    @Override
    public void onRequestProgress(Long currentProgressInPercent, Long fileSize, ImageGetterAsyncTask taskThatProgress) {
        if (listenerForCurrentProgress != null) {
            listenerForCurrentProgress.newCurrentProgress(currentProgressInPercent, fileSize, taskThatProgress.getFileDownloadPath());
        }
    }

    @Override
    public void onRequestFinished(String resultFileName, ImageGetterAsyncTask taskThatIsFinished) {
        DrawableWrapper wrappedDrawable = taskThatIsFinished.getWrapperForDrawable();

        if (!resultFileName.isEmpty()) {
            try {
                BitmapDrawable tmpDrawable = new BitmapDrawable(parentActivity.getResources(), loadBitmapFromCache(resultFileName, taskThatIsFinished.getScaleToSize()));
                if (taskThatIsFinished.getSetToDefaultAspectRatio()) {
                    wrappedDrawable.setWrappedDrawable(buildInsetDrawableForAspectRatio(tmpDrawable, taskThatIsFinished.getSetToDefaultSize()));
                } else {
                    wrappedDrawable.setWrappedDrawable(tmpDrawable);
                }
            } catch (Exception e) {
                wrappedDrawable.setWrappedDrawable(taskThatIsFinished.getSetToDefaultSize() ? deletedDrawableResized : deletedDrawable);
            }
        } else {
            wrappedDrawable.setWrappedDrawable(taskThatIsFinished.getSetToDefaultSize() ? deletedDrawableResized : deletedDrawable);
        }
        if (taskThatIsFinished.getSetToDefaultSize()) {
            wrappedDrawable.setBounds(0, 0, imagesWidth, imagesHeight);
        } else {
            wrappedDrawable.setBounds(0, 0, wrappedDrawable.getIntrinsicWidth(), wrappedDrawable.getIntrinsicHeight());
        }
        downloadOfAFileEnded(taskThatIsFinished);
    }

    public interface DownloadFinished {
        void newDownloadFinished(int numberOfDownloadRemaining);
    }

    public interface CurrentProgress {
        void newCurrentProgress(long progressInPercent, long sizeOfFile, String fileLink);
    }
}


