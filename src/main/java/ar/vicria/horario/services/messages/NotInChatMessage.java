package ar.vicria.horario.services.messages;

import ar.vicria.horario.dto.AnswerData;
import ar.vicria.horario.dto.AnswerDto;
import ar.vicria.horario.properties.TelegramProperties;
import ar.vicria.horario.services.TelegramConnector;
import ar.vicria.horario.services.util.RowUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

/**
 * Text msg after /free_time.
 */
@Slf4j
@Order(HIGHEST_PRECEDENCE)
@Component
public class NotInChatMessage extends TextMessage {

    private final TelegramProperties properties;

    private final TelegramConnector connector;
    private String username;

    /**
     * Constrictor.
     *
     * @param rowUtil    util for telegram menu
     * @param properties
     * @param connector
     */
    protected NotInChatMessage(RowUtil rowUtil,
                               TelegramProperties properties, @Lazy TelegramConnector connector) {
        super(rowUtil);
        this.properties = properties;
        this.connector = connector;
    }

    @Override
    public boolean supports(AnswerData answerData, String msg) {
        username = answerData.getQuestionId();
        return !properties.getAdmins().contains(answerData.getQuestionId())
                && (msg.equals("/free_time") || msg.equals("/start"));
    }

    @Override
    public BotApiMethod process(String chatId, Integer msgId) throws Exception {
        SendMessage sendMessage = postQuestionFirst("@" + username + " пытается узнать расписание",
                getClass().getSimpleName(),
                answer(),
                properties.getChatIdAdmin());
        try {
            connector.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Unable to send message", e);
        }
        return postQuestionFirst(question(), getClass().getSimpleName(), answer(), chatId);
    }

    @Override
    public String question() throws Exception {
        return "Используй бот только в общем чате, пожалуйста. Или напиши Вике";

    }

    /**
     * buttons.
     *
     * @return buttons
     */
    @Override
    public List<AnswerDto> answer() {
        return new ArrayList<>();
    }
}
