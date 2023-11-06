package ar.vicria.horario.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Text inside the button for understandings the user answer.
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AnswerData {

    private static final String PREFIX = "/answer";
    private static final String DELIMITER = "#";

    private String questionId;
    private Integer answerCode;

    /**
     * getting text for button.
     *
     * @param questionId query id
     * @param answer     name and number of the button
     * @return text
     */
    public static String serialize(String questionId, AnswerDto answer) {
        List<String> builder = new ArrayList<>() {
        };
        builder.add(PREFIX);
        builder.add(questionId);
        builder.add(answer.getCode().toString());
        return String.join(DELIMITER, builder);
    }

    /**
     * correct text inside the button.
     *
     * @param text text inside the button
     * @return correct or not
     */
    public static boolean match(String text) {
        if (text.isBlank() || !text.startsWith(PREFIX)) {
            return false;
        }
        String[] parts = text.split(Pattern.quote(DELIMITER));
        if (parts.length != 3) {
            return false;
        }
        try {
            Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * Understanding text inside the button.
     *
     * @param text code for deserialize
     * @return pressed data
     */
    public static AnswerData deserialize(String text) {
        Pattern pattern = Pattern.compile("^" + Pattern.quote(PREFIX + DELIMITER)
                + "([^" + DELIMITER + "]+)" + Pattern.quote(DELIMITER) + "(-?\\d+)$");
        Matcher matcher = pattern.matcher(text);
        if (matcher.matches()) {
            String questionId = matcher.group(1);
            Integer answerCode = Integer.valueOf(matcher.group(2));
            return new AnswerData(questionId, answerCode);
        } else {
            throw new IllegalArgumentException("Invalid input: " + text);
        }
    }
}
