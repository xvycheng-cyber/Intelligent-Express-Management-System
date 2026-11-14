package com.express.addressbook.demo;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Spring MVC comparison for the Servlet container mapping demo.
 *
 * <p>These handlers are reached only after the request enters the
 * DispatcherServlet. They are not Servlet container mappings.</p>
 */
@RestController
@RequestMapping("/demo/spring")
public class ServletMappingDemoController {

    @GetMapping("/info")
    public Map<String, Object> info(HttpServletRequest request) {
        return response(request, "info", "/demo/spring/info");
    }

    @GetMapping("/echo/{name}")
    public Map<String, Object> echo(@PathVariable String name, HttpServletRequest request) {
        Map<String, Object> body = response(request, "echo", "/demo/spring/echo/{name}");
        body.put("pathVariable", name);
        return body;
    }

    @GetMapping("/method-only")
    public Map<String, Object> methodOnly(HttpServletRequest request) {
        return response(request, "methodOnly", "/demo/spring/method-only");
    }

    private Map<String, Object> response(HttpServletRequest request, String controllerMethod, String mapping) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("handler", "ServletMappingDemoController");
        body.put("mappingType", "SPRING_MVC");
        body.put("mapping", mapping);
        body.put("controllerMethod", controllerMethod);
        body.put("requestUri", request.getRequestURI());
        body.put("contextPath", request.getContextPath());
        body.put("method", request.getMethod());
        body.put("query", request.getQueryString());
        return body;
    }
}
