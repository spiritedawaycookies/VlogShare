package com.lcy.vlog.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // 实现静态资源的映射
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/META-INF/resources/")  // 映射swagger2
                .addResourceLocations("file:///home/lcy/IdeaProjects/lcy-vlog-share/static/");  // 映射本地静态资源
        registry.addResourceHandler("/static/images/**")
                .addResourceLocations("file:///home/lcy/IdeaProjects/lcy-vlog-share/static/images/");
        registry.addResourceHandler("/static/avatar/**")
                .addResourceLocations("file:///home/lcy/IdeaProjects/lcy-vlog-share/static/avatar/");
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

}
