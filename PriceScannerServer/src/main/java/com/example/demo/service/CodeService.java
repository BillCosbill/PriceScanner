package com.example.demo.service;

import com.example.demo.model.Code;
import com.example.demo.model.CodeStatus;
import com.example.demo.model.Shop;
import com.example.demo.model.dto.CodeDTO;
import com.example.demo.model.enums.ECodeStatus;
import com.example.demo.model.enums.EWebScraperOperation;
import com.example.demo.repository.CodeRepository;
import com.example.demo.repository.CodeStatusRepository;
import com.example.demo.repository.ShopRepository;
import com.example.demo.service.thread_services.CodesThreadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@Slf4j
@Service
public class CodeService {

    private final static int THREADS_NUMBER = 500;

    public static Set<Code> codesSet = new HashSet<>();

    private final CodeRepository codeRepository;
    private final ShopRepository shopRepository;
    private final CodeStatusRepository codeStatusRepository;

    public CodeService(CodeRepository codeRepository, ShopRepository shopRepository, CodeStatusRepository codeStatusRepository) {
        this.codeRepository = codeRepository;
        this.shopRepository = shopRepository;
        this.codeStatusRepository = codeStatusRepository;
    }

    public void getExistingCodesFromRangeInSeries(Shop shop, int from, int to, int scansPerSeries) {
        int codesToCheck = to - from;

        Set<CodeDTO> resultSet = new HashSet<>();

        log.info("Scanning in series started. Range: " + from + " - " + to);
        log.info("This may take up to " + codesToCheck * (CodesThreadService.MAX_DELAY_IN_MILLISECONDS / 1000) / THREADS_NUMBER + " seconds");
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

    public Set<CodeDTO> getExistingCodesFromRange(Shop shop, int from, int to) {
        String url = shop.getUrlToSearchProduct();

        int codesToCheck = to - from;

        int threadsNumber = THREADS_NUMBER;

        if (codesToCheck < threadsNumber) {
            threadsNumber = codesToCheck;
        }

        List<CodesThreadService> codeCallable = new ArrayList<>();
        List<Future<Set<CodeDTO>>> futures = new ArrayList<>();

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
            codeCallable.add(new CodesThreadService(shop, range, EWebScraperOperation.GET_EXISTING_CODES));

            if (finishCode >= to) {
                break;
            }
        }

        // TODO https://stackoverflow.com/questions/24241335/waiting-at-sun-misc-unsafe-parknative-method  - UNSAVE park() wtf
        ExecutorService executor = Executors.newFixedThreadPool(codeCallable.size());

        for (CodesThreadService codesThreadService : codeCallable) {
            futures.add(executor.submit(codesThreadService));
        }

        Set<CodeDTO> resultSet = new HashSet<>();

        for (Future<Set<CodeDTO>> future : futures) {
            try {
                resultSet.addAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                log.error("InterruptedException OR Execution exception in index: " + future, e);
                Thread.currentThread().interrupt();
            }
        }

        executor.shutdown();

        log.info("Scanning " + url + " finished! Code ranges: " + from + " - " + to);


//        saveCodesToFiles(resultSet);


//        saveCodesToDatabase(shop, resultSet);

        return resultSet;
    }

    private void saveCodesToDatabase(Shop shop, Set<CodeDTO> codesSet) {

        log.info("Saving existing codes to database started!");

        int newCodes = 0;

        for (CodeDTO codeDTO : codesSet) {
            if (!codeRepository.existsByCodeValue(codeDTO.getCodeValue())) {
                Code newCode = new Code();
                newCode.setCodeValue(codeDTO.getCodeValue());
                newCode.setShop(shop);
                // TODO .orElseThrow -> dodać własny wyjątek
                Optional<CodeStatus> codeStatus = codeStatusRepository.findByStatus(ECodeStatus.valueOf(codeDTO.getCodeStatus()));
                newCode.setCodeStatus(codeStatus.get());

                codeRepository.save(newCode);
                newCodes++;
            }
        }

        log.info("Existing codes saved to database. Found " + newCodes + " new codes");
    }

    private void saveCodesToFiles(Set<CodeDTO> codesSet) {

        if (codesSet.isEmpty()) {
            return;
        }

        log.info("Saving codes to file started!");

        Stream.of(ECodeStatus.values()).forEach(ECodeStatus -> {
            Set<CodeDTO> currentSet = codesSet.stream().filter(code -> code.getCodeStatus().equals(ECodeStatus.toString())).collect(Collectors.toSet());
            if (!currentSet.isEmpty()) {
                saveToFile(ECodeStatus.toString(), currentSet);
            }
        });

        log.info("Codes saved to file");
    }

    private void saveToFile(String fileName, Set<CodeDTO> codesSet) {
        log.info("Starting saving to file: " + fileName);

        FileWriter myWriter = null;
        try {
            myWriter = new FileWriter(fileName + ".txt");

            for (CodeDTO code : codesSet) {
                myWriter.write(code.getCodeValue() + "\n");
            }

            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
