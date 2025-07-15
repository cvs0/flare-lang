package net.cvs0.flare;

/**
 * Represents a handle to a running fiber (thread).
 */
public class FiberFHandle {
    private final Thread thread;

    public FiberFHandle(Thread thread) {
        this.thread = thread;
    }

    /**
     * Waits for the fiber to finish.
     */
    public void await() throws InterruptedException {
        thread.join();
    }

    /**
     * Returns true if the fiber is still running.
     */
    public boolean isAlive() {
        return thread.isAlive();
    }

    /**
     * Returns the underlying thread (for advanced use).
     */
    public Thread getThread() {
        return thread;
    }

    /**
     * Returns the status of the fiber: "running", "finished", or "unknown".
     */
    public String getStatus() {
        if (thread == null) return "unknown";
        if (thread.isAlive()) return "running";
        return "finished";
    }
}
