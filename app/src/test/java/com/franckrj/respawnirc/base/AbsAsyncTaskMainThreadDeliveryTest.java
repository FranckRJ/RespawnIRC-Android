package com.franckrj.respawnirc.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.os.Looper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Tests du chemin de livraison réel sur le thread principal (via Handler / Looper Android), que
 * les tests JVM purs ne peuvent pas exercer faute de vrai Looper. Robolectric en fournit un qu'on
 * fait avancer manuellement avec shadowOf(getMainLooper()).idle().
 *
 * Le travail de fond tourne en ligne (exécuteur direct) pour rester déterministe ; ce qu'on vérifie
 * ici, c'est que le résultat est posté sur le Looper principal puis délivré quand celui-ci tourne —
 * et qu'une annulation (cas de la rotation pendant un chargement) court-circuite la livraison.
 */
@RunWith(RobolectricTestRunner.class)
public class AbsAsyncTaskMainThreadDeliveryTest {
    private static final Executor DIRECT_EXECUTOR = Runnable::run;

    private static boolean onMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    private static class RecordingTask extends AbsAsyncTask<String, Void, String> {
        final List<String> events = new ArrayList<>();

        @Override
        protected String doInBackground(String... params) {
            events.add("background");
            return "ok";
        }

        @Override
        protected void onPostExecute(String result) {
            events.add("post:" + result + "@" + (onMainThread() ? "main" : "bg"));
        }

        @Override
        protected void onCancelled(String result) {
            events.add("cancelled@" + (onMainThread() ? "main" : "bg"));
        }
    }

    @Test
    public void onPostExecute_isPostedToMainLooper_andDeliveredWhenItRuns() {
        RecordingTask task = new RecordingTask();

        task.executeOnExecutor(DIRECT_EXECUTOR, "x");

        // Le résultat est posté sur le Looper principal mais pas encore délivré tant qu'il ne tourne pas.
        assertEquals(List.of("background"), task.events);
        assertEquals(AbsAsyncTask.Status.RUNNING, task.getStatus());

        // On fait tourner le Looper principal : finish() -> onPostExecute s'exécute, sur le thread principal.
        shadowOf(Looper.getMainLooper()).idle();

        assertEquals(List.of("background", "post:ok@main"), task.events);
        assertEquals(AbsAsyncTask.Status.FINISHED, task.getStatus());
    }

    @Test
    public void cancelBeforeLooperRuns_routesToOnCancelledInsteadOfOnPostExecute() {
        RecordingTask task = new RecordingTask();

        task.executeOnExecutor(DIRECT_EXECUTOR, "x");
        // Annulation alors que la livraison est encore en file d'attente sur le Looper principal.
        task.cancel();

        shadowOf(Looper.getMainLooper()).idle();

        assertEquals(List.of("background", "cancelled@main"), task.events);
    }

    @Test
    public void clearListenersAndCancel_beforeDelivery_dropsResult() {
        // Modélise une rotation pendant un chargement web : la requête est en vol, l'activité est
        // détruite (clearListenersAndCancel), puis le résultat arrive sur le Looper principal.
        final List<String> received = new ArrayList<>();
        AbsWebRequestAsyncTask<String, Void, String> task = new AbsWebRequestAsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                return "result";
            }
        };
        task.setRequestIsFinishedListener(received::add);

        task.executeOnExecutor(DIRECT_EXECUTOR, "url");
        // Rotation : on coupe les listeners et on annule avant que le Looper ne délivre.
        task.clearListenersAndCancel();

        shadowOf(Looper.getMainLooper()).idle();

        assertTrue("Aucun résultat ne doit être livré après clearListenersAndCancel().", received.isEmpty());
        assertTrue(task.isCancelled());
    }
}
