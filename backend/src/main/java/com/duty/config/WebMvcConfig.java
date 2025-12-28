package com.duty.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * Web MVC 配置
 * 处理前后端合并部署后的路由
 *
 * @author duty-system
 * @since 2025-12-26
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将静态资源映射到 classpath:/static/
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600)
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        // 规范化路径：移除开头的 /
                        String normalizedPath = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;

                        // 如果请求的是根路径，返回 index.html
                        if ("".equals(normalizedPath)) {
                            return new ClassPathResource("/static/index.html");
                        }

                        // 如果请求的是 index.html，直接返回
                        if ("index.html".equals(normalizedPath)) {
                            Resource requestedResource = location.createRelative(resourcePath);
                            if (requestedResource.exists() && requestedResource.isReadable()) {
                                return requestedResource;
                            }
                            return new ClassPathResource("/static/index.html");
                        }

                        // 如果请求的是 assets 目录下的资源（静态JS/CSS文件等）
                        if (normalizedPath.startsWith("assets/")) {
                            Resource requestedResource = location.createRelative(resourcePath);
                            if (requestedResource.exists() && requestedResource.isReadable()) {
                                return requestedResource;
                            }
                            // 静态资源不存在，返回 404
                            return null;
                        }

                        // 检查是否是 API 请求（包含已知的 API 路径模式）
                        // API 路径格式：/xxx/yyy (至少两级，且匹配已知的API路径)
                        if (isApiRequest(normalizedPath)) {
                            // 让控制器处理，返回 null
                            return null;
                        }

                        // 对于 favicon.ico 等常见静态资源
                        if (normalizedPath.contains(".")) {
                            Resource requestedResource = location.createRelative(resourcePath);
                            if (requestedResource.exists() && requestedResource.isReadable()) {
                                return requestedResource;
                            }
                            return null;
                        }

                        // 对于所有其他请求（前端路由），返回 index.html
                        // 这样 Vue Router 才能处理前端路由
                        return new ClassPathResource("/static/index.html");
                    }

                    /**
                     * 判断是否是 API 请求
                     */
                    private boolean isApiRequest(String path) {
                        if (path == null || path.isEmpty()) {
                            return false;
                        }

                        // 已知的 API 路径前缀
                        String[] apiPrefixes = {
                                "schedule/",      // /schedule/page, /schedule/batch, /schedule/123
                                "shift/",         // /shift/list, /shift/123
                                "record/",        // /record/list, /record/123
                                "template/",      // /template/list, /template/123
                                "swagger",        // /swagger-ui.html, /swagger-resources
                                "v3/api-docs",    // /v3/api-docs
                                "csrf"            // /csrf
                        };

                        for (String prefix : apiPrefixes) {
                            if (path.startsWith(prefix)) {
                                return true;
                            }
                        }

                        return false;
                    }
                });
    }
}
