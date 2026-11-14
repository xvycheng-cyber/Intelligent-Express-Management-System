package com.express.addressbook.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shared JSON response for the three demonstration Servlets.
 *
 * <p>This class deliberately does not touch business services or the database.
 * The subclasses carry the actual {@code @WebServlet} mappings.</p>
 */
abstract class MappingDemoServletSupport extends HttpServlet {

    private static final ObjectMapper JSON = new ObjectMapper();

    protected void writeMapping(
            HttpServletRequest request,
            HttpServletResponse response,
            String mappingType,
            String mapping
    ) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("handler", getClass().getSimpleName());
        body.put("mappingType", mappingType);
        body.put("mapping", mapping);
        body.put("requestUri", request.getRequestURI());
        body.put("contextPath", request.getContextPath());
        body.put("servletPath", request.getServletPath());
        body.put("pathInfo", request.getPathInfo());
        body.put("method", request.getMethod());
        body.put("query", request.getQueryString());

        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("X-Demo-Handler", getClass().getSimpleName());
        response.setHeader("X-Demo-Mapping-Type", mappingType);
        JSON.writeValue(response.getOutputStream(), body);
    }
}
