package ar.vicria.horario.services.callbacks;

import ar.vicria.horario.services.util.RowUtil;
import ar.vicria.horario.services.callbacks.dto.AnswerData;
import ar.vicria.horario.services.callbacks.dto.AnswerDto;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Use for default.
 */
@Component
public class DefaultQuery extends Query {

    public DefaultQuery(RowUtil rowUtil) {
        super(rowUtil);
    }

    @Override
    public boolean supports(AnswerData answerData, String msg) {
        return true;
    }

    @Override
    public List<AnswerDto> answer(String... option) {
        return new ArrayList<>();
    }

    @Override
    public Optional<BotApiMethod> process(Integer msgId, String chatId, String msg, AnswerData answerData) {
        return Optional.empty();
    }
}
