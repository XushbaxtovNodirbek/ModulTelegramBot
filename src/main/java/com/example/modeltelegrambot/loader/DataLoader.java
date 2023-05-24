package com.example.modeltelegrambot.loader;

import com.example.modeltelegrambot.service.kursValService.ValyutaService;
import com.example.modeltelegrambot.service.namozVaqtServis.NamozVaqtiService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class DataLoader implements CommandLineRunner {
    private final ValyutaService valyutaService;
    private final NamozVaqtiService namozVaqtiService;
    @Override
    public void run(String... args) throws Exception {
        valyutaService.refreshData();
        namozVaqtiService.refreshTimes();
    }
}
