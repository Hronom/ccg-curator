package com.github.hronom.ccg.curator.server;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class CcgCuratorServer extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(CcgCuratorServer.class);
    }

    public static void main(String[] args) throws Exception {
        new SpringApplicationBuilder(CcgCuratorServer.class).run(args);
    }
}
