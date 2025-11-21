package com.qzh.backend.config;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 配置：注册拦截器（注入方式）
 */
@Configuration
@Slf4j
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("注册登录拦截器：{}", loginInterceptor != null ? "成功" : "失败（拦截器为null）");
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/api/system/**")
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/doc.html/**"
                );
    }
}