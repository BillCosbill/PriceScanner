package com.example.demo;

import com.example.demo.model.Code;
import com.example.demo.model.Shop;
import com.example.demo.repository.CodeRepository;
import com.example.demo.repository.ShopRepository;
import com.example.demo.service.ScannerService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.util.List;
import java.util.Optional;

@SpringBootApplication
public class PriceScannerApplication {

    private final ScannerService scannerService;
    private final ShopRepository shopRepository;
    private final CodeRepository codeRepository;

    public PriceScannerApplication(ScannerService scannerService, ShopRepository shopRepository, CodeRepository codeRepository) {
        this.scannerService = scannerService;
        this.shopRepository = shopRepository;
        this.codeRepository = codeRepository;
    }


    public static void main(String[] args) {
        SpringApplication.run(PriceScannerApplication.class, args);
    }


    @EventListener(ApplicationReadyEvent.class)
    public void fillDB() {

        Shop shopNew = new Shop();
        shopNew.setUrl("https://www.x-kom.pl/");
        shopNew.setName("x-kom");
        shopNew.setUrlToSearchProduct("https://www.x-kom.pl/p/");

        if (!shopRepository.existsByUrl(shopNew.getUrl())) {
            shopRepository.save(shopNew);
        }

        Optional<Shop> shopOpt = shopRepository.findByUrl(shopNew.getUrl());
        Shop shop = null;
        if (shopOpt.isPresent()) {
            shop = shopOpt.get();
        }


//        scannerService.getCodesFromFile(shop);
//
//        shopOpt = shopRepository.findByUrl(shopNew.getUrl());
//
//        shop = null;
//        if (shopOpt.isPresent()) {
//            shop = shopOpt.get();
//        }
//

        if (shop != null) {
            scannerService.getExistingCodes(shop, 485000, 490000);
        }

        shopOpt = shopRepository.findByUrlAndFetchCodes(shopNew.getUrl());

        if (shopOpt.isPresent()) {
            shop = shopOpt.get();
        }

        if (shop != null) {
            scannerService.findEnableProductCodes(shop);
        }
    }

}
