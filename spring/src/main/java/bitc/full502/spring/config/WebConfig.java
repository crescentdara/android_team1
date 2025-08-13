package bitc.full502.spring.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 안드로이드 앱에서 호출할 수 있도록 모든 오리진 임시 허용 (배포 시 도메인 제한 권장)
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET","POST","PUT","DELETE","PATCH","OPTIONS")
                .allowedHeaders("*");
    }
}