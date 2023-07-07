package ar.vicria.horario.services.messages;

import ar.vicria.horario.services.util.RowUtil;
import ar.vicria.horario.dto.EventDto;
import ar.vicria.horario.dto.Week;
import ar.vicria.horario.services.callbacks.dto.AnswerDto;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Base class for responding on text messages.
 */
public abstract class TextMessage {

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
    protected TextMessage(RowUtil rowUtil) {
        this.rowUtil = rowUtil;
    }

    /**
     * Rule for search a class.
     *
     * @param msg not required
     * @return use this class or not
     */
    public abstract boolean supports(String msg);

    /**
     * Generation a message for user.
     *
     * @param chatId number of user chat
     * @return message for sending
     */
    public abstract SendMessage process(String chatId) throws Exception;

    /**
     * text in message.
     *
     * @return text
     */
    abstract String question() throws Exception;

    /**
     * default method for query message.
     *
     * @param questionText text in msg
     * @param questionId   id for discussion and answers
     * @param answers      buttons
     * @param chatId       number of user chat
     * @return msg
     */
    SendMessage postQuestionFirst(String questionText,
                                  String questionId,
                                  List<AnswerDto> answers,
                                  String chatId) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(questionText)
                .build();

        InlineKeyboardMarkup rows = rowUtil.createRows(answers, questionId);
        message.setReplyMarkup(rows);
        return message;
    }

    /**
     * Simple msg for user.
     *
     * @param message     text in msg
     * @param buttonNames text on buttons
     * @return msg
     */
    SendMessage sendMessage(SendMessage message, List<String> buttonNames) {
        List<KeyboardRow> rows = new ArrayList<>();
        for (String button : buttonNames) {
            KeyboardRow row = new KeyboardRow();
            row.add(KeyboardButton.builder().text(button).build());
            rows.add(row);
        }

        message.setReplyMarkup(new ReplyKeyboardMarkup(rows));
        message.setParseMode("HTML");
        return message;
    }

    String horario(Map<Week, List<EventDto>> collect) {
        List<Week> sorted = new ArrayList<>(collect.keySet());
        var horario = new StringBuilder();
        sorted.sort(Comparator.comparingInt(Week::getNumber));
        for (var day : sorted) {
            horario.append(day.getRus());
            horario.append(collect.get(day).get(0).getStart().getDayOfMonth());
            horario.append(".");
            horario.append(collect.get(day).get(0).getStart().getMonthOfYear());
            horario.append(": ");
            List<String> eventDtos = collect.get(day).stream()
                    .map(time -> {
                        StringBuilder times = new StringBuilder();
                        times.append(time.getStart().getHourOfDay());
                        times.append(":");
                        int minuteOfHour1 = time.getStart().getMinuteOfHour();
                        times.append(minuteOfHour1 < 9 ? "00" : minuteOfHour1);
                        times.append("-");
                        times.append(time.getEnd().getHourOfDay());
                        times.append(":");
                        int minuteOfHour = time.getEnd().getMinuteOfHour();
                        times.append(minuteOfHour < 9 ? "00" : minuteOfHour);
                        times.append(" ");
                        return times.toString();
                    }).collect(Collectors.toList());
            horario.append(" ");
            horario.append(String.join(" ", eventDtos));
            horario.append("\n");
        }
        return horario.toString();
    }
}
