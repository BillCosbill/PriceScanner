package com.example.demo.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ProductCodeService implements Runnable {

    private int start;
    private int end;

    private String url;

    public ProductCodeService(int startCode, int finishCode, String url) {
        this.start = startCode;
        this.end = finishCode;
        this.url = url;
    }

    @Override
    public void run() {
        String redirectUrl  = "";
        int code;

        for(int i=start; i<end; i++) {
            redirectUrl = url + i;

            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url + i).openConnection();
                connection.setInstanceFollowRedirects(false);

                code = connection.getResponseCode();

                if (code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_MOVED_TEMP) {
                    redirectUrl = connection.getHeaderField("Location");
                }

                if (!redirectUrl.equals(url + i)) {
                    ScannerService.existsCodes.add(i);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
