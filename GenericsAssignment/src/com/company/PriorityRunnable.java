package com.company;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This class defines a Runnable task (eventually will run in separate thread)
 * and gives it a priority
 *
 * Strategy pattern:
 * 1. implement an interface
 * 2. declare a data member of the same type of the interface
 * 3. get the target in the constructor/Setter method
 * 4. delegate functionality to the target in the method that we override
 *
 * Your system support only Runnable tasks
 * It uses a method called start()
 * There is a PriorityBlockQueue<> of Runnable objects
 *
 *
 */
public class PriorityRunnable implements Runnable, Comparable<PriorityRunnable> {
    private static final int DEFAULT_PRIORITY = 0;
    private Runnable target;
    private int priority;

    public PriorityRunnable(Runnable task,int oPriority){
        this.target = task;
        this.priority = oPriority;
    }

    // backwards compatibility, using default priority
    public PriorityRunnable(Runnable task){
        this(task,DEFAULT_PRIORITY);
    }

    /**
     *
     * @param pRunnable is an instance of PriorityRunnable
     * This method compares the priority of 2 objects of the class using
     * their priority data member
     * @return
     * 0 if priority is the same
     * -1 if this.priority < pRunnable.priority
     * 1 if this.priority  > pRunnable.priority
     */
    @Override
    public int compareTo(PriorityRunnable pRunnable) {
        return Integer.compare(this.priority,pRunnable.priority);
    }

    @Override
    public void run() {
        if(target!=null)
            target.run();
    }

    public int getPriority(){
        return this.priority;
    }

    public static void main(String[] args) {
        PriorityRunnable pr1 = new PriorityRunnable(
                ()-> System.out.println(Runtime.getRuntime().totalMemory())
                ,5);


        PriorityRunnable pr2 = new PriorityRunnable(
                ()-> System.out.println(new StringBuilder("ABCDEFGHIJK")
                        .reverse().toString()));

        System.out.println(pr1.compareTo(pr2)); // expected: 1

        Thread worker = new Thread(pr1);
        worker.start();

        ExecutorService threadPool =
                new ThreadPoolExecutor(2,2,3,
                        TimeUnit.MILLISECONDS, new PriorityBlockingQueue<>(10));

        threadPool.execute(pr1);
        threadPool.execute(pr1);
        threadPool.execute(pr2);
        threadPool.execute(pr1);
        threadPool.execute(pr2);
        threadPool.execute(pr2);

    }
}

