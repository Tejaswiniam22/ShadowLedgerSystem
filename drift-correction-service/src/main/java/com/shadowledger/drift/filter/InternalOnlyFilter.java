package com.shadowledger.drift.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class InternalOnlyFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        String internal = req.getHeader("X-Internal-Call");

        if (!"true".equals(internal)) {
            ((HttpServletResponse) response)
                    .sendError(HttpServletResponse.SC_FORBIDDEN,
                            "Direct access forbidden");
            return;
        }

        chain.doFilter(request, response);
    }
}

