package ar.vicria.horario.services.messages;

import ar.vicria.horario.dto.AnswerData;
import ar.vicria.horario.dto.AnswerDto;
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

    /**
     * Constrictor.
     * @param rowUtil util for telegram menu
     */
    protected StartMessage(RowUtil rowUtil) {
        super(rowUtil);
    }

    @Override
    public boolean supports(AnswerData answerData, String msg) {
        return msg.equals("/start");
    }

    @Override
    public SendMessage process(String chatId, Integer msgId) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(question())
                .build();
        return sendMessage(message, Collections.emptyList());
    }

    @Override
    public String question() {
        return "Бот рабочего расписания Вики Пашкевич. Вызови команду /free_time";
    }

    /**
     * buttons.
     * @return buttons
     */
    @Override
    public List<AnswerDto> answer() {
        return Collections.emptyList();
    }
}
