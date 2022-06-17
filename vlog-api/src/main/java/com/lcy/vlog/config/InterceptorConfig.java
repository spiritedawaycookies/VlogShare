package com.lcy.vlog.config;

import com.lcy.vlog.interceptor.PassportInterceptor;
//import com.lcy.vlog.interceptor.UserTokenInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册拦截器的配置
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Bean
    public PassportInterceptor passportInterceptor() {
        return new PassportInterceptor();
    }

//    @Bean
//    public UserTokenInterceptor userTokenInterceptor() {
//        return new UserTokenInterceptor();
//    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(passportInterceptor())
                .addPathPatterns("/passport/getSMSCode");

//        registry.addInterceptor(userTokenInterceptor())
//                .addPathPatterns("/userInfo/modifyUserInfo")
//                .addPathPatterns("/userInfo/modifyImage");
    }
}
