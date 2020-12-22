package com.example.demo;

import com.example.demo.model.Shop;
import com.example.demo.repository.CodeRepository;
import com.example.demo.repository.ShopRepository;
import com.example.demo.service.AvailableCodesService;
import com.example.demo.service.CodeService;
import com.example.demo.service.ExistingCodesService;
import com.example.demo.service.ScannerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.util.Optional;

@SpringBootApplication
@Slf4j
public class PriceScannerApplication {

    private final ScannerService scannerService;
    private final AvailableCodesService availableCodesService;
    private final ExistingCodesService existingCodesService;
    private final ShopRepository shopRepository;
    private final CodeRepository codeRepository;
    private final CodeService codeService;

    public PriceScannerApplication(ScannerService scannerService, AvailableCodesService availableCodesService, ExistingCodesService existingCodesService, ShopRepository shopRepository, CodeRepository codeRepository, CodeService codeService) {
        this.scannerService = scannerService;
        this.availableCodesService = availableCodesService;
        this.existingCodesService = existingCodesService;
        this.shopRepository = shopRepository;
        this.codeRepository = codeRepository;
        this.codeService = codeService;
    }

    public static void main(String[] args) {
        SpringApplication.run(PriceScannerApplication.class, args);
    }


    @EventListener(ApplicationReadyEvent.class)
    public void fillDB() {

//        int threadsNumber = 500;
//
//        CodesThreadService[] callables = new CodesThreadService[threadsNumber];
//        List<Future<List<Code>>> futures = new ArrayList<>();
//
//        ExecutorService executor = Executors.newFixedThreadPool(threadsNumber);
//
//        for (int i = 0; i < threadsNumber; i++) {
//            long[] range = LongStream.rangeClosed(1, 10).toArray();
//            callables[i] = new CodesThreadService(null, range, WebScraperOperation.GET_EXISTING_CODES);
//        }
//
//
//        for (int i = 0; i < threadsNumber; i++) {
//            futures.add(executor.submit(callables[i]));
//        }
//
//        for (Future<List<Code>> future : futures) {
//            try {
//                future.get();
//            } catch (InterruptedException | ExecutionException e) {
//                log.error("InterruptedException OR Execution exception in index: " + future, e);
//                Thread.currentThread()
//                        .interrupt();
//            }
//        }
//
//        executor.shutdown();








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

        codeService.getExistingCodesFromRangeInSeries(shopNew, 0, 10000, 1000);

        System.out.println("KOOOOOOOOOOOOOOOONIIIIIIIIIIIIIIIIIIEEEEEEEEEEEEECC");














//        availableCodesService.saveAvailableCodesToFile("availableXKomCodes");
//        availableCodesService.saveErrorCodesToFile("errorXKomCodes");

//        Shop shopNew = new Shop();
//        shopNew.setUrl("https://www.x-kom.pl/");
//        shopNew.setName("x-kom");
//        shopNew.setUrlToSearchProduct("https://www.x-kom.pl/p/");
//
//        if (!shopRepository.existsByUrl(shopNew.getUrl())) {
//            shopRepository.save(shopNew);
//        }
//
//        Optional<Shop> shopOpt = shopRepository.findByUrl(shopNew.getUrl());
//        Shop shop = null;
//        if (shopOpt.isPresent()) {
//            shop = shopOpt.get();
//        }

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


//        if (shop != null) {
//            existingCodesService.getExistingCodesFromRangeInSeries(shop, 0, 10000, 1000);
//        }

//        shopOpt = shopRepository.findByUrlAndFetchCodes(shopNew.getUrl());
//        if (shopOpt.isPresent()) {
//            shop = shopOpt.get();
//        }

//        shopOpt = shopRepository.findByUrlAndFetchErrorCodes(shopNew.getUrl());
//        if (shopOpt.isPresent()) {
//            shop = shopOpt.get();
//        }
//
//        if (shop != null) {
//            availableCodesService.checkErorrCodes(shop);
//        }


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
