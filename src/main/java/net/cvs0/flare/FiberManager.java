package net.cvs0.flare;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FiberManager {
    private final Map<String, Thread> fibers = new ConcurrentHashMap<>();

    public void register(String name, Thread thread) {
        fibers.put(name, thread);
    }

    public Thread get(String name) {
        return fibers.get(name);
    }

    public boolean isRunning(String name) {
        Thread t = fibers.get(name);
        return t != null && t.isAlive();
    }

    public void joinAll() {
        for (Thread t : fibers.values()) {
            try {
                if (t != null) t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

   // TODO: pause(), join()
}
