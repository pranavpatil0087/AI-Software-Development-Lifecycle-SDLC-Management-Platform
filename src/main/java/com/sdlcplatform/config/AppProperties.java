package com.sdlcplatform.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Strongly-typed binding for the "app.*" configuration namespace,
 * used instead of scattering @Value across the codebase.
 */
@Configuration
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {

    private final Jwt jwt = new Jwt();
    private final Frontend frontend = new Frontend();
    private final Cors cors = new Cors();
    private final Mail mail = new Mail();
    private final Ai ai = new Ai();

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private long accessTokenExpirationMs;
        private long refreshTokenExpirationMs;
    }

    @Getter
    @Setter
    public static class Frontend {
        private String baseUrl;
    }

    @Getter
    @Setter
    public static class Cors {
        private String allowedOrigins;
    }

    @Getter
    @Setter
    public static class Mail {
        private String from;
    }

    @Getter
    @Setter
    public static class Ai {
        private String provider;
        private final Openai openai = new Openai();

        @Getter
        @Setter
        public static class Openai {
            private String apiKey;
            private String model;
        }
    }
}
