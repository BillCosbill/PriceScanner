package com.example.demo.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ProductCodeService extends Thread {

    private int startCode;
    private int finishCode;

    private String url;

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

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
