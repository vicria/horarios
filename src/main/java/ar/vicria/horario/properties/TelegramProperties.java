package ar.vicria.horario.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;

/**
 * Telegram properties for connection.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ar.vicria.adapter.telegram")
public class TelegramProperties {
    /**
     * Имя бота.
     */
    @NotBlank
    private String botUserName;
    /**
     * Токен.
     */
    @NotBlank
    private String botToken;

}
