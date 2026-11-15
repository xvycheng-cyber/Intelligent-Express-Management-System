package com.express.addressbook.common;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 全局异常处理：所有 /api/** 错误返回统一外层结构。
 *
 * <p>40001 = 字段值不合法；40003 = 集合/文本数量超限。两者都使用 data.violations[]。
 * 批量保存 items 的 @Size 违规必须返回 40003，而不是 40001。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<R<Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        boolean itemsSizeExceeded = fieldErrors.stream()
                .anyMatch(error -> "items".equals(error.getField()) && "Size".equals(error.getCode()));
        boolean idsSizeExceeded = fieldErrors.stream()
                .anyMatch(error -> "ids".equals(error.getField()) && "Size".equals(error.getCode()));

        List<Violation> violations = new ArrayList<>();
        if (itemsSizeExceeded) {
            violations.add(new Violation("items", "单次最多保存 200 条"));
            return build(ResultCode.LIMIT_EXCEEDED.code(), "单次最多保存 200 条", violationsData(violations));
        }
        if (idsSizeExceeded) {
            violations.add(new Violation("ids", "单次最多删除 200 条"));
            return build(ResultCode.LIMIT_EXCEEDED.code(), "单次最多删除 200 条", violationsData(violations));
        }

        for (FieldError error : fieldErrors) {
            violations.add(new Violation(error.getField(), safeMessage(error.getDefaultMessage())));
        }
        ex.getBindingResult().getGlobalErrors()
                .forEach(error -> violations.add(new Violation(error.getObjectName(), safeMessage(error.getDefaultMessage()))));
        return build(ResultCode.VALIDATION_FAILED.code(), "参数校验失败", violationsData(violations));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<R<Object>> handleBind(BindException ex) {
        List<Violation> violations = new ArrayList<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> violations.add(new Violation(error.getField(), safeMessage(error.getDefaultMessage()))));
        return build(ResultCode.VALIDATION_FAILED.code(), "参数校验失败", violationsData(violations));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<R<Object>> handleConstraintViolation(ConstraintViolationException ex) {
        List<Violation> violations = ex.getConstraintViolations().stream()
                .map(v -> new Violation(normalizeConstraintPath(v.getPropertyPath().toString()),
                        safeMessage(v.getMessage())))
                .toList();
        return build(ResultCode.VALIDATION_FAILED.code(), "参数校验失败", violationsData(violations));
    }

    /**
     * 方法参数校验会带方法名前缀（如 list.page）；只映射分页的两个已知路径，
     * 其他 property path 保持原样，避免破坏嵌套 DTO 错误定位。
     */
    private String normalizeConstraintPath(String path) {
        return switch (path) {
            case "list.page" -> "page";
            case "list.size" -> "size";
            default -> path;
        };
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<R<Object>> handleMissingParameter(MissingServletRequestParameterException ex) {
        return build(ResultCode.VALIDATION_FAILED.code(), "参数校验失败",
                violationsData(List.of(new Violation(ex.getParameterName(), "参数不能为空"))));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<R<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return build(ResultCode.VALIDATION_FAILED.code(), "参数类型不正确",
                violationsData(List.of(new Violation(ex.getName(), "参数类型不正确"))));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<R<Object>> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return build(ResultCode.BAD_REQUEST_BODY.code(), "请求数据格式不正确", null);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<R<Object>> handleBusiness(BusinessException ex) {
        return build(ex.getCode(), ex.getMessage(), ex.getData());
    }

    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<R<Object>> handleNotFound(Exception ex) {
        return build(ResultCode.API_NOT_FOUND.code(), "接口不存在", null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<R<Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED);
        if (ex.getSupportedHttpMethods() != null && !ex.getSupportedHttpMethods().isEmpty()) {
            Set<String> allowed = new LinkedHashSet<>();
            ex.getSupportedHttpMethods().forEach(method -> allowed.add(method.name()));
            if (allowed.contains("GET")) {
                allowed.add("HEAD");
            }
            allowed.add("OPTIONS");
            builder.header(HttpHeaders.ALLOW, String.join(", ", allowed));
        }
        return builder.body(R.fail(ResultCode.METHOD_NOT_ALLOWED.code(), "请求方法不支持", null));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<R<Object>> handleDataAccess(DataAccessException ex) {
        log.error("数据库操作失败", ex);
        return build(ResultCode.DATABASE_ERROR.code(), "数据库操作失败，请稍后重试", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<R<Object>> handleUnexpected(Exception ex) {
        log.error("未处理异常", ex);
        return build(ResultCode.INTERNAL_ERROR.code(), "服务器内部错误", null);
    }

    private ResponseEntity<R<Object>> build(int code, String message, Object data) {
        return ResponseEntity.status(statusFor(code)).body(R.fail(code, message, data));
    }

    private Map<String, List<Violation>> violationsData(List<Violation> violations) {
        return Map.of("violations", violations);
    }

    private HttpStatus statusFor(int code) {
        return switch (code) {
            case 40001, 40002, 40003 -> HttpStatus.BAD_REQUEST;
            case 40401, 40402 -> HttpStatus.NOT_FOUND;
            case 40501 -> HttpStatus.METHOD_NOT_ALLOWED;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private String safeMessage(String message) {
        return message == null || message.isBlank() ? "参数不合法" : message;
    }
}
