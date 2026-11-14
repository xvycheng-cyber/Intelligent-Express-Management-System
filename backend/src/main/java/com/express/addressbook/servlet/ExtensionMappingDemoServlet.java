package com.express.addressbook.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Extension mapping demonstration: any request ending in .smap is selected,
 * regardless of its directory.
 */
@WebServlet(name = "extensionMappingDemoServlet", urlPatterns = "*.smap")
public class ExtensionMappingDemoServlet extends MappingDemoServletSupport {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        writeMapping(request, response, "EXTENSION", "*.smap");
    }
}
