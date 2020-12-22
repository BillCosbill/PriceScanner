package com.example.demo.service.thread_services;

import com.example.demo.model.Code;
import com.example.demo.model.Shop;
import com.example.demo.model.enums.CodeStatus;
import com.example.demo.model.enums.WebScraperOperation;
import com.example.demo.service.CodeService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

@Slf4j
public class CodesRunnableThreadServiceTest implements Runnable{
    public static final int MIN_DELAY_IN_MILLISECONDS = 10000;
    public static final int MAX_DELAY_IN_MILLISECONDS = 15000;

    private final Shop shop;
    private final long[] codesArray;
    private final String url;
    private final Random randomDelay;

    private final WebScraperOperation webScraperOperation;

    public CodesRunnableThreadServiceTest(Shop shop, long[] codesArray, WebScraperOperation webScraperOperation) {
        this.shop = shop;
        this.codesArray = codesArray;
        this.url = shop.getUrlToSearchProduct();
        this.webScraperOperation = webScraperOperation;
        this.randomDelay = new Random();
    }


    @Override
    public void run() {
        switch (webScraperOperation) {
            case GET_EXISTING_CODES -> getExistingCodes();
            case GET_AVAILABLE_CODES -> getAvailableCodes();
            default -> log.error("Operation " + webScraperOperation + " is not supported");

        }
    }

    private void getExistingCodes() {
        int statusCode;

        for (long code : codesArray) {
            String fullUrl = url + code;
            randomDelay();

            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(fullUrl).openConnection();
                connection.setInstanceFollowRedirects(false);
                // TODO set other request headers: https://httpbin.org/anything
                // TODO keep changing useragent to avoid beeing blocked https://developers.whatismybrowser.com/useragents/explore/operating_system_name/windows/
                // TODO set referer, fe. google: https://www.scraperapi.com/blog/5-tips-for-web-scraping/
                connection.addRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36");
                connection.addRequestProperty("REFERER", "https://www.x-kom.pl/");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);


                statusCode = connection.getResponseCode();

                if (statusCode == HttpURLConnection.HTTP_MOVED_PERM || statusCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                    Code newCode = new Code();
                    newCode.setShop(shop);
                    newCode.setCode(code);
                    newCode.setCodeStatus(CodeStatus.REDIRECTING);

                    CodeService.codesSet.add(newCode);
                    log.info("FOUND CODE: " + code + ", URL: " + fullUrl);
                }

                connection.disconnect();
            } catch (IOException e) {
                Code newCode = new Code();
                newCode.setShop(shop);
                newCode.setCode(code);
                newCode.setCodeStatus(CodeStatus.UNKNOWN);

                CodeService.codesSet.add(newCode);
                log.error("Interrupted at code number " + code);
            }
        }
    }


    private void getAvailableCodes() {
        for (long code : codesArray) {

            randomDelay();

            String fullUrl = shop.getUrlToSearchProduct() + code;

            try {
                // TODO check your useragent: https://gs.statcounter.com/detect
                Document doc = Jsoup.connect(fullUrl)
                        // TODO set other request headers: https://httpbin.org/anything
                        // TODO keep changing useragent to avoid beeing blocked https://developers.whatismybrowser.com/useragents/explore/operating_system_name/windows/
                        .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36")
                        // TODO set referer, fe. google: https://www.scraperapi.com/blog/5-tips-for-web-scraping/
                        .referrer("https://www.x-kom.pl/")
                        .timeout(60 * 1000).get();

                if (doc.getElementsByClass("lw463u-5").first() != null && doc.getElementsByClass("lw463u-5").first().text().equals("Przepraszamy")) {
                    Code newCode = new Code();
                    newCode.setShop(shop);
                    newCode.setCode(code);
                    newCode.setCodeStatus(CodeStatus.NOT_FOUND);

                    CodeService.codesSet.add(newCode);
                    log.error("Product with code: " + code + " not found");
                } else {
                    if (doc.getElementsByClass("sc-1jultii-1").first() == null || fullUrl.equals(doc.location())) {
                        Code newCode = new Code();
                        newCode.setShop(shop);
                        newCode.setCode(code);
                        newCode.setCodeStatus(CodeStatus.UNKNOWN);

                        CodeService.codesSet.add(newCode);
                        log.error("Something went wrong checking product: " + fullUrl);
                    } else {
                        String cannotBeBought = doc.getElementsByClass("sc-1jultii-1").first().text();

                        if (!cannotBeBought.equals("Wycofany")) {
                            Code newCode = new Code();
                            newCode.setShop(shop);
                            newCode.setCode(code);
                            newCode.setCodeStatus(CodeStatus.WITHDRAWN);
                            CodeService.codesSet.add(newCode);
                        }
                    }
                }
            } catch (IOException e) {
                Code newCode = new Code();
                newCode.setShop(shop);
                newCode.setCode(code);
                newCode.setCodeStatus(CodeStatus.UNKNOWN);

                CodeService.codesSet.add(newCode);
                log.error("IOException at code number " + code, e);
            }
        }
    }

    private void randomDelay() {
        try {
            Thread.sleep(randomDelay.nextInt(MAX_DELAY_IN_MILLISECONDS - MIN_DELAY_IN_MILLISECONDS) + (long) MIN_DELAY_IN_MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("Error while trying to make sleep delay at current thread");
            Thread.currentThread()
                    .interrupt();
        }
    }
}
