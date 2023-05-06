package bt.search.analyzer.common;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AsyncLock {
    private static Map<Long, Object> ThreadCondition;

    static {
        ThreadCondition = new HashMap<>();  // 不会出现脏读现象，不需要线程安全
    }

    public static void await(Long id) throws InterruptedException {
        Object obj = new Object();
        ThreadCondition.put(id, obj);
        synchronized (obj) {
            obj.wait(10000);
        }
    }

    public static void signalAll(Long id) {
        Object obj = ThreadCondition.get(id);
        if (obj != null) {
            synchronized (obj) {
                obj.notifyAll();
            }
            ThreadCondition.remove(id);
        }
    }

    public static void createLock(Long id) {
        Object obj = new Object();
        ThreadCondition.put(id, obj);
    }

    public static void getLockAndWait(Long id) throws InterruptedException {
        Object obj = ThreadCondition.get(id);
        synchronized (obj) {
            obj.wait(100000);
        }
    }

    public static boolean exist(Long id) {
        return ThreadCondition.containsKey(id);
    }

    public static void remove(Long id) {
        if (ThreadCondition.containsKey(id)) {
            ThreadCondition.remove(id);
        }
    }
}
