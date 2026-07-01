package com.franckrj.respawnirc.base;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;

import java.util.ArrayDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Remplacement maison de android.os.AsyncTask, déprécié depuis Android 11.
 *
 * L'API publique reproduit volontairement celle d'AsyncTask (execute / executeOnExecutor /
 * cancel / isCancelled / getStatus / publishProgress ainsi que les callbacks onPreExecute /
 * doInBackground / onProgressUpdate / onPostExecute / onCancelled) afin de limiter au maximum
 * l'impact sur les classes existantes.
 *
 * Le travail s'exécute sur un Executor (par défaut un exécuteur série, comme AsyncTask.execute)
 * et les callbacks de cycle de vie sont délivrés sur le thread principal.
 */
public abstract class AbsAsyncTask<Params, Progress, Result> {
    public enum Status {
        PENDING,
        RUNNING,
        FINISHED
    }

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE_SECONDS = 30;

    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        private final AtomicInteger count = new AtomicInteger(1);

        @Override
        public Thread newThread(@NonNull Runnable runnable) {
            return new Thread(runnable, "AbsAsyncTask #" + count.getAndIncrement());
        }
    };

    /**
     * Équivalent de AsyncTask.THREAD_POOL_EXECUTOR : exécution en parallèle.
     * <p>
     * <b>Attention</b> : comme AsyncTask, la file d'attente est bornée (128). Si plus de
     * MAXIMUM_POOL_SIZE + 128 tâches sont en vol simultanément, execute() lèvera une
     * RejectedExecutionException. Ça n'arrive pas avec les usages actuels (le chemin par défaut
     * passe par SERIAL_EXECUTOR qui ne soumet qu'une tâche à la fois, et ImageDownloader limite
     * lui-même à 8 téléchargements concurrents) ; à garder en tête si on soumet un jour de gros
     * volumes directement sur ce pool.
     */
    public static final Executor THREAD_POOL_EXECUTOR;

    static {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(128), THREAD_FACTORY);
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        THREAD_POOL_EXECUTOR = threadPoolExecutor;
    }

    /** Équivalent de AsyncTask.SERIAL_EXECUTOR : exécution séquentielle, utilisé par défaut. */
    private static final Executor SERIAL_EXECUTOR = new SerialExecutor();

    private static Handler mainHandlerInstance = null;

    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private volatile Status status = Status.PENDING;

    // Permet d'injecter un exécuteur de thread principal synchrone pour les tests.
    private Executor mainThreadExecutor = null;

    /*
     * Callbacks du cycle de vie (patron de méthode, comme android.os.AsyncTask). Ils sont
     * appelés par la mécanique de cette classe (execute / publishProgress / finish) et sont
     * destinés à être surchargés par les sous-classes : doInBackground est obligatoire, les
     * autres ont une implémentation vide par défaut pour ne surcharger que ce dont on a besoin.
     * Ils paraissent inutilisés ici mais ils le sont depuis l'extérieur :
     *   - onPreExecute      : surchargé par AbsWebRequestAsyncTask ;
     *   - onPostExecute     : surchargé par AbsWebRequestAsyncTask et ImageGetterAsyncTask ;
     *   - onProgressUpdate  : surchargé par ImageGetterAsyncTask ;
     *   - onCancelled       : point d'extension pour un nettoyage à l'annulation (aucune
     *                         sous-classe ne l'utilise pour l'instant, conservé par parité
     *                         avec AsyncTask et exercé par les tests).
     */

    // @SuppressWarnings("unchecked") : « heap pollution » inhérente aux varargs de type
    // générique (Params...). @SafeVarargs serait l'annotation idéale mais le langage l'interdit
    // sur une méthode surchargeable (abstraite ici). L'usage est sûr : on ne fait que lire le
    // tableau reçu, jamais y écrire ni l'exposer.
    @WorkerThread
    @SuppressWarnings("unchecked")
    protected abstract Result doInBackground(Params... params);

    @MainThread
    protected void onPreExecute() {
    }

    @MainThread
    protected void onPostExecute(Result result) {
    }

    // Voir doInBackground pour l'explication de @SuppressWarnings("unchecked").
    @MainThread
    @SuppressWarnings("unchecked")
    protected void onProgressUpdate(Progress... values) {
    }

    @MainThread
    protected void onCancelled(Result result) {
    }

    public final Status getStatus() {
        return status;
    }

    public final boolean isCancelled() {
        return cancelled.get();
    }

    /**
     * Annulation <b>coopérative</b> : positionne simplement un drapeau que doInBackground doit
     * consulter via {@link #isCancelled()} (et que WebManager surveille via le Callable passé à
     * initWebInfos). Une fois la tâche annulée, finish() appelle onCancelled() au lieu de
     * onPostExecute().
     * <p>
     * Contrairement à android.os.AsyncTask, il n'y a volontairement pas de paramètre
     * {@code mayInterruptIfRunning} : le thread de fond n'est jamais interrompu, donc on ne propose
     * pas une option qu'on ignorerait (passer {@code true} deviendrait silencieusement faux). Si une
     * vraie interruption devient nécessaire un jour, il faudra conserver le Future renvoyé par
     * l'Executor dans executeOnExecutor() et l'annuler ici.
     */
    public final void cancel() {
        cancelled.set(true);
    }

    @MainThread
    @SafeVarargs
    public final AbsAsyncTask<Params, Progress, Result> execute(Params... params) {
        return executeOnExecutor(SERIAL_EXECUTOR, params);
    }

    @MainThread
    @SafeVarargs
    public final AbsAsyncTask<Params, Progress, Result> executeOnExecutor(Executor executor, Params... params) {
        if (status != Status.PENDING) {
            throw new IllegalStateException("Cannot execute task: the task is already " +
                    (status == Status.RUNNING ? "running." : "finished."));
        }
        status = Status.RUNNING;
        onPreExecute();
        executor.execute(() -> {
            final Result result = doInBackground(params);
            postToMainThread(() -> finish(result));
        });
        return this;
    }

    @WorkerThread
    @SafeVarargs
    protected final void publishProgress(Progress... values) {
        if (!isCancelled()) {
            postToMainThread(() -> onProgressUpdate(values));
        }
    }

    @MainThread
    private void finish(Result result) {
        if (isCancelled()) {
            onCancelled(result);
        } else {
            onPostExecute(result);
        }
        status = Status.FINISHED;
    }

    private void postToMainThread(Runnable action) {
        if (mainThreadExecutor != null) {
            mainThreadExecutor.execute(action);
        } else {
            getMainHandler().post(action);
        }
    }

    private static synchronized Handler getMainHandler() {
        if (mainHandlerInstance == null) {
            mainHandlerInstance = new Handler(Looper.getMainLooper());
        }
        return mainHandlerInstance;
    }

    @VisibleForTesting
    void setMainThreadExecutorForTesting(Executor newMainThreadExecutor) {
        mainThreadExecutor = newMainThreadExecutor;
    }

    /**
     * Exécuteur série : enchaîne les tâches une à une sur le pool de threads,
     * comme le faisait l'exécuteur par défaut d'AsyncTask.
     */
    @VisibleForTesting
    static final class SerialExecutor implements Executor {
        private final ArrayDeque<Runnable> tasks = new ArrayDeque<>();
        private Runnable active = null;

        @Override
        public synchronized void execute(@NonNull final Runnable runnable) {
            tasks.offer(() -> {
                try {
                    runnable.run();
                } finally {
                    scheduleNext();
                }
            });
            if (active == null) {
                scheduleNext();
            }
        }

        private synchronized void scheduleNext() {
            if ((active = tasks.poll()) != null) {
                THREAD_POOL_EXECUTOR.execute(active);
            }
        }
    }
}
