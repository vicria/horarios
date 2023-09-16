package ar.vicria.horario.services.messages;

import ar.vicria.horario.dto.AnswerData;
import ar.vicria.horario.dto.AnswerDto;
import ar.vicria.horario.properties.TelegramProperties;
import ar.vicria.horario.services.util.RowUtil;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Collections;
import java.util.List;

/**
 * Text msg after /start.
 */
@Order(0)
@Component
public class AdminMessage extends TextMessage {

    private final TelegramProperties properties;

    /**
     * Constrictor.
     *
     * @param rowUtil    util for telegram menu
     * @param properties
     */
    protected AdminMessage(RowUtil rowUtil, TelegramProperties properties) {
        super(rowUtil);
        this.properties = properties;
    }

    @Override
    public boolean supports(AnswerData answerData, String msg) {
        return msg.equals("/admin") && properties.getAdmins().contains(answerData.getQuestionId());
    }

    @Override
    public SendMessage process(String chatId, Integer msgId) {
        properties.setChatIdAdmin(chatId);
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(question())
                .build();
        return sendMessage(message, Collections.EMPTY_LIST);
    }

    @Override
    public String question() {
        return "админ назначен " + properties.getChatIdAdmin();
    }

    /**
     * buttons.
     *
     * @return buttons
     */
    @Override
    public List<AnswerDto> answer() {
        return Collections.emptyList();
    }
}
