package com.franckrj.respawnirc.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Stress test de l'exécuteur série maison (équivalent de AsyncTask.SERIAL_EXECUTOR).
 *
 * L'exécuteur s'appuie sur un pool de threads parallèle ; on vérifie ici les deux garanties
 * qu'il doit apporter par-dessus ce pool :
 *   - exclusion mutuelle : jamais deux tâches en même temps ;
 *   - ordre FIFO pour les soumissions issues d'un même thread.
 *
 * Ce test est du Java pur (aucune dépendance Android), il exerce donc le vrai multithreading.
 */
public class SerialExecutorTest {
    @Test
    public void runsTasksOneAtATime_underConcurrentSubmission() throws InterruptedException {
        final int producerCount = 8;
        final int tasksPerProducer = 200;
        final int totalTasks = producerCount * tasksPerProducer;

        AbsAsyncTask.SerialExecutor executor = new AbsAsyncTask.SerialExecutor();

        final AtomicInteger currentlyRunning = new AtomicInteger(0);
        final AtomicInteger maxObservedConcurrency = new AtomicInteger(0);
        final AtomicInteger completedTasks = new AtomicInteger(0);
        final CountDownLatch allDone = new CountDownLatch(totalTasks);

        final Runnable task = () -> {
            int running = currentlyRunning.incrementAndGet();
            maxObservedConcurrency.accumulateAndGet(running, Math::max);
            // Petite fenêtre pour rendre détectable une exécution concurrente si elle se produisait.
            try {
                Thread.sleep(0, 100_000);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            currentlyRunning.decrementAndGet();
            completedTasks.incrementAndGet();
            allDone.countDown();
        };

        // Plusieurs threads soumettent en même temps pour stresser la synchronisation de l'exécuteur.
        final CountDownLatch startGun = new CountDownLatch(1);
        final List<Thread> producers = new ArrayList<>();
        for (int p = 0; p < producerCount; ++p) {
            Thread producer = new Thread(() -> {
                try {
                    startGun.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                for (int i = 0; i < tasksPerProducer; ++i) {
                    executor.execute(task);
                }
            });
            producer.start();
            producers.add(producer);
        }

        startGun.countDown();
        for (Thread producer : producers) {
            producer.join();
        }

        assertTrue("Toutes les tâches auraient dû se terminer dans le délai imparti.",
                allDone.await(30, TimeUnit.SECONDS));
        assertEquals("Chaque tâche soumise doit s'exécuter exactement une fois.",
                totalTasks, completedTasks.get());
        assertEquals("L'exécuteur série ne doit jamais exécuter deux tâches simultanément.",
                1, maxObservedConcurrency.get());
    }

    @Test
    public void preservesSubmissionOrderFromSingleThread() throws InterruptedException {
        final int taskCount = 1000;
        AbsAsyncTask.SerialExecutor executor = new AbsAsyncTask.SerialExecutor();

        final List<Integer> executionOrder = Collections.synchronizedList(new ArrayList<>(taskCount));
        final CountDownLatch allDone = new CountDownLatch(taskCount);

        for (int i = 0; i < taskCount; ++i) {
            final int index = i;
            executor.execute(() -> {
                executionOrder.add(index);
                allDone.countDown();
            });
        }

        assertTrue("Toutes les tâches auraient dû se terminer dans le délai imparti.",
                allDone.await(30, TimeUnit.SECONDS));

        final List<Integer> expectedOrder = new ArrayList<>(taskCount);
        for (int i = 0; i < taskCount; ++i) {
            expectedOrder.add(i);
        }
        assertEquals("Les tâches soumises depuis un seul thread doivent s'exécuter dans l'ordre (FIFO).",
                expectedOrder, executionOrder);
    }
}
