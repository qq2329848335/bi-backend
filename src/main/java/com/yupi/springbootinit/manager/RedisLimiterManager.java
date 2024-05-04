package com.yupi.springbootinit.manager;

import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 专门提供RedisLimiter限流基础服务（提供了通用的能力）
 */
@Service
public class RedisLimiterManager {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 限流操作
     * @param key 区分不同的限流器，比如不同的用户id应该分别统计
     */
    public void doRateLimit(String key) {
        //创建一个名称为key的限流器，每秒最多访问2次
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);

        //RateType.OVERALL: 这表明设置的是全局的限流速率，而不是针对某个特定维度的速率。
        //1: 表示每秒允许处理的请求数或事件数为 1。
        //1: 表示这个速率是在 1 秒的窗口期内有效的。结合前面的数字，这意味着每 1 秒内允许通过 2 个请求。
        //RateIntervalUnit.SECONDS: 表示速率限制的时间单位是秒。
        rateLimiter.trySetRate(RateType.OVERALL, 1, 1, RateIntervalUnit.SECONDS);

        //每当一个操作来了之后，请求一个令牌
        boolean canOp = rateLimiter.tryAcquire(1);
        if (!canOp) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
        }
    }
}
