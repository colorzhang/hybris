#Caching DTO beans with Spring cache and Redis

#Motivation
Hybris creates a lot of new DTO beans object every time when you access the storefront.
This impedes the responsive time and creates a lot of small short-live objects. 
This also gives the JVM high pressure to do the garbage collection.
You can consult the topic: converters and populators to get more info.

#Design
Caching the DTO beans in the Redis server.

#Implementation

#Verify
![DTO beans cached in Redis](images/DTO_beans_redis.png)

#Performance testing

#Next step

Winston Zhang
April 18, 2016
