package ar.vicria.horario.services.messages;

import ar.vicria.horario.dto.AnswerData;
import ar.vicria.horario.dto.AnswerDto;
import ar.vicria.horario.properties.TelegramProperties;
import ar.vicria.horario.services.TelegramConnector;
import ar.vicria.horario.services.util.RowUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.groupadministration.SetChatPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * set photo chat to Run.
 */
@Slf4j
@Component
public class RunMessage extends TextMessage {


    private final TelegramProperties properties;
    private final TelegramConnector connector;
    private Integer messageId;

    private final String FILE_NAME = "run.jpg";

    /**
     * Constrictor.
     *
     * @param rowUtil    util for telegram menu
     * @param properties
     * @param connector
     */
    protected RunMessage(RowUtil rowUtil,
                         TelegramProperties properties,
                         @Lazy TelegramConnector connector) {
        super(rowUtil);
        this.properties = properties;
        this.connector = connector;
    }

    @Override
    public boolean supports(AnswerData answerData, String msg) {
        return msg.contains("us06web.zoom.us")
                && properties.getAdmins().contains(answerData.getQuestionId());
    }

    @Override
    public BotApiMethod process(String chatId, Integer msgId) {
        InputStream inputStream = Objects.requireNonNull(getClass().getResourceAsStream("/chat/" + FILE_NAME));
        var inputFile = new InputFile(inputStream, FILE_NAME);
        SetChatPhoto chatPhoto = SetChatPhoto.builder()
                .chatId(chatId)
                .photo(inputFile)
                .build();
        SendMessage send = SendMessage.builder()
                .text("Занятие назначено")
                .chatId(chatId)
                .build();
        try {
            connector.execute(chatPhoto);
            Message execute = connector.execute(send);
            Integer messageId = execute.getMessageId();
            DeleteMessage build = DeleteMessage.builder()
                    .chatId(chatId)
                    .messageId(messageId)
                    .build();
            connector.execute(build);
            this.messageId = messageId - 1;

        } catch (TelegramApiException e) {
            log.error("не смогли установить фото ожидания");
        }


        return DeleteMessage.builder()
                .chatId(chatId)
                .messageId(this.messageId)
                .build();
    }

    @Override
    public String question() {
        return "";
    }

    /**
     * buttons.
     *
     * @return buttons
     */
    @Override
    public List<AnswerDto> answer() {
        return Collections.emptyList();
    }
}
