package com.example.modeltelegrambot.service.kursValService;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import java.util.Objects;

@Service
public class ValyutaService {
    private final Gson gson = new Gson();

    private JsonArray getData(){
        try {
        URL url = new URL("https://nbu.uz/uz/exchange-rates/json/");
        URLConnection urlConnection = url.openConnection();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        return gson.fromJson(bufferedReader, JsonArray.class);
        }catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    public void refreshData(){
        String source = "/root/projects/yordamchi/img/shablon/Valyuta_shablon.png";
        String out = "/root/projects/yordamchi/img/tmp/Valyuta_shablon.png";
        try {
            BufferedImage image = ImageIO.read(new File(source));

            Graphics2D graphics = image.createGraphics();

            InputStream is = getClass().getResourceAsStream("/AgencyFB-Bold.ttf");
            Font custom = Font.createFont(Font.TRUETYPE_FONT, is);

            Font font = new Font(custom.getName(), Font.BOLD, 180);
            Color textColor = new Color(2,64, 63);

            graphics.setFont(font);
            graphics.setColor(textColor);

            int x1 = 710,x2=x1+350+1000,x3 = x2 +1200;
            int y1 = 1700,y2 = y1 + 340,y3 = y2 + 830,y4 = y3 +340;

            for (JsonElement data : Objects.requireNonNull(getData())) {
                switch (data.getAsJsonObject().get("code").getAsString()){
                    case "USD"->{
                        graphics.drawString(data.getAsJsonObject().get("nbu_buy_price").getAsString(),x1,y1);
                        graphics.drawString(data.getAsJsonObject().get("nbu_cell_price").getAsString(),x1,y2);
                    }
                    case "RUB"->{
                        graphics.drawString(data.getAsJsonObject().get("nbu_buy_price").getAsString(),x2,y1);
                        graphics.drawString(data.getAsJsonObject().get("nbu_cell_price").getAsString(),x2,y2);
                    }
                    case "EUR"->{
                        graphics.drawString(data.getAsJsonObject().get("nbu_buy_price").getAsString(),x3,y1);
                        graphics.drawString(data.getAsJsonObject().get("nbu_cell_price").getAsString(),x3,y2);
                    }
                    case "TRY"->{
                        graphics.drawString((Double.parseDouble(data.getAsJsonObject().get("cb_price").getAsString())-45)+" ",x1+15,y3);
                        graphics.drawString(data.getAsJsonObject().get("cb_price").getAsString(),x1+15,y4);
                    }
                    case "KZT"->{
                        graphics.drawString(data.getAsJsonObject().get("nbu_buy_price").getAsString(),x2,y3);
                        graphics.drawString(data.getAsJsonObject().get("nbu_cell_price").getAsString(),x2,y4);
                    }
                    case "JPY"->{
                        graphics.drawString(data.getAsJsonObject().get("nbu_buy_price").getAsString(),x3+80,y3);
                        graphics.drawString(data.getAsJsonObject().get("nbu_cell_price").getAsString(),x3+80,y4);
                    }
                }
                graphics.drawString(data.getAsJsonObject().get("date").getAsString().replaceAll("\\.","-"),x2-200,y4+850);
            }

            graphics.dispose();

            ImageIO.write(image, "png", new File(out));


        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
    public File getIMG(){
        return new File("/root/projects/yordamchi/img/tmp/Valyuta_shablon.png");
    }
    @Scheduled(cron = "0 1 0 * * *",zone = "GMT+5:00")
    private void refresh(){
        refreshData();
    }
    @Scheduled(cron = "0 0 3 * * *",zone = "GMT+5:00")
    private void refresh1(){
        refreshData();
    }
}
