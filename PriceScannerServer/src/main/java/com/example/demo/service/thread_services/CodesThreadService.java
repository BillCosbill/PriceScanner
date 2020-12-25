package com.example.demo.service.thread_services;

import com.example.demo.model.Shop;
import com.example.demo.model.dto.CodeDTO;
import com.example.demo.model.enums.EWebScraperOperation;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;

@Slf4j
public class CodesThreadService implements Callable<Set<CodeDTO>> {
    public static final int MIN_DELAY_IN_MILLISECONDS = 10000;
    public static final int MAX_DELAY_IN_MILLISECONDS = 15000;

    private final Shop shop;
    private final long[] codesArray;
    private final String url;
    private final Random randomDelay;

    private final EWebScraperOperation eWebScraperOperation;

    public CodesThreadService(Shop shop, long[] codesArray, EWebScraperOperation eWebScraperOperation) {
        this.shop = shop;
        this.codesArray = codesArray;
        this.url = shop.getUrlToSearchProduct();
        this.eWebScraperOperation = eWebScraperOperation;
        this.randomDelay = new Random();
    }

    @Override
    public Set<CodeDTO> call() {
        switch (eWebScraperOperation) {
            case GET_EXISTING_CODES -> {
                return getExistingCodes();
            }
            case GET_AVAILABLE_CODES -> {
                return getAvailableCodes();
            }
            default -> {
                log.error("Operation " + eWebScraperOperation + " is not supported");
                return Collections.emptySet();
            }
        }
    }

    private Set<CodeDTO> getExistingCodes() {
        Set<CodeDTO> resultSet = new HashSet<>();

        for (long code : codesArray) {
            String fullUrl = url + code;
            randomDelay();

            try {
                Document doc = configureConnection(fullUrl);

                if (isNotRedirecting(fullUrl, doc.location())) {
                    CodeDTO newCode = new CodeDTO();
                    newCode.setShopId(shop.getId());
                    newCode.setCodeValue(code);
                    newCode.setCodeStatus("REDIRECTING");

                    resultSet.add(newCode);
                    log.info("FOUND CODE: " + code + ", URL: " + fullUrl);
                }

            } catch (IOException e) {
                CodeDTO newCode = new CodeDTO();
                newCode.setShopId(shop.getId());
                newCode.setCodeValue(code);
                newCode.setCodeStatus("UNKNOWN");

                resultSet.add(newCode);
                log.error("Interrupted at code number " + code);
            }
        }

        return resultSet;
    }

    private boolean isNotRedirecting(String fullUrl, String location) {
        return !location.equals(fullUrl);
    }

    private Set<CodeDTO> getAvailableCodes() {
        Set<CodeDTO> resultSet = new HashSet<>();

        for (long code : codesArray) {
            randomDelay();
            String fullUrl = shop.getUrlToSearchProduct() + code;

            try {
                Document doc = configureConnection(fullUrl);

                if (isProductNotFound(doc)) {
                    CodeDTO newCode = new CodeDTO();
                    newCode.setShopId(shop.getId());
                    newCode.setCodeValue(code);
                    newCode.setCodeStatus("NOT_FOUND");
                    resultSet.add(newCode);

                    log.error("Product with code: " + code + " not found");
                } else {
                    if (isAnUnsupportedBehavior(fullUrl, doc)) {
                        CodeDTO newCode = new CodeDTO();
                        newCode.setShopId(shop.getId());
                        newCode.setCodeValue(code);
                        newCode.setCodeStatus("UNKNOWN");
                        resultSet.add(newCode);

                        log.error("Something went wrong checking product: " + fullUrl);
                    } else {
                        if (isWithdrawn(doc)) {
                            CodeDTO newCode = new CodeDTO();
                            newCode.setShopId(shop.getId());
                            newCode.setCodeValue(code);
                            newCode.setCodeStatus("WITHDRAWN");
                            resultSet.add(newCode);

                            log.error("Product with code: " + code + " is withdrawn");
                        }
                    }
                }
            } catch (IOException e) {
                CodeDTO newCode = new CodeDTO();
                newCode.setShopId(shop.getId());
                newCode.setCodeValue(code);
                newCode.setCodeStatus("UNKNOWN");
                resultSet.add(newCode);

                log.error("IOException at code number " + code, e);
            }
        }

        return resultSet;
    }

    private boolean isAnUnsupportedBehavior(String fullUrl, Document doc) {
        return doc.getElementsByClass("sc-1jultii-1")
                .first() == null || fullUrl.equals(doc.location());
    }

    private boolean isWithdrawn(Document document) {
        String availabilityInformation = document.getElementsByClass("sc-1jultii-1").first().text();
        return availabilityInformation.equals("Wycofany");
    }

    private boolean isProductNotFound(Document doc) {
        return doc.getElementsByClass("lw463u-5").first() != null && doc.getElementsByClass("lw463u-5").first().text().equals("Przepraszamy");
    }

    private Document configureConnection(String fullUrl) throws IOException {
        // TODO check your useragent: https://gs.statcounter.com/detect
        return Jsoup.connect(fullUrl)
                // TODO set other request headers: https://httpbin.org/anything
                // TODO keep changing useragent to avoid beeing blocked https://developers.whatismybrowser.com/useragents/explore/operating_system_name/windows/
                .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36")
                // TODO set referer, fe. google: https://www.scraperapi.com/blog/5-tips-for-web-scraping/
                .referrer("https://www.x-kom.pl/")
                .timeout(60 * 1000)
                .get();
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


// TODO checking connection status if JSoup doesnt work

//                HttpURLConnection connection = (HttpURLConnection) new URL(fullUrl).openConnection();
//                connection.setInstanceFollowRedirects(false);
//                connection.addRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36");
//                connection.addRequestProperty("REFERER", "https://www.x-kom.pl/");
//                connection.setConnectTimeout(10000);
//                connection.setReadTimeout(10000);
//                statusCode = connection.getResponseCode();
//                if (statusCode == HttpURLConnection.HTTP_MOVED_PERM || statusCode == HttpURLConnection.HTTP_MOVED_TEMP) {
//                    CodeDTO newCode = new CodeDTO();
//                    newCode.setShopId(shop.getId());
//                    newCode.setCodeValue(code);
//                    newCode.setCodeStatus("REDIRECTING");
//
//                    resultSet.add(newCode);
//                    log.info("FOUND CODE: " + code + ", URL: " + fullUrl);
//                }
//                connection.disconnect();
