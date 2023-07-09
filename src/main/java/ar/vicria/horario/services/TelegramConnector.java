package ar.vicria.horario.services;

import ar.vicria.horario.properties.TelegramProperties;
import ar.vicria.horario.dto.AnswerData;
import ar.vicria.horario.services.messages.TextMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Telegram adapter.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramConnector extends TelegramLongPollingBot {

    private final TelegramProperties properties;

    private final List<TextMessage> messages;

    @PostConstruct
    private void init() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(this);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Unable to register telegram bot", e);
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() || update.hasCallbackQuery()) {
            Message message = update.hasCallbackQuery() ? update.getCallbackQuery().getMessage() : update.getMessage();
            log.info("Received answer: name = {}; text = {}", message.getFrom().getFirstName(), message.getText());

            String chatId = message.getFrom().getId().toString();
            String msg = message.getText();


            if (msg.contains("@" + getBotUsername())) {
                // Бот был упомянут в сообщении
                chatId = message.getChat().getId().toString();
            }

            AnswerData answerData = null;
            if (update.hasCallbackQuery()) {
                chatId = String.valueOf(message.getChat().getId());
                String messageText = update.getCallbackQuery().getData();

                if (AnswerData.match(messageText)) {
                    answerData = AnswerData.deserialize(messageText);
                }
            }

            String finalChatId = chatId;
            AnswerData finalAnswerData = answerData;
            Optional<BotApiMethod> process = messages.stream()
                    .filter(m -> m.supports(finalAnswerData, msg))
                    .findFirst()
                    .map(m -> {
                        try {
                            return Optional.of(m.process(finalChatId, message.getMessageId()));
                        } catch (Exception e) {
                            e.printStackTrace();
                            return Optional.<BotApiMethod>empty();
                        }
                    })
                    .orElseGet(Optional::empty);

            try {
                if (process.isPresent()) {
                    execute(process.get());
                }
            } catch (TelegramApiException e) {
                log.error("Unable to send message", e);
            }

        }
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        updates.forEach(this::onUpdateReceived);
    }

    @Override
    public String getBotToken() {
        return properties.getBotToken();
    }

    @Override
    public String getBotUsername() {
        return properties.getBotUserName();
    }
}
