package com.example.demo.service;

import com.example.demo.model.Product;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
public class ScannerService {

    List<Product> productList = new ArrayList<>();
    public static Set<Integer> existsCodesSet = new HashSet<>();

    public void getExistingCodes(String url, int from, int to) {
        int codesToCheck = to - from;

        int threadsNumber = Thread.activeCount();
        Runnable[] runners = new Runnable[threadsNumber];
        Thread[] threads = new Thread[threadsNumber];

        int perThread = codesToCheck / threadsNumber;

        for (int i = 0; i < threadsNumber; i++) {
            int startCode = from + (i * perThread) + 1;
            int finishCode = from + (i * perThread) + perThread;
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
//                log.error("Interrupted!", e);
//                Thread.currentThread().interrupt();
            }
        }

        saveExistsCodesToFile();
    }

    @SneakyThrows
    private void saveExistsCodesToFile() {
        List<Integer> existsCodesArray = new ArrayList<>(existsCodesSet);
        Collections.sort(existsCodesArray);

        FileWriter myWriter = null;
        try {
            myWriter = new FileWriter("xKomExistsCodes.txt");

            for (Integer integer : existsCodesArray) {
                myWriter.write(integer.toString() + "\n");
            }

            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getData() {
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
    }

}
