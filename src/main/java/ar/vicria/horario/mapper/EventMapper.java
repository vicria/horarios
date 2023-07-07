package ar.vicria.horario.mapper;

import ar.vicria.horario.dto.EventDto;
import com.google.api.services.calendar.model.Event;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    private final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public EventDto toDto(Event event) {
        EventDto eventDto = new EventDto();
        String start = event.getStart().getDateTime().toString();
        eventDto.setStart(formatter.parseDateTime(start));
        String end = event.getEnd().getDateTime().toString();
        eventDto.setEnd(formatter.parseDateTime(end));
        return eventDto;
    }
}
