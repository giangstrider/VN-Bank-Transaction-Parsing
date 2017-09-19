package vn.com.momo.app;

import lombok.extern.log4j.Log4j2;

import java.util.concurrent.*;

/**
 * Created by anhvunguyen on 28/02/2017.
 */
@Log4j2
public class CustomThreadPoolExecutor extends ThreadPoolExecutor {

    public CustomThreadPoolExecutor(int nThreads) {
        super(nThreads, nThreads, 5000L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10000),
                new CallerRunsPolicy());
    }

    @Override
    protected void afterExecute(Runnable runnable, Throwable throwable) {
        super.afterExecute(runnable, throwable);

        if (throwable == null && runnable instanceof Future<?>) {
            try {
                ((Future<?>) runnable).get();
            } catch (CancellationException ce) {
                throwable = ce;
            } catch (ExecutionException ee) {
                throwable = ee;
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                throwable = e;
            }
        }

        if (throwable != null) {
            log.error(throwable);
        }

    }

}
