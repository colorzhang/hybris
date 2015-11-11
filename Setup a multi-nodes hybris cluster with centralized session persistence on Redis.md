#Setup a multi-nodes hybris cluster with centralized session persistence on Redis

```
本文是大规模分布式电商系统系列文章之一，规划中文章有：
＊ 典型hybris电商应用架构
＊ 大规模互联网与电商应用扩展原则
＊ 应用层无状态化与服务化
＊ 数据层读写分离与分库分表
＊ 分布式搜索与SolrCloud
＊ hybris大规模可扩展架构推荐实践
如有任何问题请联系交流讨论，Winston Zhang (colorzhang@gmail.com)
```

#Motivation
- Making hybris app stateless
- Higher scalability
- Centralized session persistence
- Load balance/failover
- Standalone Solr (start with one node)
- Media storage (in one node)

#Environment
##Software:
- Redis 3.0.5
- Spring Session 1.0.2
- hybris 5.7
- Nginx 1.8.0
- MariaDB 10.1.8

#Design
1 Nginx + 2 hybris app + 1 DB (Diagram)

#Installation & Configuration
##Install redis
Redis install
```bash
brew install redis
redis-server /usr/local/etc/redis.conf
```
