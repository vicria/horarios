package ar.vicria.horario.services.callbacks.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Answer in query.
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AnswerDto {
    private String text;
    private Integer code;
}
