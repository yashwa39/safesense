package com.safesense.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("redirect:/dashboard.html");
        registry.addViewController("/dashboard").setViewName("forward:/dashboard.html");
        registry.addViewController("/tests").setViewName("forward:/tests.html");
        registry.addViewController("/live").setViewName("forward:/coming-soon.html");
        registry.addViewController("/alerts").setViewName("forward:/coming-soon.html");
        registry.addViewController("/history").setViewName("forward:/coming-soon.html");
        registry.addViewController("/limits").setViewName("forward:/coming-soon.html");
        registry.addViewController("/settings").setViewName("forward:/coming-soon.html");
        registry.addViewController("/").setViewName("forward:/dashboard.html");
    }
}
