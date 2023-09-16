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
public class StartMessage extends TextMessage {

    boolean isChat;

    private final TelegramProperties properties;

    /**
     * Constrictor.
     *
     * @param rowUtil    util for telegram menu
     * @param properties
     */
    protected StartMessage(RowUtil rowUtil, TelegramProperties properties) {
        super(rowUtil);
        this.properties = properties;
    }

    @Override
    public boolean supports(AnswerData answerData, String msg) {
        isChat = msg.contains("@");
        return msg.equals("/start") || msg.equals("/start@" + properties.getBotUserName());
    }

    @Override
    public SendMessage process(String chatId, Integer msgId) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(question())
                .build();
        return sendMessage(message, Collections.EMPTY_LIST);
    }

    @Override
    public String question() {
        String postfix = isChat ? "@" + properties.getBotUserName() : "";
        return "Привет. Я подскажу свободное время для консультаций у Вики Пашкевич."
                + "\n\nВызови команду: \n/free_time" + postfix
                + "\n\nПосле этого появится меню на три недели. "
                + "Выбери время и пришли его в чат";
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
