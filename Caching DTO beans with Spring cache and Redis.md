#Caching DTO beans with Spring cache and Redis

#Motivation
Hybris creates a lot of new DTO beans object every time when you access the storefront.
This impedes the responsive time and creates a lot of small short-live objects. 
This also gives the JVM high pressure to do the garbage collection.
You can consult the topic: converters and populators to get more info.

:warning::warning::warning: 
In-memory cache usually has better performance than network distributed cache, nevertheless distributed cache comes with better scalability. You can switch back and forth different cache providers easily based on spring cache abstration.
:warning::warning::warning:

#Environment
##Software:
- Redis 3.0.7
- Spring framework 4.1.7 embedded with hybris
- Spring data redis 1.7.1
- hybris 6.0
- MariaDB 10.1.13

#Design
- Caching the DTO beans in the Redis server.

- Using Spring AOP to add cache apsect directly. no need to change any code. (some converters have complex data structure, maybe needs to change code then.)

- Set different TTL for different data type using cacheManager.

#Implementation
##Install & start Redis
```bash
brew install redis
redis-server /usr/local/etc/redis.conf
```

##Config spring cache
```xml
<beans>
...

    <cache:annotation-driven />

    <bean id="jedisConnFactory" class="org.springframework.data.redis.connection.jedis.JedisConnectionFactory"
          p:usePool="true"/>

    <bean id="stringRedisSerializer" class="org.springframework.data.redis.serializer.StringRedisSerializer"/>
    <bean id="int2StringRedisSerializer" class="org.springframework.data.redis.serializer.GenericToStringSerializer"
          c:type="java.lang.Integer"/>
    <bean id="genericJackson2JsonRedisSerializer" class="org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer"/>


    <bean id="redisTemplate" class="org.springframework.data.redis.core.RedisTemplate"
          p:connectionFactory-ref="jedisConnFactory"
          p:keySerializer-ref="stringRedisSerializer"
          p:hashKeySerializer-ref="stringRedisSerializer"
          p:valueSerializer-ref="genericJackson2JsonRedisSerializer"
          p:hashValueSerializer-ref="genericJackson2JsonRedisSerializer"/>


    <bean id="cacheManager" class="org.springframework.data.redis.cache.RedisCacheManager"
          c:redisOperations-ref="redisTemplate"
          p:defaultExpiration="600"
          p:usePrefix="true"/>



    <cache:advice id="cacheAdvice">
        <cache:caching cache="beans:cache">
            <cache:cacheable method="convert" key="#source.class.getName().concat(':').concat(#source.getPk().getLongValueAsString())"/>
        </cache:caching>
    </cache:advice>

    <aop:config>
        <aop:advisor advice-ref="cacheAdvice" pointcut="bean(customerConverter) || bean(productConverter)"/>
    </aop:config>
    
</beans>
```

#Verify
![DTO beans cached in Redis](images/DTO_cache_redis.png)

#Performance evaluation
I did a 10 user load testing for 1 min accessing digital camera product listing page.

| No. | Guava-CPU | Guava-Memory | Guava-TPS | Redis-CPU | Redis-Memory | Redis-TPS | hybris-CPU | hybris-Memory | hybris-TPS | 
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 |  |  | 43 |  |  | 23 |  |  | 34 |
| 2 |  |  | 32 |  |  | 35 |  |  | 41 |
| 3 |  |  | 40 |  |  | 43 |  |  | 42 |
| Average |  |  | 38 |  |  | 34 |  |  | 39 |

#Next step

Winston Zhang

April 18, 2016
