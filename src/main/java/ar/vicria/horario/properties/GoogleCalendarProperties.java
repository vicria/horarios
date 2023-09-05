package ar.vicria.horario.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Telegram properties for connection.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "com.google.calendar")
public class GoogleCalendarProperties {

    @NotBlank
    private String clientId;

    @NotBlank
    private String projectId;

    private String authUri = "https://accounts.google.com/o/oauth2/auth";

    private String tokenUri = "https://oauth2.googleapis.com/token";

    private String authProviderX509CertUrl = "https://www.googleapis.com/oauth2/v1/certs";

    @NotBlank
    private String clientSecret;

    @NotNull
    private Boolean localhost;

    @NotBlank
    private Integer redirectPort;

    private String refreshToken;

    @Override
    public String toString() {
        return "{\n" +
                "  \"web\": {\n" +
                "    \"client_id\": \"" + clientId + "\",\n" +
                "    \"project_id\": \"" + projectId +"\",\n" +
                "    \"auth_uri\": \"" + authUri +"\",\n" +
                "    \"token_uri\": \"" + tokenUri +"\",\n" +
                "    \"auth_provider_x509_cert_url\": \"" + authProviderX509CertUrl +"\",\n" +
                "    \"client_secret\": \"" + clientSecret +"\"\n" +
                "  }\n" +
                "}";
    }
}
