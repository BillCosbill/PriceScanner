package com.example.demo.service.threadServices;

import com.example.demo.model.Code;
import com.example.demo.model.Shop;
import com.example.demo.service.AvailableCodesService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

@Slf4j
public class AvailableCodesThreadService extends Thread {

    private Shop shop;

    private int startCode;
    private int finishCode;

    public AvailableCodesThreadService(int startCode, int finishCode, Shop shop) {
        this.startCode = startCode;
        this.finishCode = finishCode;
        this.shop = shop;
    }

    // TODO make delays between connections, to avoid beeing blocked by website
    @Override
    public void run() {

        for (int i = startCode; i < finishCode; i++) {

            Code code = shop.getProductCodes().get(i);
            String fullUrl = shop.getUrlToSearchProduct() + code.getCode();

            try {
                // TODO check your useragent: https://gs.statcounter.com/detect
                Document doc = Jsoup.connect(fullUrl)
                                    // TODO set other request headers: https://httpbin.org/anything
                                    // TODO keep changing useragent to avoid beeing blocked https://developers.whatismybrowser.com/useragents/explore/operating_system_name/windows/
                                    .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36")
                                    // TODO set referer, fe. google: https://www.scraperapi.com/blog/5-tips-for-web-scraping/
                                    .timeout(60 * 1000).get();

                if (doc.getElementsByClass("lw463u-5").first() != null && doc.getElementsByClass("lw463u-5").first()
                                                                             .text().equals("Przepraszamy")) {
                    log.error("Product with code: " + code.getCode() + " not found");
                } else {
                    if (doc.getElementsByClass("sc-1jultii-1").first() == null || fullUrl.equals(doc.location())) {
                        log.error("Something went wrong checking product: " + fullUrl);
                        AvailableCodesService.errorCodesSet.add(code);
                    } else {
                        String cannotBeBought = doc.getElementsByClass("sc-1jultii-1").first().text();

                        if (!cannotBeBought.equals("Wycofany")) {
                            AvailableCodesService.availableCodesSet.add(code);
//                            log.info("Available code found: " + code.getCode());
                        }
                    }
                }
            } catch (IOException e) {
                log.error("IOException at code number " + code.getCode(), e);
                AvailableCodesService.errorCodesSet.add(code);
            }
        }

    }
}
