package de.hybris.platform.ycommercewebservices.web;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;

/**
 * Created by i306724 on 10/2/2016.
 */

public class DispatcherServletServerInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext sslCtx;

    private DispatcherServlet dispatcherServlet;
    private ServletContext servletContext;

    public DispatcherServletServerInitializer(SslContext sslCtx, DispatcherServlet dispatcherServlet, ServletContext servletContext) {
        this.sslCtx = sslCtx;

        this.dispatcherServlet = dispatcherServlet;
        this.servletContext = servletContext;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        if (sslCtx != null) {
            p.addLast(sslCtx.newHandler(ch.alloc()));
        }
        p.addLast(new HttpRequestDecoder());
        // Uncomment the following line if you don't want to handle HttpChunks.
        p.addLast(new HttpObjectAggregator(1048576));
        p.addLast(new HttpResponseEncoder());
        // Remove the following line if you don't want automatic content compression.
        //p.addLast(new HttpContentCompressor());
        p.addLast(new DispatcherServletServerHandler(dispatcherServlet, servletContext));
    }
}