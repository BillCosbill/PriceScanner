package com.example.demo.service;

import com.example.demo.model.Code;
import com.example.demo.model.Shop;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

@Slf4j
public class AvailableCodesScannerService extends Thread {

    private Shop shop;

    private int startCode;
    private int finishCode;

    public AvailableCodesScannerService(int startCode, int finishCode, Shop shop) {
        this.startCode = startCode;
        this.finishCode = finishCode;
        this.shop = shop;
    }

    @Override
    public void run() {

        for (int i=startCode; i<finishCode; i++) {

            Code code = shop.getProductCodes().get(i);
            String fullUrl = shop.getUrlToSearchProduct() + code.getCode();

            try {
                Document doc = Jsoup.connect(fullUrl).get();

                if (!fullUrl.equals(doc.location())) {
                    String cannotBeBought = doc.getElementsByClass("sc-1jultii-1").first().text();

                    if(!cannotBeBought.equals("Wycofany")) {
                        ScannerService.availableCodesSet.add(code);
                        log.info("Available code found: " + code.getCode());
                    }
                }
            } catch (IOException e) {
                log.error("IOException at code number " + i, e);
            }

        }

    }
}
