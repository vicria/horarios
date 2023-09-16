package ar.vicria.horario.services.messages;

import ar.vicria.horario.dto.AnswerData;
import ar.vicria.horario.dto.AnswerDto;
import ar.vicria.horario.dto.EventDto;
import ar.vicria.horario.dto.Week;
import ar.vicria.horario.services.util.RowUtil;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Base class for responding on text messages.
 */
public abstract class TextMessage {

    public Integer hours = 6;
    public String city = "Москва";

    public Integer startTime = 10;
    public Integer endTime = 18;

    public EventDto workTime = new EventDto(new DateTime(2023, 7, 7, startTime, 0),
            new DateTime(2023, 7, 7, endTime, 0));

    public List<Week> workDays = Arrays.asList(Week.LUNES, Week.JUEVES, Week.DOMINGO);

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
        List<KeyboardRow> rows = buttonNames.stream()
                .map(KeyboardButton::new)
                .map(b -> new KeyboardRow(Collections.singleton(b)))
                .collect(Collectors.toList());

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
            //в день могут попадать даты прошлой недели, если в календаре стоит событие больше одного дня
            Comparator<EventDto> comparator = Comparator.comparingInt(e -> e.getStart().getDayOfWeek());
            events.sort(comparator.reversed());
            EventDto eventThisDay = events.stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("events is empty"));
            events = events.stream()
                    .filter(e -> e.getStart().getDayOfMonth() == eventThisDay.getStart().getDayOfMonth())
                    .collect(Collectors.toList());
            events.sort(Comparator.comparing(EventDto::getStart));

            DateTime start = events.get(0).getStart();
            DateTime dateTime = new DateTime(start.getYear(), start.getMonthOfYear(), start.getDayOfMonth(), startTime, 0);
            workTime.setStart(dateTime);

            DateTime end = events.get(0).getEnd();
            DateTime dateTime2 = new DateTime(end.getYear(), end.getMonthOfYear(), end.getDayOfMonth(), endTime, 0);
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

        if (startDuration.getStandardHours() >= 1 && eventSt.isAfter(startTime)) {
            ruleAdding(startTime, eventSt).ifPresent(freeWindows::add);
            startTime = eventSt;
        }

        // Проверяем каждое событие в списке
        for (EventDto event : withTeaTime) {
            DateTime eventStart = event.getStart();
            DateTime eventEnd = event.getEnd();

            // Если есть промежуток между начальным временем и временем начала события, добавляем свободное окно
            if (eventStart.isAfter(startTime)) {
                ruleAdding(startTime, eventStart).ifPresent(freeWindows::add);
            }

            // Обновляем начальное время для следующего итерации
            startTime = eventEnd;
        }

        // Проверяем, есть ли свободное окно после последнего события
        if (workTime.getEnd().isAfter(startTime)) {
            ruleAdding(startTime, workTime.getEnd()).ifPresent(freeWindows::add);
        }

        return freeWindows;
    }

    private Optional<EventDto> ruleAdding(DateTime startTime, DateTime endTime) {
        Duration windowDuration = new Duration(startTime, endTime);

        // Если длительность окна равна одному часу, добавляем его в список свободных окон
        if (windowDuration.getStandardHours() >= 1) {
            return Optional.of(new EventDto(startTime, endTime));
        }
        return Optional.empty();
    }

    String horario(Map<Week, List<EventDto>> collect) {
        var horario = new StringBuilder();
        for (var day : workDays) {
            if (!collect.containsKey(day)) {
                continue;
            }
            horario.append(day.getRus());
            int dayOfMonth = collect.get(day).get(0).getStart().getDayOfMonth();
            horario.append(dayOfMonth <= 9 ? "0" + dayOfMonth : dayOfMonth);
            horario.append(".");
            int monthOfYear = collect.get(day).get(0).getStart().getMonthOfYear();
            horario.append(monthOfYear <= 9 ? "0" + monthOfYear : monthOfYear);
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
                new AnswerDto("Лондон", 4),
                new AnswerDto("Варшава", 5),
                new AnswerDto("\uD83D\uDD04", 500));
    }
}
