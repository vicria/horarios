package ar.vicria.horario.services.messages;

import ar.vicria.horario.RowUtil;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Collections;
import java.util.List;

/**
 * Text msg after /start.
 */
@Component
public class StartMessage extends TextMessage {

    /**
     * Constrictor.
     * @param rowUtil util for telegram menu
     */
    protected StartMessage(RowUtil rowUtil) {
        super(rowUtil);
    }

    @Override
    public boolean supports(String msg) {
        return msg.equals("/start");
    }

    @Override
    public SendMessage process(String chatId) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(question())
                .build();
        return sendMessage(message, answer());
    }

    @Override
    public String question() {
        return "Бот рабочего расписания Вики Пашкевич. Вызови команду /free_time";
    }

    /**
     * buttons.
     * @return buttons
     */
    public List<String> answer() {
        return Collections.emptyList();
    }
}
