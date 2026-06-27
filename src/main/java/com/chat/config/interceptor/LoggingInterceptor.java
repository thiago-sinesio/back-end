package com.chat.config.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * LoggingInterceptor — Interceptador de logging de requisições HTTP
 * 
 * Responsabilidade:
 * - Log de todas as requisições HTTP (método, rota, cliente IP)
 * - Log de tempo de execução e status da resposta
 * - Rastreamento distribuído (future: correlation ID)
 * 
 * O que NÃO faz:
 * ✗ Validação de segurança (usar SecurityFilter)
 * ✗ Modificação de headers de resposta (usar Filter)
 * ✗ Manipulação de body (usar RequestWrapper)
 */
public class LoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);
    private static final String START_TIME_ATTR = "start_time";

    /**
     * Executado ANTES do handler (controller)
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) 
            throws Exception {
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME_ATTR, startTime);

        String clientIP = getClientIP(request);
        logger.debug("→ [REQUEST] {} {} | Client: {} | User-Agent: {}",
                request.getMethod(),
                request.getRequestURI(),
                clientIP,
                request.getHeader("User-Agent"));

        return true; // Continuar o processamento
    }

    /**
     * Executado DEPOIS do handler (controller), mas ANTES de renderizar a view
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, 
            Object handler, ModelAndView modelAndView) throws Exception {
        // Pode ser usado para adicionar atributos ao modelo
    }

    /**
     * Executado DEPOIS de tudo (até após a renderização da view)
     * Ideal para cleanup e logging final
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
            Object handler, Exception ex) throws Exception {
        
        long startTime = (Long) request.getAttribute(START_TIME_ATTR);
        long duration = System.currentTimeMillis() - startTime;

        if (ex != null) {
            logger.error("✗ [ERROR] {} {} | Status: {} | Duration: {}ms | Exception: {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    duration,
                    ex.getMessage());
        } else {
            String status = getStatusCategory(response.getStatus());
            logger.debug("← [RESPONSE] {} {} | Status: {} | Duration: {}ms {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    duration,
                    status);
        }
    }

    /**
     * Extrai o endereço IP do cliente considerando proxies
     */
    private String getClientIP(HttpServletRequest request) {
        String clientIP = request.getHeader("X-Forwarded-For");
        if (clientIP == null || clientIP.isEmpty()) {
            clientIP = request.getRemoteAddr();
        }
        return clientIP;
    }

    /**
     * Retorna categoria de status HTTP (sucesso, redirecionamento, erro cliente, erro servidor)
     */
    private String getStatusCategory(int status) {
        if (status >= 200 && status < 300) {
            return "✓ Success";
        } else if (status >= 300 && status < 400) {
            return "↻ Redirect";
        } else if (status >= 400 && status < 500) {
            return "⚠ Client Error";
        } else {
            return "✗ Server Error";
        }
    }
}
