package com.example.demo.service;

import com.example.demo.model.Code;
import com.example.demo.model.Product;
import com.example.demo.model.Shop;
import com.example.demo.repository.CodeRepository;
import com.example.demo.repository.ShopRepository;
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
public class ScannerService {

    private final CodeRepository codeRepository;
    private final ShopRepository shopRepository;

    List<Product> productList = new ArrayList<>();
    public static Set<Integer> existsCodesSet = new HashSet<>();
    public static Set<Code> availableCodesSet = new HashSet<>();

    public ScannerService(CodeRepository codeRepository, ShopRepository shopRepository) {
        this.codeRepository = codeRepository;
        this.shopRepository = shopRepository;
    }

    //TODO nie działa kiedy jest za mało codesToCheck
    public void getExistingCodes(Shop shop, int from, int to) {
        String url = shop.getUrlToSearchProduct();
        int codesToCheck = to - from;

        int threadsNumber = Thread.activeCount();
        Runnable[] runners = new Runnable[threadsNumber];
        Thread[] threads = new Thread[threadsNumber];

        int perThread = codesToCheck / threadsNumber;

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

    private void saveExistsCodesToDatabase(Shop shop) {
        List<Integer> existsCodesArray = new ArrayList<>(existsCodesSet);

        int newCodes = 0;

        for (Integer integer : existsCodesArray) {
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

    public void findEnableProductCodes(Shop shop) {

        int threadsNumber = Thread.activeCount();
        Runnable[] runners = new Runnable[threadsNumber];
        Thread[] threads = new Thread[threadsNumber];

        int countCodes = shop.getProductCodes().size();

        if (countCodes <= 0 ) {
            return;
        }

        int perThread = countCodes / threadsNumber;


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

        setAvailableCodesInDatabase();
    }


    private void setAvailableCodesInDatabase() {
        List<Code> availableCodesArray = new ArrayList<>(availableCodesSet);

        int newAvailableCodes = 0;

        for (Code code : availableCodesArray) {
            code.setEnable(true);
            codeRepository.save(code);
            newAvailableCodes++;
        }

        log.info("Available codes saved to database. Found " + newAvailableCodes + " new available codes");
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
