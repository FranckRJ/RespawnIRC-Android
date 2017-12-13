package com.franckrj.respawnirc.utils;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class ImageGetterAsyncTask extends AsyncTask<Void, Integer, String> {
    private final DrawableWrapper wrapperForDrawable;
    private final String fileDownloadPath;
    private final String fileLocalPath;
    private final boolean itsABigFile;
    private RequestStatusChanged requestStatusChangedListener = null;

    public ImageGetterAsyncTask(DrawableWrapper newWrapper, String link, String cacheDirPath, boolean newScaleLargeImage) {
        wrapperForDrawable = newWrapper;
        fileDownloadPath = link;
        fileLocalPath = (cacheDirPath + "/" + Utils.imageLinkToFileName(fileDownloadPath)).replace("//", "/");
        itsABigFile = newScaleLargeImage;
    }

    public DrawableWrapper getWrapperForDrawable() {
        return wrapperForDrawable;
    }

    public String getFileDownloadPath() {
        return fileDownloadPath;
    }

    public void setRequestStatusChangedListener(RequestStatusChanged newListener) {
        requestStatusChangedListener = newListener;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            int lenghtOfFile = 0;
            URL url = new URL(fileDownloadPath);

            if (itsABigFile) {
                URLConnection conection = url.openConnection();
                conection.connect();
                lenghtOfFile = conection.getContentLength();
            }

            InputStream input = new BufferedInputStream(url.openStream(), 8192);
            OutputStream output = new FileOutputStream(fileLocalPath);
            byte data[] = new byte[8192];
            long total = 0;
            int count;

            while ((count = input.read(data)) != -1 && !isCancelled()) {
                total += count;
                output.write(data, 0, count);

                if (lenghtOfFile > 0) {
                    publishProgress(Utils.roundToInt((total * 100) / lenghtOfFile), lenghtOfFile);
                }
            }

            output.flush();
            output.close();
            input.close();

            if (isCancelled()) {
                File file = new File(fileLocalPath);
                //noinspection ResultOfMethodCallIgnored
                file.delete();
                return "";
            }

            return fileLocalPath;
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        if (progress.length > 1 && requestStatusChangedListener != null) {
            requestStatusChangedListener.onRequestProgress(progress[0], progress[1], this);
        }
    }

    @Override
    protected void onPostExecute(String resultFileName) {
        if (requestStatusChangedListener != null) {
            requestStatusChangedListener.onRequestFinished(resultFileName, this);
        }
        requestStatusChangedListener = null;
    }

    public interface RequestStatusChanged {
        void onRequestProgress(Integer currentProgressInPercent, Integer fileSize, ImageGetterAsyncTask taskThatProgress);
        void onRequestFinished(String resultFileName, ImageGetterAsyncTask taskThatIsFinished);
    }
}
