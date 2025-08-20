package com.example.interview_agent.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpServletRequest;


@Controller
public class SpaController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        String path = request.getRequestURI();

        if (!path.startsWith("/api/") && !path.contains(".")) {
            return "forward:/index.html";
        }

        return "error";
    }
}