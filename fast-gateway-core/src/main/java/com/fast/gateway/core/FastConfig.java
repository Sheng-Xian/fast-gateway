package com.fast.gateway.core;
import com.fast.gateway.common.constants.BasicConst;
import com.fast.gateway.common.util.NetUtils;
import lombok.Data;

/**
 * @author sheng
 * @create 2023-06-18 21:49
 */
@Data
public class FastConfig {
    //	网关的默认端口
    private int port = 8888;

    //	网关服务唯一ID： rapidId  192.168.11.111:8888
    private String fastId = NetUtils.getLocalIp() + BasicConst.COLON_SEPARATOR + port;

    //	网关的注册中心地址
    private String registerAddress = "http://192.168.11.114:2379,http://192.168.11.115:2379,http://192.168.11.116:2379";

    //	网关的命名空间：dev test prod
    private String nameSpace = "fast-dev";

    //	网关服务器的CPU核数映射的线程数
    private int processThread = Runtime.getRuntime().availableProcessors();

    // 	Netty的Boss线程数
    private int eventLoopGroupBossNum = 1;

    //	Netty的Work线程数
    private int eventLoopGroupWorkNum = processThread;

    //	是否开启EPOLL
    private boolean useEPoll = true;

    //	是否开启Netty内存分配机制
    private boolean nettyAllocator = true;

    //	http body报文最大大小
    private int maxContentLength = 67108864;

    //	dubbo开启连接数数量
    private int dubboConnections = processThread;

    //	设置响应模式, 默认是单异步模式：CompletableFuture回调处理结果： whenComplete  or  whenCompleteAsync
    private boolean whenComplete = true;

    //	网关队列配置：缓冲模式；
    private String bufferType = ""; // FastBufferHelper.FLUSHER;

    //	网关队列：内存队列大小
    private int bufferSize = 16384;

    //	网关队列：阻塞/等待策略
    private String waitStrategy = "blocking";


    //	Http Async 参数选项：

    //	连接超时时间
    private int httpConnectTimeout = 30000;

    //	请求超时时间
    private int httpRequestTimeout = 30000;

    //	客户端请求重试次数
    private int httpMaxRequestRetry = 2;

    //	客户端请求最大连接数
    private int httpMaxConnections = 10000;

    //	客户端每个地址支持的最大连接数
    private int httpConnectionsPerHost = 8000;

    //	客户端空闲连接超时时间, 默认60秒
    private int httpPooledConnectionIdleTimeout = 60000;

}
