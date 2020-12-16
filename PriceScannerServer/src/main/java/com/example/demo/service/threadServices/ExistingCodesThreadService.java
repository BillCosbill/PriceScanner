package com.example.demo.service.threadServices;

import com.example.demo.service.ExistingCodesService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@Slf4j
public class ExistingCodesThreadService extends Thread {

    private final int startCode;
    private final int finishCode;

    private final String url;

    public ExistingCodesThreadService(int startCode, int finishCode, String url) {
        this.startCode = startCode;
        this.finishCode = finishCode;
        this.url = url;
    }

    // TODO make delays between connections, to avoid beeing blocked by website
    @Override
    public void run() {
        int statusCode;

        for (int i = startCode; i < finishCode; i++) {

            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url + i).openConnection();
                connection.setInstanceFollowRedirects(false);

                statusCode = connection.getResponseCode();

                if (statusCode == HttpURLConnection.HTTP_MOVED_PERM || statusCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                    ExistingCodesService.existsCodesSet.add(i);
                }

                connection.disconnect();
            } catch (IOException e) {
                log.error("Interrupted at code number " + i, e);
            }
        }
    }
}
