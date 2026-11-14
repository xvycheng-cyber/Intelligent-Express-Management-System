package com.express.addressbook.servlet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Real embedded-Tomcat test for the Servlet container mapping demo.
 *
 * <p>MockMvc would only exercise DispatcherServlet and could not prove that
 * @WebServlet registrations are active. This test uses RANDOM_PORT and real
 * HTTP requests; the demo endpoints do not read or write business tables.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ServletMappingIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    private String url(String path) {
        return "http://127.0.0.1:" + port + path;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getJson(String path) {
        ResponseEntity<Map> response = rest.getForEntity(url(path), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isNotNull();
        assertThat(response.getHeaders().getContentType().toString()).contains("application/json");
        return (Map<String, Object>) response.getBody();
    }

    @Test
    void exactMapping() {
        Map<String, Object> body = getJson("/demo/servlet/exact");
        assertThat(body.get("handler")).isEqualTo("ExactMappingDemoServlet");
        assertThat(body.get("mappingType")).isEqualTo("EXACT");
        assertThat(body.get("mapping")).isEqualTo("/demo/servlet/exact");
        assertThat(body.get("servletPath")).isEqualTo("/demo/servlet/exact");
        assertThat(body.get("pathInfo")).isNull();
        assertThat(body.get("method")).isEqualTo("GET");
    }

    @Test
    void exactExtraFallsToPath() {
        Map<String, Object> body = getJson("/demo/servlet/exact/extra");
        assertThat(body.get("handler")).isEqualTo("PathMappingDemoServlet");
        assertThat(body.get("mappingType")).isEqualTo("PATH");
        assertThat(body.get("servletPath")).isEqualTo("/demo/servlet");
        assertThat(body.get("pathInfo")).isEqualTo("/exact/extra");
    }

    @Test
    void pathMapping() {
        Map<String, Object> body = getJson("/demo/servlet/prefix/a");
        assertThat(body.get("handler")).isEqualTo("PathMappingDemoServlet");
        assertThat(body.get("pathInfo")).isEqualTo("/prefix/a");
        assertThat(body.get("requestUri")).isEqualTo("/demo/servlet/prefix/a");
    }

    @Test
    void pathMappingMultiLevel() {
        Map<String, Object> body = getJson("/demo/servlet/prefix/a/b");
        assertThat(body.get("handler")).isEqualTo("PathMappingDemoServlet");
        assertThat(body.get("pathInfo")).isEqualTo("/prefix/a/b");
    }

    @Test
    void extensionMapping() {
        Map<String, Object> body = getJson("/demo/legacy/report.smap");
        assertThat(body.get("handler")).isEqualTo("ExtensionMappingDemoServlet");
        assertThat(body.get("mappingType")).isEqualTo("EXTENSION");
        assertThat(body.get("mapping")).isEqualTo("*.smap");
        assertThat(body.get("servletPath")).isEqualTo("/demo/legacy/report.smap");
        assertThat(body.get("pathInfo")).isNull();
    }

    @Test
    void pathBeatsExtension() {
        Map<String, Object> body = getJson("/demo/servlet/legacy/report.smap");
        assertThat(body.get("handler")).isEqualTo("PathMappingDemoServlet");
        assertThat(body.get("mappingType")).isEqualTo("PATH");
        assertThat(body.get("servletPath")).isEqualTo("/demo/servlet");
        assertThat(body.get("pathInfo")).isEqualTo("/legacy/report.smap");
    }

    @Test
    void springInfoIsSpringMvc() {
        Map<String, Object> body = getJson("/demo/spring/info");
        assertThat(body.get("handler")).isEqualTo("ServletMappingDemoController");
        assertThat(body.get("mappingType")).isEqualTo("SPRING_MVC");
        assertThat(body.get("controllerMethod")).isEqualTo("info");
        assertThat(body.get("requestUri")).isEqualTo("/demo/spring/info");
    }

    @Test
    void exactPostIsMethodNotAllowed() {
        ResponseEntity<String> response = rest.exchange(
                url("/demo/servlet/exact"), HttpMethod.POST, HttpEntity.EMPTY, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Test
    void pathPostIsMethodNotAllowed() {
        ResponseEntity<String> response = rest.exchange(
                url("/demo/servlet/prefix/a"), HttpMethod.POST, HttpEntity.EMPTY, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Test
    @SuppressWarnings("unchecked")
    void springPostIsMethodNotAllowedWithUnifiedBody() {
        ResponseEntity<Map> response = rest.exchange(
                url("/demo/spring/method-only"), HttpMethod.POST, HttpEntity.EMPTY, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(response.getBody()).isNotNull();
        assertThat(((Map<String, Object>) response.getBody()).get("code")).isEqualTo(40501);
    }

    @Test
    @SuppressWarnings("unchecked")
    void unmatchedPathUsesDefaultDispatcherMappingAndReturnsSpring404() {
        ResponseEntity<Map> response = rest.getForEntity(url("/demo/legacy/report.json"), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(((Map<String, Object>) response.getBody()).get("code")).isEqualTo(40402);
    }

    @Test
    void queryDoesNotChangeExactMapping() {
        Map<String, Object> body = getJson("/demo/servlet/exact?trace=query-42");
        assertThat(body.get("handler")).isEqualTo("ExactMappingDemoServlet");
        assertThat(body.get("mappingType")).isEqualTo("EXACT");
        assertThat(body.get("query")).isEqualTo("trace=query-42");
    }
}
