package ar.vicria.horario.services.messages;

import ar.vicria.horario.dto.AnswerData;
import ar.vicria.horario.dto.AnswerDto;
import ar.vicria.horario.properties.TelegramProperties;
import ar.vicria.horario.services.util.RowUtil;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

/**
 * Text msg after /free_time.
 */
@Order(HIGHEST_PRECEDENCE)
@Component
public class AddUserMessage extends TextMessage {

    private final TelegramProperties properties;
    private String user;

    /**
     * Constrictor.
     *
     * @param rowUtil    util for telegram menu
     * @param properties
     */
    protected AddUserMessage(RowUtil rowUtil,
                             TelegramProperties properties) {
        super(rowUtil);
        this.properties = properties;
    }

    @Override
    public boolean supports(AnswerData answerData, String msg) {
        boolean contains = msg.contains("/add_user_");
        if (contains) {
            this.user = msg.replaceFirst("/add_user_", "");

        }
        return contains;
    }

    @Override
    public BotApiMethod process(String chatId, Integer msgId) throws Exception {
        List<String> admins = properties.getAdmins();
        admins.add(this.user);
        properties.setAdmins(admins);
        return postQuestionFirst(question(), getClass().getSimpleName(), answer(), chatId);
    }

    @Override
    public String question() throws Exception {
        return String.format("пользователь %s добавлен", user);
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
