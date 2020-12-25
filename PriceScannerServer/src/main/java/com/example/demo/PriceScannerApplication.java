package com.example.demo;

import com.example.demo.model.CodeStatus;
import com.example.demo.model.Shop;
import com.example.demo.model.enums.ECodeStatus;
import com.example.demo.repository.CodeStatusRepository;
import com.example.demo.repository.ShopRepository;
import com.example.demo.service.CodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.util.Optional;

@SpringBootApplication
@Slf4j
public class PriceScannerApplication {

    private final ShopRepository shopRepository;
    private final CodeService codeService;
    private final CodeStatusRepository codeStatusRepository;

    public PriceScannerApplication(ShopRepository shopRepository, CodeService codeService, CodeStatusRepository codeStatusRepository) {
        this.shopRepository = shopRepository;
        this.codeService = codeService;
        this.codeStatusRepository = codeStatusRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(PriceScannerApplication.class, args);
    }


    @EventListener(ApplicationReadyEvent.class)
    public void fillDB() {

        CodeStatus a = new CodeStatus(ECodeStatus.UNKNOWN);
        CodeStatus b = new CodeStatus(ECodeStatus.REDIRECTING);
        CodeStatus c = new CodeStatus(ECodeStatus.NOT_FOUND);
        CodeStatus d = new CodeStatus(ECodeStatus.WITHDRAWN);
        CodeStatus e = new CodeStatus(ECodeStatus.AVAILABLE);

        if (!codeStatusRepository.existsByStatus(a.getStatus())) {
            codeStatusRepository.save(a);
        }
        if (!codeStatusRepository.existsByStatus(b.getStatus())) {
            codeStatusRepository.save(b);
        }
        if (!codeStatusRepository.existsByStatus(c.getStatus())) {
            codeStatusRepository.save(c);
        }
        if (!codeStatusRepository.existsByStatus(d.getStatus())) {
            codeStatusRepository.save(d);
        }
        if (!codeStatusRepository.existsByStatus(e.getStatus())) {
            codeStatusRepository.save(e);
        }

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

        codeService.getExistingCodesFromRangeInSeries(shop, 0, 10000, 1000);
    }
}
