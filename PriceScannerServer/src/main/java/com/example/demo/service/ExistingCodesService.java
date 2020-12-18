package com.example.demo.service;

import com.example.demo.model.Code;
import com.example.demo.model.Shop;
import com.example.demo.repository.CodeRepository;
import com.example.demo.service.thread_services.ExistingCodesThreadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class ExistingCodesService {

    public final static int THREADS_NUMBER = 1000;

    public static Set<Integer> existsCodesSet = new HashSet<>();
    public static Set<Integer> disconnectedCodesSet = new HashSet<>();

    private final CodeRepository codeRepository;

    public ExistingCodesService(CodeRepository codeRepository) {
        this.codeRepository = codeRepository;
    }

    public void getExistingCodesFromRangeInSeries(Shop shop, int from, int to, int scansPerSeries) {
        int codesToCheck = to - from;

        log.info("Scanning in series started. Range: " + from + " - " + to);
        log.info("This may take up to " + codesToCheck * (ExistingCodesThreadService.MAX_DELAY_IN_MILLISECONDS / 1000) / THREADS_NUMBER + " seconds");
        log.info("Number of threads: " + THREADS_NUMBER);

        if (codesToCheck < scansPerSeries) {
            getExistingCodesFromRange(shop, from, to);
        } else {
            for (int i = from; i < to; i += scansPerSeries) {
                int newToValue = Math.min(i + scansPerSeries, to) - 1;

                if (newToValue == to - 1) {
                    newToValue++;
                }

                getExistingCodesFromRange(shop, i, newToValue);
            }
        }

        log.info("Scanning in series finished. Range: " + from + " - " + to);
    }

    public void getExistingCodesFromRange(Shop shop, int from, int to) {
        String url = shop.getUrlToSearchProduct();

        int codesToCheck = to - from;

        int threadsNumber = THREADS_NUMBER;

        if (codesToCheck < threadsNumber) {
            threadsNumber = codesToCheck;
        }

        Runnable[] runners = new Runnable[threadsNumber];
        Thread[] threads = new Thread[threadsNumber];

        int perThread = (int) (Math.ceil((double) codesToCheck / (threadsNumber - 1)));

        if (perThread <= 0) {
            perThread = 1;
        }

        log.info("Scanning " + url + " started! Code ranges: " + from + " - " + to);

        for (int i = 0; i < threadsNumber; i++) {
            int startCode = from + (i * perThread);
            int finishCode = from + (i * perThread) + perThread - 1;

            if (i == threadsNumber - 1 || finishCode > to) {
                finishCode = to;
            }

            if (startCode > finishCode) {
                startCode = finishCode;
            }

            runners[i] = new ExistingCodesThreadService(startCode, finishCode, url);

            if (finishCode >= to) {
                break;
            }
        }

        for (int i = 0; i < threadsNumber; i++) {
            threads[i] = new Thread(runners[i]);
        }

        for (int i = 0; i < threadsNumber; i++) {
            threads[i].start();
        }

        for (int i = 0; i < threadsNumber; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                log.error("Interrupted!", e);
                Thread.currentThread().interrupt();
            }
        }

        log.info("Scanning " + url + " finished! Code ranges: " + from + " - " + to);
        saveDisconnectedCodesToFile("disconnectedCodes_" + from + "_" + to);
//        saveExistsCodesToDatabase(shop);
    }

    private void saveExistsCodesToDatabase(Shop shop) {

        log.info("Saving existing codes to database started!");

        int newCodes = 0;

        for (Integer integer : existsCodesSet) {
            if (!codeRepository.existsByCode(integer.longValue())) {
                Code code = new Code();
                code.setCode(integer.longValue());
                code.setShop(shop);
                codeRepository.save(code);
                newCodes++;
            }
        }

        log.info("Existing codes saved to database. Found " + newCodes + " new codes");
    }

    private void saveDisconnectedCodesToFile(String fileName) {

        if (disconnectedCodesSet.isEmpty()) {
            return;
        }

        log.info("Saving disconnected codes to file started!");

        int disconnectedCodes = 0;

        FileWriter myWriter = null;
        try {
            myWriter = new FileWriter(fileName + ".txt");

            for (Integer integer : disconnectedCodesSet) {
                disconnectedCodes++;
                myWriter.write(integer.toString() + "\n");
            }

            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        disconnectedCodesSet.clear();
        log.info("Disconnected codes saved to file. Found " + disconnectedCodes + " disconnected codes");
    }

    public void saveExistsCodesToFile(String fileName) {
        List<Integer> existsCodesArray = new ArrayList<>(existsCodesSet);

        FileWriter myWriter = null;
        try {
            myWriter = new FileWriter(fileName + ".txt");

            for (Integer integer : existsCodesArray) {
                myWriter.write(integer.toString() + "\n");
            }

            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        log.info("Existing codes saved to file: " + fileName + ".txt");
    }
}
