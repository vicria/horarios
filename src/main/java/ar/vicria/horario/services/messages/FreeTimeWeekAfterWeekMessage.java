package ar.vicria.horario.services.messages;

import ar.vicria.horario.dto.AnswerData;
import ar.vicria.horario.dto.AnswerDto;
import ar.vicria.horario.dto.EventDto;
import ar.vicria.horario.dto.Week;
import ar.vicria.horario.mapper.EventMapper;
import ar.vicria.horario.services.GoogleCalendarClient;
import ar.vicria.horario.services.util.DateUtil;
import ar.vicria.horario.services.util.RowUtil;
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
@Component
public class FreeTimeWeekAfterWeekMessage extends TextMessage {

    private final GoogleCalendarClient client;
    private final EventMapper mapper;

    private boolean empty = false;

    /**
     * Constrictor.
     *
     * @param rowUtil util for telegram menu
     * @param client
     * @param mapper
     */
    protected FreeTimeWeekAfterWeekMessage(RowUtil rowUtil, GoogleCalendarClient client, EventMapper mapper) {
        super(rowUtil);
        this.client = client;
        this.mapper = mapper;
    }

    @Override
    public boolean supports(AnswerData answerData, String msg) {
        if (answerData.getAnswerCode() == null) {
            return false;
        }
        empty = false;
        if (answerData != null && !answerData.getAnswerCode().equals(100) && !answerData.getAnswerCode().equals(101)
                && !answerData.getAnswerCode().equals(500)) {
            hours = answerData.getAnswerCode();
            List<AnswerDto> answer = super.answer();
            city = answer.stream()
                    .filter(ans -> ans.getCode().equals(answerData.getAnswerCode()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("don't have this city"))
                    .getText();
        }
        return answerData != null
                && answerData.getAnswerCode() != null
                && (answerData.getQuestionId().equals(FreeTimeNextWeekMessage.class.getSimpleName())
                && answerData.getAnswerCode().equals(101)

                || answerData.getQuestionId().equals(getClass().getSimpleName())
                && !answerData.getAnswerCode().equals(100)
                && !answerData.getAnswerCode().equals(101)

                || answerData.getQuestionId().equals(getClass().getSimpleName())
                && answerData.getAnswerCode().equals(500));
    }

    @Override
    public BotApiMethod process(String chatId, Integer msgId) throws Exception {
        return postQuestionEdit(msgId, question(), getClass().getSimpleName(), answer(), chatId);
    }

    @Override
    public String question() throws Exception {
        Map<Week, List<EventDto>> collect = client.getMySchedule(14).stream()
                .map(mapper::toDto)
                .collect(Collectors.groupingBy(event -> {
                    int dayOfWeek = event.getEnd().getDayOfWeek();
                    return Week.init(dayOfWeek);
                }));
        Map<Week, List<EventDto>> free = new HashMap<>();
        for (var week : collect.keySet()) {
            List<EventDto> eventDtos = collect.get(week);
            List<EventDto> freeWindows = inverse(eventDtos).stream()
                    .peek(eventDto -> eventDto.setStart(eventDto.getStart().plusHours(hours)))
                    .peek(eventDto -> eventDto.setEnd(eventDto.getEnd().plusHours(hours)))
                    .collect(Collectors.toList());
            if (!freeWindows.isEmpty()) {
                free.put(week, freeWindows);
            }
        }
        String horario = horario(free);

        if (horario.isEmpty()) {
            empty = true;
            return "Свободного времени нет";
        } else return "<b>Свободное время через неделю (" + city
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
        answerDtos.add(new AnswerDto(DateUtil.week(7) + " ⬅️", 100));
        return answerDtos;
    }
}
