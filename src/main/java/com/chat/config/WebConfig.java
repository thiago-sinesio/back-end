package com.chat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.chat.config.interceptor.LoggingInterceptor;

import jakarta.servlet.MultipartConfigElement;
import java.util.Arrays;
import java.util.List;

/**
 * WebConfig — Configuração centralizada de infraestrutura HTTP
 * 
 * Responsabilidades:
 * 1. CORS (Cross-Origin Resource Sharing) para frontend React
 * 2. Multipart upload configuration (máximo 10MB)
 * 3. Interceptadores para logging e segurança
 * 4. RestTemplate bean para chamadas síncronas a APIs externas
 * 
 * Princípios SOLID aplicados:
 * - Single Responsibility: Apenas configurações HTTP
 * - Open/Closed: Fácil adicionar novos interceptadores/filters
 * - Dependency Inversion: Injeta propriedades via @Value
 * 
 * O que NÃO faz:
 * ✗ Lógica de negócio
 * ✗ Validações complexas
 * ✗ Acesso a banco de dados
 * ✗ Geração de respostas
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;

    @Value("${app.cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${app.cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${app.cors.max-age:3600}")
    private long maxAge;

    @Value("${spring.servlet.multipart.max-file-size:10MB}")
    private String maxFileSize;

    @Value("${spring.servlet.multipart.max-request-size:10MB}")
    private String maxRequestSize;

    // ============================================================================
    // CORS Configuration
    // ============================================================================

    /**
     * Configura CORS (Cross-Origin Resource Sharing) nativo do Spring MVC.
     * 
     * @param registry Registro de configurações de CORS
     */
    @Override
    public void addCorsMappings(org.springframework.web.servlet.config.annotation.CorsRegistry registry) {
        String[] origins = Arrays.stream(allowedOrigins.split(","))
                                 .map(String::trim)
                                 .toArray(String[]::new);
                                 
        String[] methods = Arrays.stream(allowedMethods.split(","))
                                 .map(String::trim)
                                 .toArray(String[]::new);
                                 
        String[] headers = Arrays.stream(allowedHeaders.split(","))
                                 .map(String::trim)
                                 .toArray(String[]::new);

        registry.addMapping("/**")
                .allowedOrigins(origins)
                .allowedMethods(methods)
                .allowedHeaders(headers)
                .allowCredentials(allowCredentials)
                .maxAge(maxAge);
    }

    // ============================================================================
    // Multipart Configuration
    // ============================================================================

    /**
     * Configura o limite máximo de tamanho para upload de arquivos.
     * 
     * Parâmetros:
     * - max-file-size: Tamanho máximo de um arquivo individual (10MB)
     * - max-request-size: Tamanho máximo de toda a requisição multipart (10MB)
     * 
     * Notas:
     * - Também configurado em application.yml para compatibilidade com Spring
     * - Aceita valores como "10MB", "100KB", etc.
     * 
     * @return MultipartConfigElement configurado
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();

        factory.setMaxFileSize(DataSize.parse(maxFileSize));
        factory.setMaxRequestSize(DataSize.parse(maxRequestSize));

        // Diretório temporário para upload (usa temp padrão do sistema)
        // factory.setLocation("/path/to/temp");

        return factory.createMultipartConfig();
    }

    // ============================================================================
    // Interceptadores
    // ============================================================================

    /**
     * Registra interceptadores de requisição/resposta HTTP.
     * 
     * Interceptadores disponíveis:
     * 1. LoggingInterceptor: Log de todas as requisições e respostas
     * 
     * Futuro:
     * - RateLimitingInterceptor: Controle de taxa de requisições
     * - SecurityHeadersInterceptor: Headers de segurança (HSTS, X-Frame-Options, etc.)
     * 
     * @param registry Registro de interceptadores do Spring MVC
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoggingInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/health", "/actuator/**", "/swagger-ui/**", "/v3/api-docs/**");
    }

    // ============================================================================
    // RestTemplate Bean (para chamadas síncronas a APIs externas)
    // ============================================================================

    /**
     * Bean RestTemplate para comunicação síncrona com APIs externas.
     * 
     * Pode ser injetado em serviços que precisem chamar APIs:
     * - Modelos de IA (OpenAI, Hugging Face, etc.)
     * - Serviços terceirizados
     * - Microserviços internos
     * 
     * Futuro:
     * - Adicionar timeout
     * - Adicionar retry logic
     * - Adicionar circuit breaker
     * 
     * @return RestTemplate configurado
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
