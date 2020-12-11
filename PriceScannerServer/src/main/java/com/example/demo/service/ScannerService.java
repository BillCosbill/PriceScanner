package com.example.demo.service;

import com.example.demo.model.Product;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ScannerService {

    List<Product> productList = new ArrayList<>();
    public static Set<Integer> existsCodes = new HashSet<>();

    public void getExistingCodes() {
        int toTest = 1000;

        int countThreads = Thread.activeCount();
        Runnable[] runners = new Runnable[countThreads];
        Thread[] threads = new Thread[countThreads];

        int perThread = toTest / countThreads;

        for(int i=0; i<countThreads; i++) {
            runners[i] = new ProductCodeService((i * perThread) + 1, (i * perThread) + perThread, "https://www.x-kom.pl/p/");
        }

        for(int i=0; i<countThreads; i++) {
            threads[i] = new Thread(runners[i]);
        }

        for(int i=0; i<countThreads; i++) {
            threads[i].start();
        }

        for(int i=0; i<countThreads; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        Object[] existsCodesArray = existsCodes.toArray();

        FileWriter myWriter = null;
        try {
            myWriter = new FileWriter("xKomExistsCodes.txt");

            for(int i=0; i<existsCodesArray.length; i++) {
                myWriter.write(existsCodesArray[i].toString() + "\n");
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
