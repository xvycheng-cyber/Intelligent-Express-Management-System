package com.express.addressbook.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Exact mapping demonstration: only /demo/servlet/exact is selected.
 */
@WebServlet(name = "exactMappingDemoServlet", urlPatterns = "/demo/servlet/exact")
public class ExactMappingDemoServlet extends MappingDemoServletSupport {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        writeMapping(request, response, "EXACT", "/demo/servlet/exact");
    }
}
