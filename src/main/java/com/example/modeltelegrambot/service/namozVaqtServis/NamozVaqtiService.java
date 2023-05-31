package com.example.modeltelegrambot.service.namozVaqtServis;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class NamozVaqtiService {
    private final ArrayList<String> regions = new ArrayList<>(List.of(
            "Toshkent",
            "Angren",
            "Jizzax",
            "Qarshi",
            "Namangan",
            "Navoiy",
            "Samarqand",
            "Denov",
            "Guliston",
            "Farg'ona",
            "Xiva",
            "Nukus",
            "Buxoro"
    ));
    private final Gson gson =new Gson();

    private List<JsonObject> getTimes(){
        ArrayList<JsonObject> times = new ArrayList<>();
        try {
        for (String region : regions) {
            URL url = new URL("https://islomapi.uz/api/present/day?region="+region);
            URLConnection urlConnection = url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            times.add(gson.fromJson(bufferedReader,JsonObject.class));
        }
        return times;
        }catch (Exception e){
            System.out.printf(e.getMessage());
            return null;
        }
    }

    public void refreshTimes(){
        String source = "/root/projects/yordamchi/img/shablon/Namoz_shablon.png";
        String out = "/root/projects/yordamchi/img/tmp/Namoz_shablon.png";
        try {
            BufferedImage image = ImageIO.read(new File(source));

            Graphics2D graphics = image.createGraphics();

            InputStream is = getClass().getResourceAsStream("/AgencyFB-Bold.ttf");
            Font custom = Font.createFont(Font.TRUETYPE_FONT, is);

            Font font = new Font(custom.getName(), Font.BOLD, 180);
            Color textColor = new Color(225,155,0);

            graphics.setFont(font);
            graphics.setColor(textColor);

            int x1 = 1350,x2 = x1+500,x3 = x2+500,x4= x3+500,x5 = x4+500,x6=x5+500;
            int y;
            y = 1100;

            for (JsonObject time : Objects.requireNonNull(getTimes())) {
                graphics.drawString(time.get("times").getAsJsonObject().get("tong_saharlik").getAsString(), x1, y);
                graphics.drawString(time.get("times").getAsJsonObject().get("quyosh").getAsString(), x2, y);
                graphics.drawString(time.get("times").getAsJsonObject().get("peshin").getAsString(), x3, y);
                graphics.drawString(time.get("times").getAsJsonObject().get("asr").getAsString(), x4, y);
                graphics.drawString(time.get("times").getAsJsonObject().get("shom_iftor").getAsString(), x5, y);
                graphics.drawString(time.get("times").getAsJsonObject().get("hufton").getAsString(), x6, y);
                y+=215;
            }

            graphics.setColor(Color.white);
            graphics.drawString(getTimes().get(0).get("date").getAsString(),x2,y+300-120-20);

            graphics.dispose();

            ImageIO.write(image, "png", new File(out));

        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public File getIMG(){
        return new File("/root/projects/yordamchi/img/tmp/Namoz_shablon.png");
    }

    @Scheduled(cron = "0 1 0 * * *",zone = "GMT+5:00")
    private void refresh(){
        refreshTimes();
    }
    @Scheduled(cron = "0 1 3 * * *",zone = "GMT+5:00")
    private void refresh1(){
        refreshTimes();
    }
}
