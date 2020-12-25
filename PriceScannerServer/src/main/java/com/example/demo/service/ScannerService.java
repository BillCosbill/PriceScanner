package com.example.demo.service;

import com.example.demo.model.Code;
import com.example.demo.model.Shop;
import com.example.demo.repository.CodeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

@Slf4j
@Service
public class ScannerService {

    private final CodeRepository codeRepository;

    public ScannerService(CodeRepository codeRepository) {
        this.codeRepository = codeRepository;
    }

    public void getCodesFromFile(Shop shop) {
        log.info("Getting codes from file started!");
        try {
            File myObj = new File("xKomExistsCodes");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();

                if (!codeRepository.existsByCodeValue(Long.valueOf(data))) {
                    Code code = new Code();
                    code.setCodeValue(Long.valueOf(data));
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
