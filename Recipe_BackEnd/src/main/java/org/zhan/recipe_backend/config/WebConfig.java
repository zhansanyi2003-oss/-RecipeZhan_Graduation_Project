package org.zhan.recipe_backend.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 🏆 动态构建路径
        // 注意：Windows 和 Linux 的路径分隔符可能不同，但 Java 会自动处理
        // 关键是前面要加 "file:"
        String path = "file:" + System.getProperty("user.dir") + "/uploads/";

        // 映射：浏览器访问 /images/** -> 去项目根目录下的 /uploads/ 找
        registry.addResourceHandler("/images/**")
                .addResourceLocations(path);


    }
}
