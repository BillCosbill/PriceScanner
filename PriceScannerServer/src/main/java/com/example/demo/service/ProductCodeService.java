package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@Slf4j
public class ProductCodeService extends Thread {

    private final int startCode;
    private final int finishCode;

    private final String url;

    public ProductCodeService(int startCode, int finishCode, String url) {
        this.startCode = startCode;
        this.finishCode = finishCode;
        this.url = url;
    }

    @Override
    public void run() {
        int code;

        for (int i = startCode; i < finishCode; i++) {

            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url + i).openConnection();
                connection.setInstanceFollowRedirects(false);

                code = connection.getResponseCode();

                if (code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_MOVED_TEMP) {
                    ScannerService.existsCodesSet.add(i);
                }

                connection.disconnect();
            } catch (IOException e) {
                log.error("Interrupted at code number " + i, e);
            }
        }
    }
}
