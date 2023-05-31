package com.example.modeltelegrambot.bots.bot;

import com.example.modeltelegrambot.config.BotConfig;
import com.example.modeltelegrambot.entity.ChatEntity;
import com.example.modeltelegrambot.entity.UserEntity;
import com.example.modeltelegrambot.service.impl.ChatServiceImpl;
import com.example.modeltelegrambot.service.impl.UserServiceImpl;
import com.example.modeltelegrambot.service.kursValService.ValyutaService;
import com.example.modeltelegrambot.service.namozVaqtServis.NamozVaqtiService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMemberCount;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeChat;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

@Component
@RequiredArgsConstructor
public class ApplicationBot extends TelegramLongPollingBot {
    private final BotConfig config;
    private ArrayList<Long> AdminsId = new ArrayList<>(List.of(1927612883L,5094739326L));
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
//        System.out.println(update);
        if (update.hasMessage()){
            var message = update.getMessage();
            var userId = message.getFrom().getId();
            var user = message.getFrom();
            if (message.getLeftChatMember()!=null){
                if (message.getLeftChatMember().getUserName().equals(getBotUsername())){
                    chatService.deleteChat(message.getChatId());
                    return;
                }
            }
            if (message.getFrom().getId().equals(message.getChatId())) {
                if (userService.getUser(userId) == null) {
                    userService.addUser(userId, user.getFirstName(), user.getUserName());
                    SendSelectLanguage(userId);
                    return;
                }else if (IsAdmin(userId))setBotCommandsForUser(userId);
                if (message.hasText()) {
                    var messageText = message.getText();
                    switch (messageText) {
                        case "/start" -> {
                            if (IsAdmin(userId))setBotCommandsForUser(userId);
                            SendHomeMessage(userId);
                        }
                        case "/lang"->{
                            SendSelectLanguage(userId);
                        }
                        case "/statistika"->{
                            SendStatsMessage(userId);
                        }
                        case "/post"->{
                            if (IsAdmin(userId)){
                                if (message.getReplyToMessage()==null){
                                    SendMessageNotFound(userId);
                                }else{
                                    CheckPostMessage(userId,message.getReplyToMessage().getMessageId());
                                }
                            }
                        }
                        case "/listchats"->{
                            if (IsAdmin(userId)){
                                SendListChats(userId);
                            }
                        }

                    }
                }
            }else {
                Long chatId = message.getChatId();
                if (chatService.getChat(chatId)==null){
                    chatService.addChat(message.getChat().getTitle(),message.getChat().getUserName(),getChatMembersCount(chatId),chatId);
                    SendFirstMessageToChat(chatId);
                }
                switch (message.getText()){
                    case "/start@Kunlik_yordamchi_bot"->SendFirstMessageToChat(chatId);
                    case "/namoz@Kunlik_yordamchi_bot","/namoz"->SendNamazTimeToChat(chatId);
                    case "/kurs@Kunlik_yordamchi_bot","/kurs","/valyuta"->SendKursInfoToChat(chatId);
                    case "/help@Kunlik_yordamchi_bot","/help"->SendHelpMessageToChat(chatId);
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
                        SendHomeMessage(message.getChatId());
                    }
                    case "namoz"-> {
                        DeleteMessage(message.getChatId(),message.getMessageId());
                        SendNamazTimeToUser(message.getChatId());
                    }
                    case "valyuta"-> {
                        DeleteMessage(message.getChatId(),message.getMessageId());
                        SendValyutaInfoToUser(message.getChatId());
                    }
                    case "cancel"-> {
                        DeleteMessage(message.getChatId(),message.getMessageId());
                        SendCancelMessage(message.getChatId());
                    }
                    case "yes"->{
                        Message replyToMessage = message.getReplyToMessage();
                        new Thread(()->{
                            Long fromChatId = message.getChatId();
                            for (int i = 0; i < userService.findAllUsers(0).getTotalPages(); i++) {
                                for (UserEntity user : userService.findAllUsers(i).getContent()) {
                                    SendForwardMessage(fromChatId,user.getUserId(),replyToMessage);
                                }
                            }
                            for (int i = 0; i < chatService.getAllChats(0).getTotalPages(); i++) {
                                for (ChatEntity chat : chatService.getAllChats(i).getContent()) {
                                    SendForwardMessage(fromChatId,chat.getChatId(),replyToMessage);
                                }
                            }

                            while (!Thread.interrupted())
                                Thread.currentThread().interrupt();
                        }).start();
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
        } else if (update.hasChatMember()) {
            update.getChatMember();
        }
    }

    private void SendListChats(Long userId) {
        if (chatService.getAllChatList(0).getTotalPages()<1){
            SendMessage sendMessage = new SendMessage(userId.toString(),"Chatlar Mavjud Emas!");
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
        int count = 1;
        for (int i = 0; i < chatService.getAllChatList(0).getTotalPages(); i++) {
            StringBuilder builder = new StringBuilder();
            for (ChatEntity chat : chatService.getAllChatList(i).getContent()) {
                builder.append(count).append(".");
                builder.append(chat.toString());
                count++;
            }
            SendMessage sendMessage = new SendMessage();
            sendMessage.setText(builder.toString());
            sendMessage.setChatId(userId);

            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private void SendForwardMessage(Long fromChatId,Long toChatId,Message reply){
        CopyMessage copyMessage = new CopyMessage();
        copyMessage.setFromChatId(fromChatId);
        copyMessage.setChatId(toChatId);
        copyMessage.setMessageId(reply.getMessageId());
        copyMessage.setReplyMarkup(reply.getReplyMarkup());

        try {
            execute(copyMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    private void SendCancelMessage(Long chatId) {
        SendMessage sendMessage = new SendMessage(chatId.toString(),"Amal bekor qilindi.");
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    private void CheckPostMessage(Long userId, Integer messageId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userId);
        sendMessage.setReplyToMessageId(messageId);
        sendMessage.setText(
                "Ushbu xabarni post qilmoqchimisiz?"
        );
        sendMessage.setReplyMarkup(PostMarkup());

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    private void SendMessageNotFound(Long userId) {
        SendMessage sendMessage = new SendMessage(userId.toString(),
                "Post qilish uchun habarni reply qiling!"
                );
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    // Methods
    private void SendHomeMessage(Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setParseMode(ParseMode.HTML);
        if (userService.getUser(chatId).isUzbek()){
            sendMessage.setText(
                    "Assalomu alaykum "+userService.getUser(chatId).getFirstName()+", botga xush kelibsiz!\n\n" +
                            "\uD83D\uDCC6 <b>Namoz vaqtlarini</b>\n" +
                            "\uD83D\uDCB9 <b>Valyutalar kurslari</b>\n\n" +
                    "Bilmoqchi bo’lsangiz quyidagi tugmalardan foydalanishingiz mumkin\uD83D\uDC47\uD83C\uDFFD");
            sendMessage.setReplyMarkup(MainKeyboard(true));
        }else {
            sendMessage.setText("Привет "+userService.getUser(chatId).getFirstName()+", добро пожаловать в бот!\n" +
                    "Если вы хотите узнать погоду, курсы валют или время молитвы\n" +
                    "Вы можете использовать следующие кнопки:");
                    sendMessage.setReplyMarkup(MainKeyboard(false));
        }

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    private void SendStatsMessage(Long chatId){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setParseMode(ParseMode.HTML);
        sendMessage.setText(
                "\uD83D\uDC65<b>Botdagi faol obunachilar:</b> "+userService.getUsersCount()+"ta.\n\n" +
                        "<b>Oxirgi 24 soatda:</b> "+userService.getUsersCountDay()+" ta obunachi qoshildi.\n" +
                        "<b>Oxirgi 1 oyda:</b> "+userService.getUsersCountMonth()+" ta obunchi qo'shildi.\n" +
                        "<b>Botdan foydalanuvchi guruhlar soni:</b> "+chatService.getCountAllChats()+" ta.\n" +
                        "<b>Guruhlardagi jami foydalanuvchilar:</b> "+(chatService.getAllMembersCount()==null?0:chatService.getAllMembersCount())+" ta.\n\n" +
                        "\uD83D\uDCCA Bot statistikasi."
        );

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    private boolean IsAdmin(Long chatId){
        for (Long aLong : AdminsId) {
            if (aLong.equals(chatId))return true;
        }
        return false;
    }
    public void setBotCommandsForUser(Long chatId) {
        // create the list of bot commands
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("start", "Chatni qayta yuklash"));
        commands.add(new BotCommand("post", "Post joylash"));
        commands.add(new BotCommand("statistika", "Bot statistikasi"));
        commands.add(new BotCommand("listchats", "Guruhlar ro'yxati"));
        commands.add(new BotCommand("lang", "Til tanlash"));
        // create the SetMyCommands object with the chat ID and the list of commands
        SetMyCommands setMyCommands = new SetMyCommands();
        setMyCommands.setCommands(commands);
        setMyCommands.setScope(new BotCommandScopeChat(chatId.toString()));
        try {
            // execute the SetMyCommands method
            execute(setMyCommands);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void SendKursInfoToChat(Long chatId) {
        SendPhoto sendPhoto =new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setParseMode(ParseMode.HTML);
        sendPhoto.setPhoto(new InputFile(valyutaService.getIMG()));
        sendPhoto.setReplyMarkup(KursKeyboard(true));
        sendPhoto.setCaption(
                """
                        🏦<b>Bugungi kun Markaziy Bank valyuta kurslari</b>
                                              
                        Guruhingiz , kanalingiz va asosiy siz uchun 👉🏽\s
                        @Kunlik_yordamchi_bot"""
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
    private void SendSelectLanguage(Long chatId){
        SendMessage sendMessage = new SendMessage(chatId.toString(),"Til tanlang\uD83C\uDDFA\uD83C\uDDFF\nВыберите язык\uD83C\uDDF7\uD83C\uDDFA");
        sendMessage.setReplyMarkup(SelectLanguageUser());

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
                            "Malumotlar islom.uz va nbu.uz web saytlaridan to'g'ridan to'g'ri olib tarqatiladi.\n\n@Kunlik_yordamchi_bot"
            );
        else
            sendMessage.setText(
                    "С помощью бота вы можете получить информацию о ежедневных курсах валют и времени молитв по всему Узбекистану." +
                            "Информация распространяется напрямую с сайтов islom.uz и nbu.uz.\n\n@Kunlik_yordamchi_bot"
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
        sendPhoto.setParseMode(ParseMode.HTML);
        sendPhoto.setCaption(
                """
                        <b>Bugungi kun Namoz vaqtlari</b>
                                           
                        Namozni to'kis ado etinglar, albatta namoz mo'minlarga
                        vaqtida farz qilingandir!
                                           
                        Guruhingiz , kanalingiz va asosiy siz uchun 👉🏽\s
                        @Kunlik_yordamchi_bot"""
        );

        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    private void SendNamazTimeToUser(Long userId){
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(userId);
        sendPhoto.setParseMode(ParseMode.HTML);
        sendPhoto.setPhoto(new InputFile(namozVaqtiService.getIMG()));
        sendPhoto.setReplyMarkup(NamazKeyboard(userService.getUser(userId).isUzbek()));
        sendPhoto.setCaption(
                """
                        <b>Bugungi kun Namoz vaqtlari</b>
                                           
                        Namozni to'kis ado etinglar, albatta namoz mo'minlarga
                        vaqtida farz qilingandir!
                                           
                        Guruhingiz , kanalingiz va asosiy siz uchun 👉🏽\s
                        @Kunlik_yordamchi_bot"""
        );
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    private void SendValyutaInfoToUser(Long userId){
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(userId);
        sendPhoto.setParseMode(ParseMode.HTML);
        sendPhoto.setPhoto(new InputFile(valyutaService.getIMG()));
        sendPhoto.setReplyMarkup(KursKeyboard(userService.getUser(userId).isUzbek()));
        sendPhoto.setCaption(
                """
                        🏦<b>Bugungi kun Markaziy Bank valyuta kurslari</b>
                                           
                        Guruhingiz , kanalingiz va asosiy siz uchun 👉🏽\s
                        @Kunlik_yordamchi_bot"""
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
                """
                        <b>Assalomu alaykum Kunlik Yoramchi bot aktivlashdi.</b>

                        <b>Foydali buyruqlar ro'yxati:</b>
                        <b>1.</b> /namoz - <b>Butun O'zbekiston bo'yicha namoz vaqtlari.</b>
                        <b>2.</b> /kurs - <b>Ayni vaqtdagi valyuta kurslari bo'yicha ma'lumot.</b>
                        <b>3.</b> /help - <b>Bot haqida batafsil ma'lumot.</b>

                        @Kunlik_yordamchi_bot <b>Kuningiz hayrli bo'lsin!</b>"""
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
            button.setText("Yangilash♻️");
            button.setCallbackData("namoz");
            row.add(button);
            keyboard.add(row);
            row = new ArrayList<>();
            button = new InlineKeyboardButton();
            button.setCallbackData("valyuta");
            button.setText("Valyuta Kurslari\uD83D\uDCB9");
            row.add(button);
            keyboard.add(row);
            row = new ArrayList<>();
            button = new InlineKeyboardButton();
            button.setUrl("https://t.me/Kunlik_yordamchi_bot?startgroup=new");
            button.setText("➕ Botni guruhga qo'shish ➕");
            row.add(button);
        }else {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText("Обновить♻️");
            button.setCallbackData("namoz");
            row.add(button);
            keyboard.add(row);
            row = new ArrayList<>();
            button = new InlineKeyboardButton();
            button.setCallbackData("valyuta");
            button.setText("Курсы обмена\uD83D\uDCB9");
            row.add(button);
            keyboard.add(row);
            row = new ArrayList<>();
            button = new InlineKeyboardButton();
            button.setUrl("https://t.me/Kunlik_yordamchi_bot?startgroup=new");
            button.setText("➕ Добавить бота в группу ➕");
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
            keyboard.add(row);
            row = new ArrayList<>();
            button = new InlineKeyboardButton();
            button.setCallbackData("namoz");
            button.setText("Namoz vaqtlari⏰");
            row.add(button);
            keyboard.add(row);
            row = new ArrayList<>();
            button = new InlineKeyboardButton();
            button.setUrl("https://t.me/Kunlik_yordamchi_bot?startgroup=new");
            button.setText("➕ Botni guruhga qo'shish ➕");
            row.add(button);
        }else {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText("Обновить♻\uFE0F");
            button.setCallbackData("valyuta");
            row.add(button);
            keyboard.add(row);
            row = new ArrayList<>();
            button = new InlineKeyboardButton();
            button.setCallbackData("namoz");
            button.setText("Время молитвы⏰");
            row.add(button);
            keyboard.add(row);
            row = new ArrayList<>();
            button = new InlineKeyboardButton();
            button.setUrl("https://t.me/Kunlik_yordamchi_bot?startgroup=new");
            button.setText("➕ Добавить бота в группу ➕");
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
    private ReplyKeyboard MainKeyboard(boolean bool) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button;
        if (bool){
            button = new InlineKeyboardButton();
            button.setCallbackData("namoz");
            button.setText("Namoz vaqtlari⏰");
            row.add(button);
            keyboard.add(row);

            row = new ArrayList<>();
            button = new InlineKeyboardButton();
            button.setCallbackData("valyuta");
            button.setText("Valyuta Kurslari\uD83D\uDCB9");
            row.add(button);

            keyboard.add(row);
            row = new ArrayList<>();
            button = new InlineKeyboardButton();
            button.setUrl("https://t.me/Kunlik_yordamchi_bot?startgroup=new");
            button.setText("➕ Botni guruhga qo'shish ➕");
            row.add(button);
        }else {
            button = new InlineKeyboardButton();
            button.setCallbackData("namoz");
            button.setText("Время молитвы⏰");
            row.add(button);
            keyboard.add(row);

            row = new ArrayList<>();
            button = new InlineKeyboardButton();
            button.setCallbackData("valyuta");
            button.setText("Курсы обмена\uD83D\uDCB9");
            row.add(button);

            keyboard.add(row);
            row = new ArrayList<>();
            button = new InlineKeyboardButton();
            button.setUrl("https://t.me/Kunlik_yordamchi_bot?startgroup=new");
            button.setText("➕ Добавить бота в группу ➕");
            row.add(button);
        }
        keyboard.add(row);
        markup.setKeyboard(keyboard);
        return markup;
    }
    private ReplyKeyboard PostMarkup() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Ha");
        button.setCallbackData("yes");
        row.add(button);
        button = new InlineKeyboardButton();
        button.setCallbackData("cancel");
        button.setText("Yoq");
        row.add(button);
        keyboard.add(row);
        markup.setKeyboard(keyboard);
        return markup;
    }

    @Scheduled(cron = "0 0 4 * * *",zone = "GMT+5:00")
    private void firstScheduled(){
        SendNamazTimesInNewThread();
    }
    @Scheduled(cron = "0 0 12 * * *",zone = "GMT+5:00")
    private void secondScheduled(){
        SendNamazTimesInNewThread();
    }
    @Scheduled(cron = "0 0 18 * * *",zone = "GMT+5:00")
    private void thirdScheduled(){
        SendNamazTimesInNewThread();
    }

    @Scheduled(cron = "0 0 10 * * *",zone = "GMT+5:00")
    private void test(){
        SendKursInNewThread();
    }

    private void SendNamazTimesInNewThread(){
        new Thread(()->{
            for (int i = 0; i < userService.findAllUsers(0).getTotalPages(); i++) {
                for (UserEntity user : userService.findAllUsers(i).getContent()) {
                    SendNamazTimeToUser(user.getUserId());
                }
            }
            for (int i = 0; i < chatService.getAllChats(0).getTotalPages(); i++) {
                for (ChatEntity chat : chatService.getAllChats(i).getContent()) {
                    SendNamazTimeToChat(chat.getChatId());
                }
            }

            while (!Thread.interrupted())
                Thread.currentThread().interrupt();
        }).start();
    }
    private void SendKursInNewThread(){
        new Thread(()->{
            for (int i = 0; i < userService.findAllUsers(0).getTotalPages(); i++) {
                for (UserEntity user : userService.findAllUsers(i).getContent()) {
                    SendValyutaInfoToUser(user.getUserId());
                }
            }
            for (int i = 0; i < chatService.getAllChats(0).getTotalPages(); i++) {
                for (ChatEntity chat : chatService.getAllChats(i).getContent()) {
                    SendKursInfoToChat(chat.getChatId());
                }
            }

            while (!Thread.interrupted())
                Thread.currentThread().interrupt();
        }).start();
    }

}
