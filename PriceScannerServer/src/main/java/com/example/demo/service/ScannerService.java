package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.AvailableCodeRepository;
import com.example.demo.repository.CodeRepository;
import com.example.demo.repository.ErrorCodeRepository;
import com.example.demo.repository.ShopRepository;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
public class ScannerService {

    private final CodeRepository codeRepository;
    private final ShopRepository shopRepository;
    private final ErrorCodeRepository errorCodeRepository;
    private final AvailableCodeRepository availableCodeRepository;

    List<Product> productList = new ArrayList<>();
    public static Set<Integer> existsCodesSet = new HashSet<>();
    public static Set<Code> availableCodesSet = new HashSet<>();
    public static Set<Code> errorCodesSet = new HashSet<>();

    public ScannerService(CodeRepository codeRepository, ShopRepository shopRepository, ErrorCodeRepository errorCodeRepository, AvailableCodeRepository availableCodeRepository) {
        this.codeRepository = codeRepository;
        this.shopRepository = shopRepository;
        this.errorCodeRepository = errorCodeRepository;
        this.availableCodeRepository = availableCodeRepository;
    }

    //TODO nie działa kiedy jest za mało codesToCheck
    public void getExistingCodes(Shop shop, int from, int to) {
        String url = shop.getUrlToSearchProduct();
        int codesToCheck = to - from;

        int threadsNumber = Thread.activeCount();
        Runnable[] runners = new Runnable[threadsNumber];
        Thread[] threads = new Thread[threadsNumber];

        int perThread = codesToCheck / threadsNumber;

        log.info("Scanning " + url + " started!");

        for (int i = 0; i < threadsNumber; i++) {
            int startCode = from + (i * perThread);
            int finishCode = from + (i * perThread) + perThread + 1;

            if (i == threadsNumber - 1) {
                finishCode = to;
            }

            runners[i] = new ProductCodeService(startCode, finishCode, url);
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

        log.info("Scanning " + url + " finished!");
        saveExistsCodesToDatabase(shop);
    }

    public void getCodesFromFile(Shop shop) {
        log.info("Getting codes from file started!");
        try {
            File myObj = new File("xKomExistsCodes");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();

                if (!codeRepository.existsByCode(Long.valueOf(data))) {
                    Code code = new Code();
                    code.setCode(Long.valueOf(data));
                    code.setShop(shop);
                    codeRepository.save(code);
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        log.info("Getting codes from file finished.");
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

    public void findAvailableProductCodes(Shop shop) {

        int threadsNumber = Thread.activeCount();
        Runnable[] runners = new Runnable[threadsNumber];
        Thread[] threads = new Thread[threadsNumber];

        int countCodes = shop.getProductCodes().size();

        if (countCodes <= 0 ) {
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

            runners[i] = new AvailableCodesScannerService(startCode, finishCode, shop);
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

    public void checkErorrCodes(Shop shop) {
        log.info("Started checking error codes");

        for (ErrorCode errorCode: shop.getErrorCodes()) {

            String fullUrl = shop.getUrlToSearchProduct() + errorCode.getCode();

            try {
                Document doc = Jsoup.connect(fullUrl).userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36").timeout(60 * 1000).get();

                if(doc.getElementsByClass("lw463u-5").first() != null && doc.getElementsByClass("lw463u-5").first().text().equals("Przepraszamy")){
                    log.error("Product with code: " + errorCode.getCode() + " not found");
                } else {
                    if (doc.getElementsByClass("sc-1jultii-1").first() == null || fullUrl.equals(doc.location())) {
                        log.error("Something went wrong checking product: " + fullUrl);
                        if(!errorCodeRepository.existsByCode(errorCode.getCode())){
                            errorCodeRepository.save(errorCode);
                        }
                    } else {
                        String cannotBeBought = doc.getElementsByClass("sc-1jultii-1").first().text();

                        if(!cannotBeBought.equals("Wycofany")) {
                            if(!availableCodeRepository.existsByCode(errorCode.getCode())){
                                AvailableCode code = new AvailableCode();
                                code.setCode(errorCode.getCode());
                                code.setShop(errorCode.getShop());
                                availableCodeRepository.save(code);
                                errorCodeRepository.delete(errorCode);
                                log.info("Repaired code: " + errorCode.getCode());
                            }
                        } else {
                            if(errorCodeRepository.existsByCode(errorCode.getCode())){
                                errorCodeRepository.delete(errorCode);
                                log.info("Repaired code: " + errorCode.getCode());
                            }
                        }
                    }
                }
            } catch (IOException e) {
                log.error("IOException at code number " + errorCode.getCode(), e);
                if(!errorCodeRepository.existsByCode(errorCode.getCode())){
                    errorCodeRepository.save(errorCode);
                }
            }
        }
        log.info("Finished checking error codes. Error codes left: " + errorCodeRepository.findAll().size());
    }


    private void setAvailableCodesInDatabase() {
        for (Code code : errorCodesSet) {
            ErrorCode errorCode = new ErrorCode();
            errorCode.setCode(code.getCode());
            errorCode.setShop(code.getShop());

            if(!errorCodeRepository.existsByCode(errorCode.getCode())){
                errorCodeRepository.save(errorCode);
            }
        }

        for (Code code : availableCodesSet) {
            AvailableCode availableCode = new AvailableCode();
            availableCode.setCode(code.getCode());
            availableCode.setShop(code.getShop());

            if(!errorCodeRepository.existsByCode(code.getCode())){
                availableCodeRepository.save(availableCode);
            }
        }


        log.info("Detected " + errorCodesSet.size() + " error codes");
        log.info("Available codes saved to database. Found " + availableCodesSet.size() + " new available codes");
    }

    private void saveExistsCodesToFile(String fileName) {
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

//    public void getData() {
//        for (int i=0; i<100; i++) {
//
//            try {
//                System.out.println(productCode);
//                String fullUrl = url + productCode;
//
//                Document doc = Jsoup.connect(fullUrl).get();
//
//                if(!fullUrl.equals(doc.location())){
//                    String name = doc.getElementsByClass("sc-1x6crnh-5").first().text();
//                    String price = doc.getElementsByClass("u7xnnm-4 iVazGO").first().text();
//                    String code = String.valueOf(productCode);
//
//                    productList.add(new Product(fullUrl, name, price, code));
//                }
//
//            } catch (IOException e) {
//                System.out.println("OPS");
//            }
//
//            productCode++;
//        }
//
//        System.out.println(productList.size());
//    }

}
