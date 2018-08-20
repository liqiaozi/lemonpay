package com.lemon.config;/**
 * Created by xflig on 2018/8/16.
 */

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * @ClassName: WebMvcConfiguration
 * @Description: TODO
 * @Author: 李雪飞
 * @Date: 2018/8/16 13:19
 * @Vserion 1.0
 **/
@Configuration
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Override
    protected void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/gotoWapPage").setViewName("gotoWapPay");
        registry.addViewController("/gotoPagePage").setViewName("gotoPagePay");
        registry.addViewController("/gotoH5Page").setViewName("gotoH5Page");
        registry.addViewController("/h5PaySuccess").setViewName("h5PaySuccess");

        super.addViewControllers(registry);
    }

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        super.addResourceHandlers(registry);
    }
}
