package ar.vicria.horario.services.messages;

import ar.vicria.horario.services.util.RowUtil;
import ar.vicria.horario.dto.EventDto;
import ar.vicria.horario.dto.Week;
import ar.vicria.horario.mapper.EventMapper;
import ar.vicria.horario.services.GoogleCalendarClient;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Text msg after /free_time.
 */
@Component
public class HorarioMessage extends TextMessage {

    private final GoogleCalendarClient client;
    private final EventMapper mapper;

    /**
     * Constrictor.
     *
     * @param rowUtil util for telegram menu
     * @param client
     * @param mapper
     */
    protected HorarioMessage(RowUtil rowUtil, GoogleCalendarClient client, EventMapper mapper) {
        super(rowUtil);
        this.client = client;
        this.mapper = mapper;
    }

    @Override
    public boolean supports(String msg) {
        return msg.equals("/horario");
    }

    @Override
    public SendMessage process(String chatId) throws Exception {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(question())
                .build();
        return sendMessage(message, answer());
    }

    @Override
    public String question() throws Exception {
        Map<Week, List<EventDto>> collect = client.getMySchedule().stream()
                .map(mapper::toDto)
                .collect(Collectors.groupingBy(event -> {
                    int dayOfWeek = event.getStart().getDayOfWeek();
                    return Week.init(dayOfWeek);
                }));

        String horario = horario(collect);

        return "<b>Расписание на этой неделе</b> \n" + horario;
    }

    /**
     * buttons.
     *
     * @return buttons
     */
    public List<String> answer() {
        return Collections.emptyList();
    }
}
