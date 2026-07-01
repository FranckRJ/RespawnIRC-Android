package com.franckrj.respawnirc.base;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Vérifie le cycle de vie du remplacement d'AsyncTask.
 *
 * On injecte des exécuteurs synchrones (le travail de fond et le « thread principal » s'exécutent
 * en ligne) pour rendre les tests déterministes, sans dépendre d'un Looper Android.
 */
public class AbsAsyncTaskTest {
    private static final Executor DIRECT_EXECUTOR = Runnable::run;

    /** Tâche de test qui journalise chaque étape de son cycle de vie. */
    private static class RecordingTask extends AbsAsyncTask<String, Integer, String> {
        final List<String> events = new ArrayList<>();
        String[] receivedParams = null;
        boolean throwInBackground = false;

        RecordingTask() {
            setMainThreadExecutorForTesting(DIRECT_EXECUTOR);
        }

        @Override
        protected void onPreExecute() {
            events.add("pre");
        }

        @Override
        protected String doInBackground(String... params) {
            receivedParams = params;
            events.add("background");
            publishProgress(1, 2);
            return "result:" + (params.length > 0 ? params[0] : "");
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            events.add("progress:" + values.length);
        }

        @Override
        protected void onPostExecute(String result) {
            events.add("post:" + result);
        }

        @Override
        protected void onCancelled(String result) {
            events.add("cancelled:" + result);
        }
    }

    private static RecordingTask runSynchronously(RecordingTask task, String... params) {
        task.executeOnExecutor(DIRECT_EXECUTOR, params);
        return task;
    }

    @Test
    public void execute_runsFullLifecycleInOrder() {
        RecordingTask task = runSynchronously(new RecordingTask(), "abc");

        assertEquals(
                List.of("pre", "background", "progress:2", "post:result:abc"),
                task.events);
        assertArrayEquals(new String[]{"abc"}, task.receivedParams);
    }

    @Test
    public void status_progressesFromPendingToFinished() {
        RecordingTask task = new RecordingTask();
        assertEquals(AbsAsyncTask.Status.PENDING, task.getStatus());

        runSynchronously(task, "x");
        assertEquals(AbsAsyncTask.Status.FINISHED, task.getStatus());
    }

    @Test
    public void cancelBeforeExecute_skipsPostAndCallsOnCancelled() {
        RecordingTask task = new RecordingTask();
        task.cancel();

        runSynchronously(task, "y");

        assertTrue(task.isCancelled());
        // Pas de progression ni de onPostExecute quand la tâche est annulée.
        assertEquals(List.of("pre", "background", "cancelled:result:y"), task.events);
    }

    @Test
    public void isCancelled_isFalseByDefault() {
        assertFalse(new RecordingTask().isCancelled());
    }

    @Test
    public void executeTwice_throwsIllegalState() {
        RecordingTask task = runSynchronously(new RecordingTask(), "once");

        try {
            task.executeOnExecutor(DIRECT_EXECUTOR);
            fail("Une seconde exécution aurait dû lever IllegalStateException.");
        } catch (IllegalStateException expected) {
            // Comportement attendu.
        }
    }
}
