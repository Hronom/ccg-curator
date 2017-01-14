package com.github.hronom.ccg.curator.server.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
            .pathMapping("/")
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.github.hronom.ccg.curator.server"))
            .paths(PathSelectors.any())
            .build()
            .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
            "CCG Curator server",
            "CCG Curator server",
            "API 1.0.0",
            "",
            new Contact("Hronom", "", "hronom@gmail.com"),
            "License",
            ""
        );
    }
}
