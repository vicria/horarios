package ar.vicria.horario.services.callbacks;

import ar.vicria.horario.RowUtil;
import ar.vicria.horario.services.callbacks.dto.AnswerData;
import ar.vicria.horario.services.callbacks.dto.AnswerDto;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;
import java.util.Optional;

/**
 * Base class for responding on callback query messages.
 */
public abstract class Query {

    /**
     * id for discussion and answers.
     *
     * @return id
     */
    public String queryId() {
        return this.getClass().getSimpleName();
    }

    private final RowUtil rowUtil;

    /**
     * Constrictor.
     *
     * @param rowUtil util for telegram menu
     */
    protected Query(RowUtil rowUtil) {
        this.rowUtil = rowUtil;
    }

    /**
     * Rule for search a class.
     *
     * @param msg        not required
     * @param answerData answer in query, what the button pressed
     * @return use this class or not
     */
    public abstract boolean supports(AnswerData answerData, String msg);

    /**
     * Buttons.
     *
     * @param option condition for choosing buttons names
     * @return text and numbers of buttons
     */
    abstract List<AnswerDto> answer(String... option);

    EditMessageText postQuestionEdit(Integer messageId,
                                     String questionText,
                                     String questionId,
                                     List<AnswerDto> answers,
                                     String chatId) {
        EditMessageText editMessageText = EditMessageText.builder()
                .messageId(messageId)
                .chatId(chatId)
                .text(questionText)
                .build();

        InlineKeyboardMarkup rows = rowUtil.createRows(answers, questionId);
        editMessageText.setParseMode("HTML");
        editMessageText.setReplyMarkup(rows);
        return editMessageText;
    }

    /**
     * Generation a message for user.
     *
     * @param chatId     number of user chat
     * @param answerData user pressed the button
     * @param msg        new msg for user
     * @param msgId      for message must to edit
     * @return message for sending
     */
    public abstract Optional<BotApiMethod> process(Integer msgId, String chatId, String msg, AnswerData answerData);

}
