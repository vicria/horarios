package ar.vicria.horario.dto;

import lombok.Data;
import org.joda.time.DateTime;

@Data
public class EventDto {

    DateTime start;

    DateTime end;
}
