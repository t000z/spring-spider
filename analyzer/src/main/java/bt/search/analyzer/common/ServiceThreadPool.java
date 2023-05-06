package bt.search.analyzer.common;

import java.util.concurrent.*;

public final class ServiceThreadPool {
    private static ExecutorService executorService = null;
    private static int useService = 0;

    public static ExecutorService createExecutorService() {
        useService++;
        return executorService;
    }

    public static void shutdown(ExecutorService executorService) {
        useService--;
        if (useService <= 0) {
            executorService.shutdown();
        }
    }

    static {
        executorService = new ThreadPoolExecutor(4,
                30, 1, TimeUnit.HOURS,
                new LinkedBlockingQueue(),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }
}
