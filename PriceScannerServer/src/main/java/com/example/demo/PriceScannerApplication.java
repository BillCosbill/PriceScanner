package com.example.demo;

import com.example.demo.model.AvailableCode;
import com.example.demo.model.Shop;
import com.example.demo.repository.CodeRepository;
import com.example.demo.repository.ShopRepository;
import com.example.demo.service.AvailableCodesService;
import com.example.demo.service.ExistingCodesService;
import com.example.demo.service.ScannerService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.util.Optional;

@SpringBootApplication
public class PriceScannerApplication {

    private final ScannerService scannerService;
    private final AvailableCodesService availableCodesService;
    private final ExistingCodesService existingCodesService;
    private final ShopRepository shopRepository;
    private final CodeRepository codeRepository;

    public PriceScannerApplication(ScannerService scannerService, AvailableCodesService availableCodesService, ExistingCodesService existingCodesService, ShopRepository shopRepository, CodeRepository codeRepository) {
        this.scannerService = scannerService;
        this.availableCodesService = availableCodesService;
        this.existingCodesService = existingCodesService;
        this.shopRepository = shopRepository;
        this.codeRepository = codeRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(PriceScannerApplication.class, args);
    }


    @EventListener(ApplicationReadyEvent.class)
    public void fillDB() {

//        availableCodesService.saveAvailableCodesToFile("availableXKomCodes");
//        availableCodesService.saveErrorCodesToFile("errorXKomCodes");

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
//        scannerService.getCodesFromFile(shop);
//
//        shopOpt = shopRepository.findByUrl(shopNew.getUrl());
//
//        shop = null;
//        if (shopOpt.isPresent()) {
//            shop = shopOpt.get();
//        }
//
//
        if (shop != null) {
            existingCodesService.getExistingCodesFromRangeInSeries(shop, 0, 5000, 1000);
        }
//
//        shopOpt = shopRepository.findByUrlAndFetchCodes(shopNew.getUrl());
//
//        if (shopOpt.isPresent()) {
//            shop = shopOpt.get();
//        }
//
//        if (shop != null) {
//            availableCodesService.findAvailableProductCodes(shop);
//        }

//        shopOpt = shopRepository.findByUrlAndFetchErrorCodes(shopNew.getUrl());
//
//        if (shopOpt.isPresent()) {
//            shop = shopOpt.get();
//        }
//
//        if (shop != null) {
//            availableCodesService.checkErorrCodes(shop);
//        }
    }

}
