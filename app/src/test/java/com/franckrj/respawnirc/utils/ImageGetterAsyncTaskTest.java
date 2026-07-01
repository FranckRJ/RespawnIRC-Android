package com.franckrj.respawnirc.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.graphics.drawable.ColorDrawable;
import android.os.Looper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Couvre le chemin « image » (parallèle), différent du chemin « topic » sur un seul point :
 * la progression (publishProgress / onProgressUpdate), que les tâches web n'utilisent pas. On
 * vérifie via le vrai Looper que progression et fin sont transmises au RequestStatusChanged sur le
 * thread principal, et qu'un teardown façon ImageDownloader.stopAllCurrentTasks() (listener coupé +
 * cancel) avant que le Looper ne tourne abandonne la livraison.
 *
 * doInBackground est stubbé (pas de réseau ni de fichier) ; on teste le câblage des callbacks, pas
 * le téléchargement réel.
 */
@RunWith(RobolectricTestRunner.class)
public class ImageGetterAsyncTaskTest {
    private static final Executor DIRECT_EXECUTOR = Runnable::run;

    private static String threadTag() {
        return Looper.myLooper() == Looper.getMainLooper() ? "main" : "bg";
    }

    private static class RecordingListener implements ImageGetterAsyncTask.RequestStatusChanged {
        final List<String> events = new ArrayList<>();

        @Override
        public void onRequestProgress(Long currentProgressInPercent, Long fileSize, ImageGetterAsyncTask task) {
            events.add("progress:" + currentProgressInPercent + "/" + fileSize + "@" + threadTag());
        }

        @Override
        public void onRequestFinished(String resultFileName, ImageGetterAsyncTask task) {
            events.add("finished:" + resultFileName + "@" + threadTag());
        }
    }

    /** Tâche dont le doInBackground est stubbé : publie une progression puis renvoie un chemin, sans réseau. */
    private static ImageGetterAsyncTask newStubTask(final String resultPath) {
        DrawableWrapper wrapper = new DrawableWrapper(new ColorDrawable());
        return new ImageGetterAsyncTask(wrapper, "http://example.test/img.png", "/cache", true, false, false, false) {
            @Override
            protected String doInBackground(Void... params) {
                publishProgress(50L, 1000L);
                return resultPath;
            }
        };
    }

    @Test
    public void progressAndFinish_areForwardedOnMainLooper() {
        RecordingListener listener = new RecordingListener();
        ImageGetterAsyncTask task = newStubTask("/cache/img.png");
        task.setRequestStatusChangedListener(listener);

        task.executeOnExecutor(DIRECT_EXECUTOR);

        // Tout est posté sur le Looper principal mais rien n'est délivré tant qu'il ne tourne pas.
        assertTrue(listener.events.isEmpty());

        shadowOf(Looper.getMainLooper()).idle();

        assertEquals(List.of("progress:50/1000@main", "finished:/cache/img.png@main"), listener.events);
    }

    @Test
    public void teardownBeforeDelivery_dropsProgressAndFinish() {
        RecordingListener listener = new RecordingListener();
        ImageGetterAsyncTask task = newStubTask("/cache/img.png");
        task.setRequestStatusChangedListener(listener);

        task.executeOnExecutor(DIRECT_EXECUTOR);
        // Modélise ImageDownloader.stopAllCurrentTasks() pendant que les vignettes se chargent.
        task.setRequestStatusChangedListener(null);
        task.cancel();

        shadowOf(Looper.getMainLooper()).idle();

        assertTrue("Rien ne doit être livré après stopAllCurrentTasks().", listener.events.isEmpty());
    }
}
