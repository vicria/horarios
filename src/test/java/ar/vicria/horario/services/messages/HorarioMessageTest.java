package ar.vicria.horario.services.messages;

import ar.vicria.horario.mapper.EventMapper;
import ar.vicria.horario.services.GoogleCalendarClient;
import ar.vicria.horario.services.util.RowUtil;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HorarioMessageTest {

    @Mock
    public GoogleCalendarClient client;

    private EventMapper mapper = new EventMapper();
    private RowUtil util = new RowUtil();


    @Test
    public void question() throws Exception {
        HorarioMessage horarioMessage = new HorarioMessage(util, client, mapper);
        List<Event> responseEntity = new ArrayList<>();
        var lunes = new Event();
        var start = new EventDateTime();
        var startGoogle = new com.google.api.client.util.DateTime(new DateTime(2023, 7, 7, 10, 30).toDate());
        var endGoogle = new com.google.api.client.util.DateTime(new DateTime(2023, 7, 7, 11, 30).toDate());
        start.setDateTime(startGoogle);
        lunes.setStart(start);

        var end = new EventDateTime();
        end.setDateTime(endGoogle);
        lunes.setEnd(end);

        responseEntity.add(lunes);

        when(client.getMySchedule()).thenReturn(responseEntity);

        String question = horarioMessage.question();
        assertEquals("<b>Расписание на этой неделе</b> \n" +
                "пт 7.7:  10:30-11:30 \n", question);
    }
}