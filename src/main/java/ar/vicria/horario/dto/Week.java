package ar.vicria.horario.dto;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Week {

    LUNES("пн ", 1),
    MARTES("вт ", 2),
    MIERCOLES("ср ", 3),
    JUEVES("чт ", 4),
    VIERNES("пт ", 5),
    SABADO("сб ", 6),
    DOMINGO("вс ", 7);

    private String rus;
    private Integer number;

    Week(String rus, Integer number) {
        this.rus = rus;
        this.number = number;
    }

    public static Week init(Integer number) {
        return Arrays.stream(Week.values())
                .filter(w -> w.getNumber().equals(number))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("существует только 7 дней недели"));
    }
}
