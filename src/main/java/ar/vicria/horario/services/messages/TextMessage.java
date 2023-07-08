package ar.vicria.horario.services.messages;

import ar.vicria.horario.dto.AnswerData;
import ar.vicria.horario.services.util.RowUtil;
import ar.vicria.horario.dto.EventDto;
import ar.vicria.horario.dto.Week;
import ar.vicria.horario.dto.AnswerDto;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Base class for responding on text messages.
 */
public abstract class TextMessage {

    public EventDto workTime = new EventDto(new DateTime(2023, 7, 7, 10, 00),
            new DateTime(2023, 7, 7, 18, 00));

    private List<Week> workDays = Arrays.asList(Week.LUNES, Week.MARTES, Week.MIERCOLES, Week.JUEVES);

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
    public abstract boolean supports(AnswerData answerData, String msg);

    /**
     * Generation a message for user.
     *
     * @param chatId number of user chat
     * @return message for sending
     */
    public abstract BotApiMethod process(String chatId, Integer msgId) throws Exception;

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
        message.setParseMode("HTML");
        message.setReplyMarkup(rows);
        return message;
    }

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

    List<EventDto> teaTime(List<EventDto> events) {
        events.forEach(event -> {
            long differenceMillis = Math.abs(event.getEnd().getMillis() - event.getStart().getMillis());
            Duration oneHour = Duration.standardHours(1);
            if (differenceMillis == oneHour.getMillis()) {
                event.setStart(event.getStart().minusMinutes(30));
                event.setEnd(event.getEnd().plusMinutes(30));
            }
        });
        return events;
    }

    List<EventDto> inverse(List<EventDto> events) {
        if (!events.isEmpty()) {
            DateTime start = events.get(0).getStart();
            DateTime dateTime = new DateTime(start.getYear(), start.getMonthOfYear(), start.getDayOfMonth(), 10, 00);
            workTime.setStart(dateTime);

            DateTime end = events.get(0).getEnd();
            DateTime dateTime2 = new DateTime(end.getYear(), end.getMonthOfYear(), end.getDayOfMonth(), 18, 00);
            workTime.setEnd(dateTime2);
        }
        List<EventDto> withTeaTime = teaTime(events);
        withTeaTime.sort(Comparator.comparing(EventDto::getStart));

        // Создаем список для хранения свободных окон
        List<EventDto> freeWindows = new ArrayList<>();

        // Инициализируем начальное время свободного окна
        DateTime startTime = workTime.getStart();

        DateTime eventSt = events.isEmpty() ? workTime.getEnd() : events.get(0).getStart();
        Duration startDuration = new Duration(startTime, eventSt);

        //todo много дат
//        while (startDuration.getStandardHours() >= 1 && eventSt.isAfter(startTime)) {
//            EventDto window = new EventDto(startTime, startTime.plusHours(1));
//            freeWindows.add(window);
//            startTime = startTime.plusHours(1);
//            startDuration = new Duration(startTime, eventSt);
//        }

        if (startDuration.getStandardHours() >= 1 && eventSt.isAfter(startTime)) {
            EventDto window = new EventDto(startTime, eventSt);
            freeWindows.add(window);
            startTime = eventSt;
        }

        // Проверяем каждое событие в списке
        for (EventDto event : withTeaTime) {
            DateTime eventStart = event.getStart();
            DateTime eventEnd = event.getEnd();

            // Если есть промежуток между начальным временем и временем начала события, добавляем свободное окно
            if (eventStart.isAfter(startTime)) {
                Duration windowDuration = new Duration(startTime, eventStart);

                // Если длительность окна равна одному часу, добавляем его в список свободных окон
                if (windowDuration.getStandardHours() == 1) {
                    EventDto freeWindow = new EventDto(startTime, eventStart);
                    freeWindows.add(freeWindow);
                }
            }

            // Обновляем начальное время для следующего итерации
            startTime = eventEnd;
        }

        //todo много дат
//        while (workTime.getEnd().isAfter(startTime)) {
//            EventDto window = new EventDto(startTime, startTime.plusHours(1));
//            freeWindows.add(window);
//            startTime = startTime.plusHours(1);
//        }

        // Проверяем, есть ли свободное окно после последнего события
        if (workTime.getEnd().isAfter(startTime)) {
            EventDto window = new EventDto(startTime, workTime.getEnd());
            freeWindows.add(window);
        }

        return freeWindows;
    }

    String horario(Map<Week, List<EventDto>> collect) {
        var horario = new StringBuilder();
        for (var day : workDays) {
            if (!collect.containsKey(day)) {
                continue;
            }
            horario.append(day.getRus());
            horario.append(collect.get(day).get(0).getStart().getDayOfMonth());
            horario.append(".");
            horario.append(collect.get(day).get(0).getStart().getMonthOfYear());
            horario.append(": ");
            List<String> eventDtos = collect.get(day).stream()
                    .map(time -> {
                        StringBuilder times = new StringBuilder();
                        int hourOfDay = time.getStart().getHourOfDay();
                        times.append(hourOfDay == 0 ? "00" : hourOfDay);
                        times.append(":");
                        int minuteOfHour1 = time.getStart().getMinuteOfHour();
                        times.append(minuteOfHour1 < 9 ? "00" : minuteOfHour1);
                        times.append("-");
                        int hourOfDay2 = time.getEnd().getHourOfDay();
                        times.append(hourOfDay2 == 0 ? "00" : hourOfDay2);
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

    List<AnswerDto> answer() {
        return Arrays.asList(new AnswerDto("Москва", 6),
                new AnswerDto("Буэнос Айрес", 0),
                new AnswerDto("Варшава", 5));
    }
}
