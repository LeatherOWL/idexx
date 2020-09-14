package com.idexx.test.config;

import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;

@Configuration
public class NettyConfig {

    @Bean
    public WebServerFactoryCustomizer serverFactoryCustomizer() {
        return new NettyTimeoutCustomizer();
    }

    static class NettyTimeoutCustomizer implements WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {

        @Override
        public void customize(NettyReactiveWebServerFactory factory) {
            int connectionTimeout = 60000;
            factory.addServerCustomizers(server -> server.tcpConfiguration(tcp ->
                    tcp.option(CONNECT_TIMEOUT_MILLIS, connectionTimeout).doOnConnection(connection ->
                            connection.addHandlerLast(new WriteTimeoutHandler(connectionTimeout)))));
        }
    }

}
