package com.drivereval.controller;

import javax.servlet.http.HttpServletRequest;

public abstract class BaseController {
    protected Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }
    protected Integer getRole(HttpServletRequest request) {
        return (Integer) request.getAttribute("role");
    }
}
