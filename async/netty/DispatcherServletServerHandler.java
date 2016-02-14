package de.hybris.platform.ycommercewebservices.web;

import de.hybris.platform.catalog.CatalogService;
import de.hybris.platform.catalog.constants.CatalogConstants;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.Registry;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.servicelayer.model.ModelService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by i306724 on 10/2/2016.
 */

public class DispatcherServletServerHandler extends SimpleChannelInboundHandler<Object> {

    private HttpRequest request;

    private final Servlet servlet;

    private final ServletContext servletContext;

    public DispatcherServletServerHandler(Servlet servlet, ServletContext servletContext) {
        this.servlet = servlet;
        this.servletContext = servletContext;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

        this.request = (HttpRequest) msg;

        MockHttpServletRequest servletRequest = createServletRequest((FullHttpRequest) msg);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();


        try {
            Registry.activateMasterTenant();
            JaloSession session = JaloSession.getCurrentSession();

            CatalogService catalogService = (CatalogService) Registry.getApplicationContext().getBean("catalogService");
            ModelService modelService = (ModelService) Registry.getApplicationContext().getBean("modelService");
            CatalogVersionModel cv = catalogService.getCatalogVersion("electronicsProductCatalog", "Online");
            Collection catalogversions = new ArrayList();
            catalogversions.add(modelService.getSource(cv));
            session.setAttribute(CatalogConstants.SESSION_CATALOG_VERSIONS, catalogversions);

            catalogService.setSessionCatalogVersions(new HashSet<>(catalogversions));

            // call spring dispatcherservlet
            this.servlet.service(servletRequest, servletResponse);
        } catch (Exception e) {
            //TODO
            e.printStackTrace();
        } finally {
            JaloSession.deactivate();
            Registry.unsetCurrentTenant();
        }


        if (!writeResponse(servletResponse, ctx)) {
            // If keep-alive is off, close the connection once the content is fully written.
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }

    }


    private boolean writeResponse(MockHttpServletResponse servletResponse, ChannelHandlerContext ctx) throws Exception {
        // Decide whether to close the connection or not.
        boolean keepAlive = HttpUtil.isKeepAlive(request);
        // Build the response object.
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1,
                HttpResponseStatus.valueOf(servletResponse.getStatus()),
                Unpooled.copiedBuffer(servletResponse.getContentAsString(), CharsetUtil.UTF_8));

        //response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");

        for (String name : servletResponse.getHeaderNames()) {
            for (Object value : servletResponse.getHeaderValues(name)) {
                response.headers().add(name, value);
            }
        }

        if (keepAlive) {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            // Add keep alive header as per:
            // - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        ctx.write(response);

        return keepAlive;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


    private MockHttpServletRequest createServletRequest(FullHttpRequest fullHttpRequest) {
        UriComponents uriComponents = UriComponentsBuilder.fromUriString(fullHttpRequest.getUri()).build();

        MockHttpServletRequest servletRequest = new MockHttpServletRequest(this.servletContext);
        servletRequest.setRequestURI(uriComponents.getPath());
        servletRequest.setPathInfo(uriComponents.getPath());
        servletRequest.setMethod(fullHttpRequest.getMethod().name());

        if (uriComponents.getScheme() != null) {
            servletRequest.setScheme(uriComponents.getScheme());
        }
        if (uriComponents.getHost() != null) {
            servletRequest.setServerName(uriComponents.getHost());
        }
        if (uriComponents.getPort() != -1) {
            servletRequest.setServerPort(uriComponents.getPort());
        }

        for (String name : fullHttpRequest.headers().names()) {
            servletRequest.addHeader(name, fullHttpRequest.headers().get(name));
        }

        ByteBuf bbContent = fullHttpRequest.content();
        if (bbContent.hasArray()) {
            byte[] baContent = bbContent.array();
            servletRequest.setContent(baContent);
        }

        try {
            ByteBuf buf = fullHttpRequest.content();
            int readable = buf.readableBytes();
            byte[] bytes = new byte[readable];
            buf.readBytes(bytes);
            String contentStr = UriUtils.decode(new String(bytes, "UTF-8"), "UTF-8");
            for (String params : contentStr.split("&")) {
                String[] para = params.split("=");
                if (para.length > 1) {
                    servletRequest.addParameter(para[0], para[1]);
                } else {
                    servletRequest.addParameter(para[0], "");
                }
            }
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            if (uriComponents.getQuery() != null) {
                String query = UriUtils.decode(uriComponents.getQuery(), "UTF-8");
                servletRequest.setQueryString(query);
            }

            for (Entry<String, List<String>> entry : uriComponents.getQueryParams().entrySet()) {
                for (String value : entry.getValue()) {
                    servletRequest.addParameter(
                            UriUtils.decode(entry.getKey(), "UTF-8"),
                            UriUtils.decode(value, "UTF-8"));
                }
            }
        } catch (UnsupportedEncodingException ex) {
            // shouldn't happen
        }

        return servletRequest;
    }
}