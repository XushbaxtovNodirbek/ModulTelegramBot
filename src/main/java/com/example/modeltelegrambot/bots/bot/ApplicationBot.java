package com.example.modeltelegrambot.bots.bot;

import com.example.modeltelegrambot.config.BotConfig;
import com.example.modeltelegrambot.service.impl.ChatServiceImpl;
import com.example.modeltelegrambot.service.impl.UserServiceImpl;
import com.example.modeltelegrambot.service.kursValService.ValyutaService;
import com.example.modeltelegrambot.service.namozVaqtServis.NamozVaqtiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMemberCount;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ApplicationBot extends TelegramLongPollingBot {
    private final BotConfig config;
    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getBotToken();
    }

    private final UserServiceImpl userService;
    private final NamozVaqtiService namozVaqtiService;
    private final ValyutaService valyutaService;
    private final ChatServiceImpl chatService;

    @Override
    public void onUpdateReceived(Update update) {
        System.out.println(update);
        if (update.hasMessage()){
            var message = update.getMessage();
            var userId = message.getFrom().getId();
            var user = message.getFrom();
            if (message.getFrom().getId().equals(message.getChatId())) {
                if (userService.getUser(userId) == null) {
                    userService.addUser(userId, user.getFirstName(), user.getUserName());

                }
                if (message.hasText()) {
                    var messageText = message.getText();
                    switch (messageText) {
                        case "/start" -> {

                        }

                    }
                }
            }else {
                Long chatId = message.getChatId();
                if (chatService.getChat(chatId)==null){
                    chatService.addChat(message.getChat().getTitle(),getChatMembersCount(chatId),chatId);
                    SendFirstMessageToChat(chatId);
                }
                switch (message.getText()){
                    case "/namoz@kunlikyordamchibot","/namoz"->SendNamazTimeToChat(chatId);
                    case "/kurs@kunlikyordamchibot","/kurs"->SendKursInfoToChat(chatId);
                    case "/help@kunlikyordamchibot","/help"->SendHelpMessageToChat(chatId);
                }
            }
        }else if(update.hasCallbackQuery()){
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String data = callbackQuery.getData();
            Message message = callbackQuery.getMessage();
            if (message.getChat().getId().equals(callbackQuery.getFrom().getId())){
                switch (data){
                    case "uz","ru"->{
                        DeleteMessage(message.getChatId(),message.getMessageId());
                        userService.setIsUzbek(message.getChatId(),data.equals("uz"));
                    }
                }
            }else {
                switch (data){
                    case "namoz"-> {
                        DeleteMessage(message.getChatId(),message.getMessageId());
                        SendNamazTimeToChat(message.getChatId());
                    }
                    case "valyuta"-> {
                        DeleteMessage(message.getChatId(),message.getMessageId());
                        SendKursInfoToChat(message.getChatId());
                    }
                    case "helpUz"-> {
                        DeleteMessage(message.getChatId(),message.getMessageId());
                        SendBotInfo(message.getChatId(), true);
                    }
                    case "helpRu"-> {
                        DeleteMessage(message.getChatId(),message.getMessageId());
                        SendBotInfo(message.getChatId(), false);
                    }
                }
            }
        }
    }

    private void SendKursInfoToChat(Long chatId) {
        SendPhoto sendPhoto =new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(new InputFile(valyutaService.getIMG()));
        sendPhoto.setReplyMarkup(KursKeyboard(true));
        sendPhoto.setCaption(
                "\uD83C\uDFE6Markaziy Bank valyuta kurslari.\n\n@kunlikyordamchibot"
        );

        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    private void SendHelpMessageToChat(Long chatId){
        SendMessage sendMessage = new SendMessage(chatId.toString(),"Til tanlang\uD83C\uDDFA\uD83C\uDDFF\nВыберите язык\uD83C\uDDF7\uD83C\uDDFA");
        sendMessage.setReplyMarkup(SelectLanguageChat());

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    private void SendBotInfo(Long id,boolean isUzbek){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(id);
        if (isUzbek)
            sendMessage.setText(
                    "Bot yordamida kunlik valyuta kurslari va butun O'zbekiston bo'yicha namoz vaqtlari haqida ma'lumotlar olishingiz mumkin." +
                            "Malumotlar islom.uz va nbu.uz web saytlaridan to'g'ridan to'g'ri olib tarqatiladi.\n\n@kunlikyordamchibot"
            );
        else
            sendMessage.setText(
                    "С помощью бота вы можете получить информацию о ежедневных курсах валют и времени молитв по всему Узбекистану." +
                            "Информация распространяется напрямую с сайтов islom.uz и nbu.uz.\n\n@kunlikyordamchibot"
            );

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    private void SendNamazTimeToChat(Long chatId){
        SendPhoto sendPhoto =new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(new InputFile(namozVaqtiService.getIMG()));
        sendPhoto.setReplyMarkup(NamazKeyboard(true));
        sendPhoto.setCaption(
                "Namozni to'kis ado etinglar, albatta namoz mo'minlarga vaqtida farz qilingandir!\n\n@kunlikyordamchibot"
        );

        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void SendFirstMessageToChat(Long chatId){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setParseMode(ParseMode.HTML);
        sendMessage.setChatId(chatId);
        sendMessage.setText(
                "<b>Assalomu alaykum Kunlik Yoramchi bot aktivlashdi.</b>\n\n" +
                "<b>Foydali buyruqlar ro'yxati:</b>\n" +
                        "<b>1.</b> /namoz - <b>Butun O'zbekiston bo'yicha namoz vaqtlari.</b>\n" +
                        "<b>2.</b> /kurs - <b>Ayni vaqtdagi valyuta kurslari bo'yicha ma'lumot.</b>\n" +
                        "<b>3.</b> /help - <b>Bot haqida batafsil ma'lumot.</b>\n\n" +
                        "@kunlikyordamchibot <b>Kuningiz hayrli bo'lsin!</b>"
        );

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    // get group members
    private int getChatMembersCount(Long chatId){
        try {
            GetChatMemberCount getChatMemberCount =new GetChatMemberCount();
            getChatMemberCount.setChatId(chatId);
            return execute(getChatMemberCount);
        }catch (Exception e){
            return 0;
        }
    }
    private void DeleteMessage(Long id,int messageId){
        DeleteMessage deleteMessage = new DeleteMessage(id.toString(),messageId);
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    // Keyboards

    private InlineKeyboardMarkup NamazKeyboard(boolean isUzbek){
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        if (isUzbek) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText("Yangilash♻\uFE0F");
            button.setCallbackData("namoz");
            row.add(button);
            button = new InlineKeyboardButton();
            button.setCallbackData("valyuta");
            button.setText("Valyuta Kurslari\uD83D\uDCB9");
            row.add(button);
        }else {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText("Обновить♻\uFE0F");
            button.setCallbackData("namoz");
            row.add(button);
            button = new InlineKeyboardButton();
            button.setCallbackData("valyuta");
            button.setText("Курсы обмена\uD83D\uDCB9");
            row.add(button);
        }
        keyboard.add(row);
        markup.setKeyboard(keyboard);
        return markup;

    }
    private InlineKeyboardMarkup KursKeyboard(boolean isUzbek){
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        if (isUzbek) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText("Yangilash♻\uFE0F");
            button.setCallbackData("valyuta");
            row.add(button);
            button = new InlineKeyboardButton();
            button.setCallbackData("namoz");
            button.setText("Namoz vaqtalri⏰");
            row.add(button);
        }else {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText("Обновить♻\uFE0F");
            button.setCallbackData("namoz");
            row.add(button);
            button = new InlineKeyboardButton();
            button.setCallbackData("valyuta");
            button.setText("Время молитвы⏰");
            row.add(button);
        }
        keyboard.add(row);
        markup.setKeyboard(keyboard);
        return markup;

    }
    private InlineKeyboardMarkup SelectLanguageChat() {
        InlineKeyboardMarkup markup =new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row  = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("O'zbekcha\uD83C\uDDFA\uD83C\uDDFF");
        button.setCallbackData("helpUz");
        row.add(button);
        button = new InlineKeyboardButton();
        button.setText("Русский\uD83C\uDDF7\uD83C\uDDFA");
        button.setCallbackData("helpRu");
        row.add(button);
        keyboard.add(row);
        markup.setKeyboard(keyboard);
        return markup;
    }
    private InlineKeyboardMarkup SelectLanguageUser() {
        InlineKeyboardMarkup markup =new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row  = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("O'zbekcha\uD83C\uDDFA\uD83C\uDDFF");
        button.setCallbackData("uz");
        row.add(button);
        button = new InlineKeyboardButton();
        button.setText("Русский\uD83C\uDDF7\uD83C\uDDFA");
        button.setCallbackData("ru");
        row.add(button);
        keyboard.add(row);
        markup.setKeyboard(keyboard);
        return markup;
    }

}
