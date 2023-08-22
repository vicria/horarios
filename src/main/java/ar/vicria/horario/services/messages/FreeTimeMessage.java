package ar.vicria.horario.services.messages;

import ar.vicria.horario.dto.EventDto;
import ar.vicria.horario.dto.Week;
import ar.vicria.horario.mapper.EventMapper;
import ar.vicria.horario.services.GoogleCalendarClient;
import ar.vicria.horario.dto.AnswerData;
import ar.vicria.horario.dto.AnswerDto;
import ar.vicria.horario.services.util.DateUtil;
import ar.vicria.horario.services.util.RowUtil;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Text msg after /free_time.
 */
@Order(0)
@Component
public class FreeTimeMessage extends TextMessage {

    private final GoogleCalendarClient client;
    private final EventMapper mapper;

    private boolean query;
    private boolean empty = false;

    /**
     * Constrictor.
     *
     * @param rowUtil util for telegram menu
     * @param client
     * @param mapper
     */
    protected FreeTimeMessage(RowUtil rowUtil, GoogleCalendarClient client, EventMapper mapper) {
        super(rowUtil);
        this.client = client;
        this.mapper = mapper;
    }

    @Override
    public boolean supports(AnswerData answerData, String msg) {
        empty = false;
        query = answerData != null;
        if (query) {
            if (!answerData.getAnswerCode().equals(100) && !answerData.getAnswerCode().equals(101)
                    && !answerData.getAnswerCode().equals(500)) {
                hours = answerData.getAnswerCode();
                city = super.answer().stream()
                        .filter(ans -> ans.getCode().equals(answerData.getAnswerCode()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("don't have this city"))
                        .getText();
            }
        } else {
            hours = 6;
            city = "Москва";
        }
        return msg.equals("/free_time@time_vicria_bot")
                || msg.equals("/free_time")
                || answerData != null && answerData.getQuestionId().equals(FreeTimeNextWeekMessage.class.getSimpleName())
                && answerData.getAnswerCode().equals(100)
                || answerData != null && answerData.getQuestionId().equals(getClass().getSimpleName())
                && !answerData.getAnswerCode().equals(100)
                || answerData != null && answerData.getQuestionId().equals(getClass().getSimpleName())
                && answerData.getAnswerCode().equals(500);
    }

    @Override
    public BotApiMethod process(String chatId, Integer msgId) throws Exception {
        if (query) {
            return postQuestionEdit(msgId, question(), getClass().getSimpleName(), answer(), chatId);
        } else {
            return postQuestionFirst(question(), getClass().getSimpleName(), answer(), chatId);
        }
    }

    @Override
    public String question() throws Exception {
        Map<Week, List<EventDto>> collect = client.getMySchedule(0).stream()
                .map(mapper::toDto)
                .collect(Collectors.groupingBy(event -> {
                    int dayOfWeek = event.getEnd().getDayOfWeek();
                    return Week.init(dayOfWeek);
                }));
        Map<Week, List<EventDto>> free = new HashMap<>();
        //не могу брать консультации меньше чем за два часа
        DateTime cutDate = new DateTime(DateUtil.now()).plusHours(2);
        for (var week : collect.keySet()) {
            List<EventDto> eventDtos = collect.get(week);
            List<EventDto> freeWindows = inverse(eventDtos).stream()
                    .filter(eventDto -> eventDto.getStart().getDayOfMonth() >= DateUtil.now().getDayOfMonth())
                    .peek(eventDto -> eventDto.setStart(eventDto.getStart().plusHours(hours)))
                    .peek(eventDto -> eventDto.setEnd(eventDto.getEnd().plusHours(hours)))
                    .filter(eventDto -> {
                        if (week.getNumber() == DateTime.now().getDayOfWeek()
                                && !eventDto.getStart().isAfter(cutDate)) {
                            eventDto.setStart(eventDto.getStart().plusHours(2));
                            long differenceMillis = Math.abs(eventDto.getEnd().getMillis() - eventDto.getStart().getMillis());
                            Duration oneHour = Duration.standardHours(1);
                            return differenceMillis >= oneHour.getMillis();
                        } else
                            return true;
                    })
                    .collect(Collectors.toList());
            if (!freeWindows.isEmpty()) {
                free.put(week, freeWindows);
            }
        }
        String horario = horario(free);

        if (horario.isEmpty()) {
            empty = true;
            return "Свободного времени на этой неделе нет";
        } else return "<b>Свободное время на этой неделе (" + city
                + ")</b> \n" + horario;

    }

    /**
     * buttons.
     *
     * @return buttons
     */
    @Override
    public List<AnswerDto> answer() {
        List<AnswerDto> answerDtos = new ArrayList<>();
        if (!empty) {
            List<AnswerDto> answer = super.answer().stream()
                    .filter(a -> !a.getText().equals(city))
                    .collect(Collectors.toList());
            answerDtos.addAll(answer);
        }
        answerDtos.add(new AnswerDto("➡️ " + DateUtil.week(7), 100));
        return answerDtos;
    }
}
