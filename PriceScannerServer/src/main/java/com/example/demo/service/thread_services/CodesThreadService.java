package com.example.demo.service.thread_services;

import com.example.demo.model.Code;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

// TODO try to do global thread service
// TODO use Callable and Future to return resultCodeSet with found codes

@Slf4j
public class CodesThreadService implements Runnable {
    public static final int MIN_DELAY_IN_MILLISECONDS = 10000;
    public static final int MAX_DELAY_IN_MILLISECONDS = 15000;

    private final int[] codesArray;
//    private final String url;
    private final Random randomDelay;

    private volatile Set<Integer> resultCodeSet;

    public CodesThreadService(int[] codesArray) {
        this.codesArray = codesArray;
//        this.url = url;
        this.resultCodeSet = new HashSet<>();
        this.randomDelay = new Random();
    }

    @Override
    public void run() {
        int statusCode;

//        randomDelay();

        for (int i=0; i < codesArray.length; i++) {
            resultCodeSet.add(codesArray[i]);
        }

//        try {
//
//        } catch (IOException e) {
//
//        }


    }

    private void randomDelay() {
        try {
            Thread.sleep(randomDelay.nextInt(MAX_DELAY_IN_MILLISECONDS - MIN_DELAY_IN_MILLISECONDS) + (long)MIN_DELAY_IN_MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("Error while trying to make sleep delay at current thread");
            Thread.currentThread().interrupt();
        }
    }

    public Set<Integer> getResultCodeSet() {
        return resultCodeSet;
    }
}
