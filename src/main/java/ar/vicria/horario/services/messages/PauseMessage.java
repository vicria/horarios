package ar.vicria.horario.services.messages;

import ar.vicria.horario.dto.AnswerData;
import ar.vicria.horario.dto.AnswerDto;
import ar.vicria.horario.properties.TelegramProperties;
import ar.vicria.horario.services.TelegramConnector;
import ar.vicria.horario.services.util.RowUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.groupadministration.SetChatPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberOwner;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * set photo chat to WAIT..
 */
@Slf4j
@Component
public class PauseMessage extends TextMessage {


    private final TelegramProperties properties;
    private final TelegramConnector connector;
    private String manager;

    /**
     * Constrictor.
     *
     * @param rowUtil    util for telegram menu
     * @param properties
     * @param connector
     */
    protected PauseMessage(RowUtil rowUtil,
                           TelegramProperties properties,
                           @Lazy TelegramConnector connector) {
        super(rowUtil);
        this.properties = properties;
        this.connector = connector;
    }

    @Override
    public boolean supports(AnswerData answerData, String msg) {
        return msg.equals("/pause@" + properties.getBotUserName())
                && properties.getAdmins().contains(answerData.getQuestionId());
    }

    @Override
    public SendMessage process(String chatId, Integer msgId) {
        InputStream inputStream = Objects.requireNonNull(getClass().getResourceAsStream("/chat/wait.jpg"));
        var inputFile = new InputFile(inputStream, "wait.jpg");
        SetChatPhoto chatPhoto = SetChatPhoto.builder()
                .chatId(chatId)
                .photo(inputFile)
                .build();
        String text = question();

        try {
            GetChatAdministrators getChatAdministrators = new GetChatAdministrators();
            getChatAdministrators.setChatId(chatId);
            manager = connector.execute(getChatAdministrators)
                    .stream()
                    .filter(admin -> admin instanceof ChatMemberOwner)
                    .map(admin -> (ChatMemberOwner) admin)
                    .map(admin -> admin.getUser().getUserName())
                    .findFirst()
                    .map((name) -> String.format("@%s", name))
                    .orElseGet(() -> "");
            text = question();
            connector.execute(chatPhoto);
        } catch (TelegramApiException e) {
            log.error("не смогли установить фото ожидания");
            text += "\n\nСделай бот админом, пожалуйста";
        }

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        return sendMessage(message, Collections.emptyList());
    }

    @Override
    public String question() {
        return String.format("%s следующую встречу не назначали", manager);
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
