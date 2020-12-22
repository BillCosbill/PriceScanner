package com.example.demo.service;

import com.example.demo.model.Code;
import com.example.demo.model.Shop;
import com.example.demo.model.enums.WebScraperOperation;
import com.example.demo.repository.CodeRepository;
import com.example.demo.service.thread_services.CodesThreadService;
import com.example.demo.service.thread_services.ExistingCodesThreadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.LongStream;

@Slf4j
@Service
public class CodeService {

    private final static int THREADS_NUMBER = 500;

    public static Set<Code> codesSet = new HashSet<>();

    private final CodeRepository codeRepository;

    public CodeService(CodeRepository codeRepository) {
        this.codeRepository = codeRepository;
    }

    public void getExistingCodesFromRangeInSeries(Shop shop, int from, int to, int scansPerSeries) {
        int codesToCheck = to - from;

        Set<Code> resultSet = new HashSet<>();

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

                resultSet.addAll(getExistingCodesFromRange(shop, i, newToValue));
            }
        }

        log.info("Scanning in series finished. Range: " + from + " - " + to);
    }

//    public void getExistingCodesFromRange(Shop shop, int from, int to) {
//
//        String url = shop.getUrlToSearchProduct();
//
//        int codesToCheck = to - from;
//
//        int threadsNumber = THREADS_NUMBER;
//
//        if (codesToCheck < threadsNumber) {
//            threadsNumber = codesToCheck;
//        }
//
//        Runnable[] runners = new Runnable[threadsNumber];
//        Thread[] threads = new Thread[threadsNumber];
//
//        int perThread = (int) (Math.ceil((double) codesToCheck / (threadsNumber - 1)));
//
//        if (perThread <= 0) {
//            perThread = 1;
//        }
//
//        log.info("Scanning " + url + " started! Code ranges: " + from + " - " + to);
//
//        for (int i = 0; i < threadsNumber; i++) {
//            int startCode = from + (i * perThread);
//            int finishCode = from + (i * perThread) + perThread - 1;
//
//            if (i == threadsNumber - 1 || finishCode > to) {
//                finishCode = to;
//            }
//
//            if (startCode > finishCode) {
//                startCode = finishCode;
//            }
//            long[] range = LongStream.rangeClosed(startCode, finishCode).toArray();
//            runners[i] = new CodesThreadService(shop, range, WebScraperOperation.GET_EXISTING_CODES);
//
//            if (finishCode >= to) {
//                break;
//            }
//        }
//
//        for (int i = 0; i < threadsNumber; i++) {
//            threads[i] = new Thread(runners[i]);
//        }
//
//        for (int i = 0; i < threadsNumber; i++) {
//            threads[i].start();
//        }
//
//        for (int i = 0; i < threadsNumber; i++) {
//            try {
//                threads[i].join();
//            } catch (InterruptedException e) {
//                log.error("Interrupted!", e);
//                Thread.currentThread().interrupt();
//            }
//        }
//
//        System.out.println(codesSet.size());
//        log.info("Scanning " + url + " finished! Code ranges: " + from + " - " + to);
////        saveDisconnectedCodesToFile("disconnectedCodes_" + from + "_" + to);
////        saveExistsCodesToDatabase(shop);
//    }


    public Set<Code> getExistingCodesFromRange(Shop shop, int from, int to) {
        String url = shop.getUrlToSearchProduct();

        int codesToCheck = to - from;

        int threadsNumber = THREADS_NUMBER;

        if (codesToCheck < threadsNumber) {
            threadsNumber = codesToCheck;
        }

        List<CodesThreadService> codeCallable = new ArrayList<>();
        List<Future<Set<Code>>> futures = new ArrayList<>();

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

            long[] range = LongStream.rangeClosed(startCode, finishCode)
                    .toArray();
            codeCallable.add(new CodesThreadService(shop, range, WebScraperOperation.GET_EXISTING_CODES));

            if (finishCode >= to) {
                break;
            }
        }

        // TODO https://stackoverflow.com/questions/24241335/waiting-at-sun-misc-unsafe-parknative-method  - UNSAVE park() wtf
        ExecutorService executor = Executors.newFixedThreadPool(codeCallable.size());

        for (CodesThreadService codesThreadService : codeCallable) {
            futures.add(executor.submit(codesThreadService));
        }

        Set<Code> resultSet = new HashSet<>();

        for (Future<Set<Code>> future : futures) {
            try {
                resultSet.addAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                log.error("InterruptedException OR Execution exception in index: " + future, e);
                Thread.currentThread().interrupt();
            }
        }

        executor.shutdown();

        log.info("Scanning " + url + " finished! Code ranges: " + from + " - " + to);
//        saveDisconnectedCodesToFile("disconnectedCodes_" + from + "_" + to);
//        saveExistsCodesToDatabase(shop);

        return resultSet;
    }
}
