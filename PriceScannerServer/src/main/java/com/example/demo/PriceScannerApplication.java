package com.example.demo;

import com.example.demo.service.ScannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class PriceScannerApplication {

    @Autowired
    private ScannerService scannerService;

    public static void main(String[] args) {
        SpringApplication.run(PriceScannerApplication.class, args);
    }


    @EventListener(ApplicationReadyEvent.class)
    public void test() {
//        scannerService.getExistsCodes();
        scannerService.getExistingCodes("https://www.x-kom.pl/p/", 0, 1000);

    }

}
