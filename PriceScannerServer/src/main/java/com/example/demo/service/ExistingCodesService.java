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

    public static Set<Integer> existsCodesSet = new HashSet<>();

    private final CodeRepository codeRepository;

    public ExistingCodesService(CodeRepository codeRepository) {
        this.codeRepository = codeRepository;
    }

    public void getExistingCodesFromRangeInSeries(Shop shop, int from, int to, int scansPerSeries) {
        int codesToCheck = to - from;

        log.info("Scanning in series started. Range: " + from + " - " + to);
        log.info("This may take up to " + codesToCheck * (ExistingCodesThreadService.MAX_DELAY_IN_MILLISECONDS / 1000) / Thread.activeCount() + " seconds");

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

        int threadsNumber = Thread.activeCount();
        Runnable[] runners = new Runnable[threadsNumber];
        Thread[] threads = new Thread[threadsNumber];

        int perThread = codesToCheck / threadsNumber;

        log.info("Scanning " + url + " started! Code ranges: " + from + " - " + to);

        for (int i = 0; i < threadsNumber; i++) {
            int startCode = from + (i * perThread);
            int finishCode = from + (i * perThread) + perThread - 1;

            if (i == threadsNumber - 1) {
                finishCode = to;
            }

            runners[i] = new ExistingCodesThreadService(startCode, finishCode, url);
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
