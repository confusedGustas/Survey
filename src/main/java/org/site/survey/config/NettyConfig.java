package org.site.survey.config;

import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NettyConfig {
    @Bean
    public WebServerFactoryCustomizer<NettyReactiveWebServerFactory> customNettyWebServerFactoryCustomizer() {
        return factory -> factory.addServerCustomizers(httpServer ->
                httpServer.httpRequestDecoder(decoder ->
                        decoder.maxHeaderSize(65536) // 64KB
                                .maxInitialLineLength(65536)
                )
        );
    }
}