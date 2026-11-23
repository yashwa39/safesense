package com.safesense.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns(
                        "/dashboard",
                        "/dashboard.html",
                        "/tests",
                        "/tests.html",
                        "/live",
                        "/alerts",
                        "/history",
                        "/limits",
                        "/settings",
                        "/coming-soon.html",
                        "/api/**"
                )
                .excludePathPatterns("/api/login", "/api/logout");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("forward:/login.html");
        registry.addViewController("/dashboard").setViewName("forward:/dashboard.html");
        registry.addViewController("/tests").setViewName("forward:/tests.html");
        registry.addViewController("/live").setViewName("forward:/coming-soon.html");
        registry.addViewController("/alerts").setViewName("forward:/coming-soon.html");
        registry.addViewController("/history").setViewName("forward:/coming-soon.html");
        registry.addViewController("/limits").setViewName("forward:/coming-soon.html");
        registry.addViewController("/settings").setViewName("forward:/coming-soon.html");
        registry.addViewController("/").setViewName("redirect:/dashboard");
    }
}
