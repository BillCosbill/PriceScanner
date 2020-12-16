package com.example.demo.service;

import com.example.demo.model.AvailableCode;
import com.example.demo.model.Code;
import com.example.demo.model.ErrorCode;
import com.example.demo.model.Shop;
import com.example.demo.repository.AvailableCodeRepository;
import com.example.demo.repository.ErrorCodeRepository;
import com.example.demo.service.threadServices.AvailableCodesThreadService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class AvailableCodesService {

    public static Set<Code> availableCodesSet = new HashSet<>();
    public static Set<Code> errorCodesSet = new HashSet<>();

    private final ErrorCodeRepository errorCodeRepository;
    private final AvailableCodeRepository availableCodeRepository;

    public AvailableCodesService(ErrorCodeRepository errorCodeRepository, AvailableCodeRepository availableCodeRepository) {
        this.errorCodeRepository = errorCodeRepository;
        this.availableCodeRepository = availableCodeRepository;
    }

    public void findAvailableProductCodes(Shop shop) {

        int threadsNumber = Thread.activeCount();
        Runnable[] runners = new Runnable[threadsNumber];
        Thread[] threads = new Thread[threadsNumber];

        int countCodes = shop.getProductCodes().size();

        if (countCodes <= 0) {
            return;
        }

        int perThread = countCodes / threadsNumber;

        log.info("Started searching for available codes");

        for (int i = 0; i < threadsNumber; i++) {
            int startCode = (i * perThread);
            int finishCode = (i * perThread) + perThread + 1;

            if (i == threadsNumber - 1) {
                finishCode = countCodes;
            }

            runners[i] = new AvailableCodesThreadService(startCode, finishCode, shop);
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

        log.info("Finished searching for available codes");
        setAvailableCodesInDatabase();
    }

    private void setAvailableCodesInDatabase() {
        for (Code code : errorCodesSet) {
            ErrorCode errorCode = new ErrorCode();
            errorCode.setCode(code.getCode());
            errorCode.setShop(code.getShop());

            if (!errorCodeRepository.existsByCode(errorCode.getCode())) {
                errorCodeRepository.save(errorCode);
            }
        }

        for (Code code : availableCodesSet) {
            AvailableCode availableCode = new AvailableCode();
            availableCode.setCode(code.getCode());
            availableCode.setShop(code.getShop());

            if (!errorCodeRepository.existsByCode(code.getCode())) {
                availableCodeRepository.save(availableCode);
            }
        }


        log.info("Detected " + errorCodesSet.size() + " error codes");
        log.info("Available codes saved to database. Found " + availableCodesSet.size() + " new available codes");
    }

    public void checkErorrCodes(Shop shop) {
        log.info("Started checking error codes");

        for (ErrorCode errorCode : shop.getErrorCodes()) {

            String fullUrl = shop.getUrlToSearchProduct() + errorCode.getCode();

            try {
                Document doc = Jsoup.connect(fullUrl)
                                    .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36")
                                    .timeout(60 * 1000).get();

                if (doc.getElementsByClass("lw463u-5").first() != null && doc.getElementsByClass("lw463u-5").first()
                                                                             .text().equals("Przepraszamy")) {
                    log.error("Product with code: " + errorCode.getCode() + " not found");
                } else {
                    if (doc.getElementsByClass("sc-1jultii-1").first() == null || fullUrl.equals(doc.location())) {
                        log.error("Something went wrong checking product: " + fullUrl);
                        if (!errorCodeRepository.existsByCode(errorCode.getCode())) {
                            errorCodeRepository.save(errorCode);
                        }
                    } else {
                        String cannotBeBought = doc.getElementsByClass("sc-1jultii-1").first().text();

                        if (!cannotBeBought.equals("Wycofany")) {
                            if (!availableCodeRepository.existsByCode(errorCode.getCode())) {
                                AvailableCode code = new AvailableCode();
                                code.setCode(errorCode.getCode());
                                code.setShop(errorCode.getShop());
                                availableCodeRepository.save(code);
                                errorCodeRepository.delete(errorCode);
                                log.info("Repaired code: " + errorCode.getCode());
                            }
                        } else {
                            if (errorCodeRepository.existsByCode(errorCode.getCode())) {
                                errorCodeRepository.delete(errorCode);
                                log.info("Repaired code: " + errorCode.getCode());
                            }
                        }
                    }
                }
            } catch (IOException e) {
                log.error("IOException at code number " + errorCode.getCode(), e);
                if (!errorCodeRepository.existsByCode(errorCode.getCode())) {
                    errorCodeRepository.save(errorCode);
                }
            }
        }
        log.info("Finished checking error codes. Error codes left: " + errorCodeRepository.findAll().size());
    }
}