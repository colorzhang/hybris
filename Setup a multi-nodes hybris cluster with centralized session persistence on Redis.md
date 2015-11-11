#Setup a multi-nodes hybris cluster with centralized session persistence on Redis

#Motivation
Making hybris app stateless
Higher scalability
Centralized session persistence
Load balance/failover
Standalone Solr (start with one node)
Media storage (in one node)

#Environment
##Software:
Redis 3.0.5
Spring Session 1.0.2
hybris 5.7
Nginx 1.8.0
MariaDB 10.1.8

#Design
1 Nginx + 2 hybris app + 1 DB (Diagram)

#Installation & Configuration
##Install redis
Redis install
```bash
brew install redis
redis-server /usr/local/etc/redis.conf
```
