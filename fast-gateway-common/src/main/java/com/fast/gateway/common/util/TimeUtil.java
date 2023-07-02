package com.fast.gateway.common.util;

import java.util.concurrent.TimeUnit;

/**
 * @author sheng
 * @create 2023-07-02 22:20
 */
public final class TimeUtil {
    private static volatile long currentTimeMillis;

    static {
        currentTimeMillis = System.currentTimeMillis();
        Thread daemon = new Thread((new Runnable() {
            @Override
            public void run() {
                for (;;) {
                // while (true) {
                    currentTimeMillis = System.currentTimeMillis();
                    try {
                        TimeUnit.MILLISECONDS.sleep(1);
                    } catch (Throwable e) {

                    }
                }
            }
        }));
        daemon.setDaemon(true);
        daemon.setName("common-fd-time-tick-thread");
        daemon.start();
    }

    public static long currentTimeMillis() {
        return currentTimeMillis;
    }
}
