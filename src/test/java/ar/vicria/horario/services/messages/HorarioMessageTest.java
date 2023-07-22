package ar.vicria.horario.services.messages;

import ar.vicria.horario.dto.EventDto;
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
import java.util.LinkedList;
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
        var startGoogle = new com.google.api.client.util.DateTime(new DateTime(2023, 7, 6, 10, 30).toDate());
        var endGoogle = new com.google.api.client.util.DateTime(new DateTime(2023, 7, 6, 11, 30).toDate());
        start.setDateTime(startGoogle);
        lunes.setStart(start);

        var end = new EventDateTime();
        end.setDateTime(endGoogle);
        lunes.setEnd(end);

        responseEntity.add(lunes);

        when(client.getMySchedule(0)).thenReturn(responseEntity);

        String question = horarioMessage.question();
        assertEquals("<b>Расписание на этой неделе</b> \n" +
                "чт 06.07:  10:30-11:30 \n", question);
    }

    @Test
    public void teaTime() {
        HorarioMessage horarioMessage = new HorarioMessage(util, client, mapper);

        List<EventDto> events = new LinkedList<>();
        EventDto hour = new EventDto();
        hour.setStart(new DateTime(2023, 7, 7, 10, 30));
        hour.setEnd(new DateTime(2023, 7, 7, 11, 30));
        events.add(hour);

        EventDto notHour = new EventDto();
        notHour.setStart(new DateTime(2023, 7, 7, 12, 30));
        notHour.setEnd(new DateTime(2023, 7, 7, 13, 40));
        events.add(notHour);

        List<EventDto> eventDtos = horarioMessage.teaTime(events);

        assertEquals(new DateTime(2023, 7, 7, 10, 00), eventDtos.get(0).getStart());
        assertEquals(new DateTime(2023, 7, 7, 12, 00), eventDtos.get(0).getEnd());

        assertEquals(new DateTime(2023, 7, 7, 12, 30), eventDtos.get(1).getStart());
        assertEquals(new DateTime(2023, 7, 7, 13, 40), eventDtos.get(1).getEnd());
    }


    @Test
    public void inverse() {
        HorarioMessage horarioMessage = new HorarioMessage(util, client, mapper);

        List<EventDto> events = new ArrayList<>();
        events.add(new EventDto(new DateTime(2023, 7, 7, 11, 00), new DateTime(2023, 7, 7, 12, 00)));
        events.add(new EventDto(new DateTime(2023, 7, 7, 14, 00), new DateTime(2023, 7, 7, 15, 00)));
        events.add(new EventDto(new DateTime(2023, 7, 7, 18, 00), new DateTime(2023, 7, 7, 19, 00)));

        List<EventDto> freeWindows = horarioMessage.inverse(events);

        assertEquals(new DateTime(2023, 7, 7, 12, 30), freeWindows.get(0).getStart());
        assertEquals(new DateTime(2023, 7, 7, 13, 30), freeWindows.get(0).getEnd());
    }

    @Test
    public void inverse2() {
        HorarioMessage horarioMessage = new HorarioMessage(util, client, mapper);

        List<EventDto> events = new ArrayList<>();
        events.add(new EventDto(new DateTime(2023, 7, 7, 10, 00), new DateTime(2023, 7, 7, 12, 30)));
        events.add(new EventDto(new DateTime(2023, 7, 7, 13, 00), new DateTime(2023, 7, 7, 14, 00)));

        List<EventDto> freeWindows = horarioMessage.inverse(events);

        assertEquals(1, freeWindows.size());
    }

    @Test
    public void inverse3() {
        HorarioMessage horarioMessage = new HorarioMessage(util, client, mapper);

        List<EventDto> events = new ArrayList<>();
        events.add(new EventDto(new DateTime(2023, 7, 7, 14, 30), new DateTime(2023, 7, 7, 16, 30)));

        List<EventDto> freeWindows = horarioMessage.inverse(events);

        assertEquals(2, freeWindows.size());
    }

    @Test
    public void inverse4() {
        HorarioMessage horarioMessage = new HorarioMessage(util, client, mapper);

        List<EventDto> events = new ArrayList<>();
        events.add(new EventDto(
                new DateTime(2023, 7, 7, 10, 0),
                new DateTime(2023, 7, 7, 12, 30)));
        events.add(new EventDto(
                new DateTime(2023, 7, 7, 17, 00),
                new DateTime(2023, 7, 7, 22, 00)));

        List<EventDto> freeWindows = horarioMessage.inverse(events);

        assertEquals(1, freeWindows.size());
    }

    @Test
    public void inverse5() {
        HorarioMessage horarioMessage = new HorarioMessage(util, client, mapper);
        horarioMessage.startTime = 16;
        horarioMessage.endTime = 23;

        List<EventDto> events = new ArrayList<>();
        events.add(new EventDto(
                new DateTime(2023, 7, 7, 16, 0),
                new DateTime(2023, 7, 7, 19, 00)));
        events.add(new EventDto(
                new DateTime(2023, 7, 7, 19, 30),
                new DateTime(2023, 7, 7, 21, 30)));
        events.add(new EventDto(
                new DateTime(2023, 7, 7, 21, 30),
                new DateTime(2023, 7, 7, 22, 00)));

        List<EventDto> freeWindows = horarioMessage.inverse(events);

        assertEquals(1, freeWindows.size());
        assertEquals(22, freeWindows.get(0).getStart().getHourOfDay());
        assertEquals(0, freeWindows.get(0).getEnd().getHourOfDay());
    }
}