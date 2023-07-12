package com.fast.gateway.core;

import com.fast.gateway.common.constants.BasicConst;
import com.fast.gateway.common.constants.FastBufferHelper;
import com.fast.gateway.common.util.NetUtils;
import com.lmax.disruptor.*;
import lombok.Data;

/**
 * @author sheng
 * @create 2023-06-18 21:49
 */
@Data
public class FastConfig {
    // Default port for the gateway
    private int port = 8888;

    // Unique ID for the gateway service: rapidId  192.168.11.111:8888
    private String fastId = NetUtils.getLocalIp() + BasicConst.COLON_SEPARATOR + port;

    // Address of the gateway's registration center
    private String registerAddress = "http://192.168.11.114:2379,http://192.168.11.115:2379,http://192.168.11.116:2379";

    // Namespace for the gateway: dev, test, prod
    private String nameSpace = "fast-dev";

    // Number of threads mapped to the CPU cores of the gateway server
    private int processThread = Runtime.getRuntime().availableProcessors();

    // Number of Netty's Boss threads
    private int eventLoopGroupBossNum = 1;

    // Number of Netty's Work threads
    private int eventLoopGroupWorkNum = processThread;

    // Enable EPOLL (Linux-specific transport) if available
    private boolean useEPoll = true;

    // Enable Netty's memory allocation mechanism
    private boolean nettyAllocator = true;

    // Maximum size of HTTP body content
    private int maxContentLength = 67108864;

    // Number of Dubbo connections to open
    private int dubboConnections = processThread;

    // Set the response mode, default is single asynchronous mode using CompletableFuture callbacks: whenComplete or whenCompleteAsync
    private boolean whenComplete = true;

    // Gateway queue configuration: buffering mode
    private String bufferType = FastBufferHelper.FLUSHER; //FLUSHER

    // Gateway queue: size of the in-memory queue
    private int bufferSize = 16384;

    // Gateway queue: blocking/wait strategy
    private String waitStrategy = "blocking";

    public WaitStrategy getATrueWaitStrategy() {
        switch (waitStrategy) {
            case "blocking":
                return new BlockingWaitStrategy();
            case "busySpin":
                return new BusySpinWaitStrategy();
            case "sleep":
                return new SleepingWaitStrategy();
            case "yielding" :
                return new YieldingWaitStrategy();
            default:
                return new BlockingWaitStrategy();
        }
    }

    // Http Async parameters:

    // Connection timeout
    private int httpConnectTimeout = 30000;

    // Request timeout
    private int httpRequestTimeout = 30000;

    // Maximum number of request retries for the client
    private int httpMaxRequestRetry = 2;

    // Maximum number of client connections
    private int httpMaxConnections = 10000;

    // Maximum number of connections per host for the client
    private int httpConnectionsPerHost = 8000;

    // Idle connection timeout for pooled connections, default is 60 seconds
    private int httpPooledConnectionIdleTimeout = 60000;


}
