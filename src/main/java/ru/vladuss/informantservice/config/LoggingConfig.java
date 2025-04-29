package ru.vladuss.informantservice.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.UUID;

@Configuration
public class LoggingConfig implements Filter {

    private static final String CORR = "corrId";
    private static final String HDR  = "X-Correlation-Id";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        try {
            var http = (HttpServletRequest) req;
            String id = http.getHeader(HDR);
            if (id == null || id.isBlank()) id = UUID.randomUUID().toString();

            MDC.put(CORR, id);
            chain.doFilter(req, res);
        } finally {
            MDC.remove(CORR);
        }
    }
}
