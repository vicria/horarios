package ar.vicria.horario;

import ar.vicria.horario.services.callbacks.dto.AnswerData;
import ar.vicria.horario.services.callbacks.dto.AnswerDto;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Util for working with UI telegram.
 */
@Component
public class RowUtil {
    
    private final static int NUM_COLUMNS = 2;

    /**
     * create rows for buttons.
     *
     * @param answers    buttons
     * @param questionId id of query or message for answer
     * @return menu
     */
    public InlineKeyboardMarkup createRows(List<AnswerDto> answers, String questionId) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        
        int numRows = (int) Math.ceil((double) answers.size() / NUM_COLUMNS);

        for (int row = 0; row < numRows; row++) {
            int start = row * NUM_COLUMNS;
            int end = Math.min(start + NUM_COLUMNS, answers.size());

            List<InlineKeyboardButton> currentRow = new ArrayList<>();
            for (int j = start; j < end; j++) {
                AnswerDto answer = answers.get(j);
                InlineKeyboardButton button = InlineKeyboardButton.builder()
                        .text(answer.getText())
                        .callbackData(AnswerData.serialize(questionId, answer))
                        .build();
                currentRow.add(button);
            }
            rows.add(currentRow);
        }
        markupInline.setKeyboard(rows);
        return markupInline;
    }
}
