#Make hybris OCC API asynchronize with Netty

#Motivation
- high throughput
- high concurrency
- high scalability
- seperation of connection and business thread pool
- less resource utilization

#Environment
##Software
- SAP Hybris commerce suite 6.0 beta
- Netty 4.1 CR2
- Gatling 2.1

##Design

#Implementation

#Stress testing

![Default OCC 100 concurrency stat](images/y100-stat.png)
![Netty OCC 100 concurrency stat](images/netty-100-stat.png)

#Conclusion

#Open issues
- some logic is hard coded

#Next step
- Full funcational testing
- Move part of OAUTH2 logic to Nginx + Lua
- Modulize as an extension

#References
[Netty project](http://netty.io)

Winston Zhang (colorzhang@gmail.com)

Feburary 14, 2016
