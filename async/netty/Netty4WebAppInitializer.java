package de.hybris.platform.ycommercewebservices.web;

import de.hybris.platform.core.Registry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.ServletContext;

/**
 * Created by i306724 on 10/2/2016.
 */

public class Netty4WebAppInitializer implements WebApplicationInitializer {

    static final int PORT = 8088;

    @Override
    public void onStartup(ServletContext container) {

        MockServletConfig servletConfig = new MockServletConfig(container);

        //load root application context
        //container.addListener(new HybrisContextLoaderListener());

        // Child dispatcher application context
        AnnotationConfigWebApplicationContext wac = new AnnotationConfigWebApplicationContext();
        wac.setParent(Registry.getApplicationContext());
        wac.setServletContext(container);
        wac.setServletConfig(servletConfig);
        //wac.register(de.hybris.platform.ycommercewebservices.v2.config.WebConfig.class);
        wac.register(WebConfig.class);
        //wac.refresh();


        DispatcherServlet dispatcherServlet = new DispatcherServlet(wac);

        Runnable runnable = () -> {
            Registry.activateMasterTenant();
            try {
                dispatcherServlet.init(servletConfig);
                start(dispatcherServlet, container);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        };

        Thread t = new Thread(runnable);
        t.start();
    }

    public void start(DispatcherServlet dispatcherServlet, ServletContext servletContext) throws Exception {

        // Configure SSL.
        final SslContext sslCtx;
        if (SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslCtx = null;
        }

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new DispatcherServletServerInitializer(sslCtx, dispatcherServlet, servletContext));
                    //.childHandler(new DispatcherServletChannelInitializer(dispatcherServlet, servletContext));

            Channel ch = b.bind(PORT).sync().channel();
            System.out.println("Netty OCC Server started at port: " + PORT + '.');

            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @Configuration
    @EnableWebMvc
    @ComponentScan(basePackages="de.hybris.platform.ycommercewebservices.web")
    @ImportResource({
            "WEB-INF/config/v2/springmvc-v2-servlet.xml",
            "WEB-INF/config/v2-web-spring.xml",
            "WEB-INF/config/common-web-spring.xml"
    })
    static class WebConfig extends WebMvcConfigurerAdapter {
    }


    static final boolean SSL = System.getProperty("ssl") != null;
    //static final int PORT = Integer.parseInt(System.getProperty("port", SSL? "8443" : "8080"));

}