package com.company;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * This class enables the conversion of both Runnable and Callable tasks
 * to a PriorityRunnable object.
 * A PriorityRunnable tasks wraps a Runnable and assigns it with a priority.
 * The various apply methods convert Runnable or Callable tasks and
 * submit them (using the offer method) to a PriorityBlockingQueue
 * An instance of the EnhancedGenerics class creates a separate thread and while it is not stopped
 * tries to take a task from the queue and run it.
 * Complete the methods marked with TODO. Use main method to test your code
 * @param <T> A Runnable tasks or an instance of a type that implements the Runnable interface
 */

public class EnhancedGenerics<T extends Runnable> { // Runnable סוג של
    protected boolean stop = false;
    protected boolean stopNow = false;
    protected final BlockingQueue<T> taskQueue;// תור משימות חוסם
    protected final Thread consumerThread; // לקחת דברים מטור המשימות ולהריץ אותם
    protected final Function<Runnable, T> defaultFunction; // T <- Runnable פונקצית מיפוי

    //private static volatile EnhancedGenerics singleGenericService;
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(); // מנעול

    public EnhancedGenerics(Function<Runnable, T> runnableTFunction) { // מקבל פונקצית מיפוי
        this(new LinkedBlockingQueue<>(10), runnableTFunction);
    }

    public EnhancedGenerics(BlockingQueue<T> paramBlockingQueue,
                            Function<Runnable, T> runnableTFunction) {//מקבל תור חוסם ופונקצית מיפוי
        throwIfNull(paramBlockingQueue, runnableTFunction);
        this.taskQueue = paramBlockingQueue;
        this.defaultFunction = runnableTFunction;
        this.consumerThread = new Thread( // הרצה למשימה מהתור ומריץ אותה עד עצירה
                () -> {
                    while ((!stop || !this.taskQueue.isEmpty()) &&
                            (!stopNow)) {
                        try {
                            taskQueue.take().run();
                        } catch (InterruptedException e) {
                            //e.printStackTrace();
                        }
                    }
                });
        this.consumerThread.start();
    }

    /**
     * @param objects pass unknown number of arguments passed in run-time
     * @throws NullPointerException
     */
    public static void throwIfNull(Object... objects) throws NullPointerException {
        for (Object argument : objects) {
            if (argument == null) {
                throw new NullPointerException("one of the arguments is null");
            }
        }
    }

    public void apply(final Runnable runnable) throws InterruptedException {
        this.apply(runnable, defaultFunction);
    }

    public <V> Future<V> apply(final Callable<V> callable) throws InterruptedException {
        return this.apply(callable, defaultFunction);
    }

    /**
     * TODO: UPGRADE THIS METHOD
     *
     * @param runnable
     * @param runnableTFunction can be Null, if Null will use the default converter
     * @throws InterruptedException
     */
    public void apply(final Runnable runnable, Function<Runnable, T> runnableTFunction) throws InterruptedException {
        if (runnableTFunction == null) {
                readWriteLock.writeLock().lock(); // לנעול
                if (runnableTFunction == null) {
                    runnableTFunction = this.defaultFunction;
            }
            readWriteLock.writeLock().unlock(); // לבטל נעילה
        }
        taskQueue.offer(runnableTFunction.apply(runnable)); // להציע לתור משימה לביצוע
    }

    public<V> Future<V> apply(final Callable<V> callable,Function<Runnable,T> runnableTFunction) throws InterruptedException {
        /*
        TODO: COMPLETE THIS METHOD
        Hint:
        1. You may use a FutureTask Object
        2. You may also use the method above
         */
        FutureTask<V> fComputation = new FutureTask<>(callable);
        apply(fComputation, runnableTFunction);
        return fComputation;
    }

    /**
     * TODO: COMPLETE THIS METHOD
     * This method drains the task queue and
     * @return unhandled tasks
     */
    public List<Runnable> drain(){
        List<Runnable> tasks = new ArrayList<Runnable>();
        return tasks;
    }

    /**
     * TODO: COMPLETE THIS METHOD
     * This method waits for current executing task then stops worker thread
     * @param wait - if true, wait till execution of the current task is completed using
     * the waitUntilDone method
     * HINT: This method should use the interrupt() method on the worker thread
     * @throws InterruptedException
     */
    public void stop(boolean wait) throws InterruptedException{
        stop = true;
        this.consumerThread.interrupt();
        if (wait) {
            waitUntilDone();
        }
    }

    /**
     * TODO: COMPLETE THIS METHOD
     * This method should be invoked if wait flag for the stop method is true
     * HINT: Use join() method if thread is alive
     * @throws InterruptedException
     */
    public void waitUntilDone() throws InterruptedException {
        if (this.consumerThread.isAlive()) {
            this.consumerThread.join();
        }
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        EnhancedGenerics<PriorityRunnable> service =
                new EnhancedGenerics<PriorityRunnable>(new PriorityBlockingQueue<>(),
                        aRunnableTask -> new PriorityRunnable(aRunnableTask, 1)); // פונקצית מיפוי דיפולטיבית
        /*
         submit Runnable tasks to to the queue (as PriorityRunnable objects) using
         the apply methods above
         */
        service.apply(() -> System.out.println(
                "There are more than 2 design patterns in this class"),
                runnable -> new PriorityRunnable(runnable,1));

        service.apply(() -> System.out.println("a runnable"));

        service.apply(new Runnable() {
            @Override
            public void run() {
                System.out.println("Fun");
            }
                }, runnable -> new PriorityRunnable(runnable,5));

        Callable<String> stringCallable= () -> {
            try {
                Thread.sleep(5000); // wait until interrupt
            } catch (InterruptedException e) {
                System.out.println("interrupted");
            }
            return "callable string";
        };
        Future<String> futureString = service.apply(stringCallable);
        Future<String> anotherFutureString = service.apply(stringCallable);

        System.out.println(futureString.get());
        System.out.println(anotherFutureString.get());
        service.stop(false);
        System.out.println("done");
    }
}

