package com.express.addressbook.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Path mapping demonstration: /demo/servlet/* keeps the remaining path in
 * servletPath/pathInfo.
 */
@WebServlet(name = "pathMappingDemoServlet", urlPatterns = "/demo/servlet/*")
public class PathMappingDemoServlet extends MappingDemoServletSupport {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        writeMapping(request, response, "PATH", "/demo/servlet/*");
    }
}
