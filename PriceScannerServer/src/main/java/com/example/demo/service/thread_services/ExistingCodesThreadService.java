package com.example.demo.service.thread_services;

import com.example.demo.service.ExistingCodesService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

@Slf4j
public class ExistingCodesThreadService extends Thread {

    public static final int MIN_DELAY_IN_MILLISECONDS = 2000;
    public static final int MAX_DELAY_IN_MILLISECONDS = 5000;

    private final int startCode;
    private final int finishCode;

    private final String url;
    private final Random randomDelay;

    public ExistingCodesThreadService(int startCode, int finishCode, String url) {
        this.startCode = startCode;
        this.finishCode = finishCode;
        this.url = url;
        this.randomDelay = new Random();
    }

    @Override
    public void run() {
        int statusCode;

        for (int i = startCode; i < finishCode; i++) {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url + i).openConnection();
                connection.setInstanceFollowRedirects(false);
                // TODO set other request headers: https://httpbin.org/anything
                // TODO keep changing useragent to avoid beeing blocked https://developers.whatismybrowser.com/useragents/explore/operating_system_name/windows/
                // TODO set referer, fe. google: https://www.scraperapi.com/blog/5-tips-for-web-scraping/
                connection.addRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36");
                connection.addRequestProperty("REFERER", "https://www.x-kom.pl/");

                statusCode = connection.getResponseCode();

                if (statusCode == HttpURLConnection.HTTP_MOVED_PERM || statusCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                    ExistingCodesService.existsCodesSet.add(i);
                }

                connection.disconnect();
            } catch (IOException e) {
                log.error("Interrupted at code number " + i, e);
            }

            try {
                Thread.sleep(randomDelay.nextInt(MAX_DELAY_IN_MILLISECONDS - MIN_DELAY_IN_MILLISECONDS) + (long)MIN_DELAY_IN_MILLISECONDS);
            } catch (InterruptedException e) {
                log.error("Error while trying to make sleep delay at current thread");
                Thread.currentThread().interrupt();
            }
        }
    }
}
