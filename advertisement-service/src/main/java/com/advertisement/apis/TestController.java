package com.advertisement.apis;

import com.advertisement.dtos.Ads;
import com.advertisement.service.ScrapService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/test")
@CrossOrigin(origins = "*")
public class TestController {


    private static Map<String, Long> impressionMap = new HashMap<>();
    private static Map<String, Long> clickMap = new HashMap<>();

    @Autowired
    private ScrapService scrapService;

    @GetMapping
    public ResponseEntity<Ads> getAds(HttpServletRequest httpServletRequest) throws IOException {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.OK).body(scrapService.scrap(httpServletRequest.getParameter("htmlURL")));
    }

    @GetMapping(value = "/impression")
    public ResponseEntity<String> impression(@RequestParam("adId") String adId,  HttpServletRequest httpServletRequest) {
        if(impressionMap.get(adId) == null) {
            impressionMap.put(adId, 0L);
        }
        impressionMap.put(adId, impressionMap.get(adId) + 1);
        return ResponseEntity.status(HttpStatus.OK).body("Received");
    }

    @GetMapping(value = "/click")
    public ResponseEntity<String> click(@RequestParam("adId") String adId,  HttpServletRequest httpServletRequest) {
        if(clickMap.get(adId) == null) {
            clickMap.put(adId, 0L);
        }
        clickMap.put(adId, clickMap.get(adId) + 1);
        return ResponseEntity.status(HttpStatus.OK).body("Received");
    }

    @GetMapping(value="/report")
    public ResponseEntity<Map<String, Map<String,Long>>> report(HttpServletRequest httpServletRequest) {
        Map<String, Map<String,Long>> reports = new HashMap<>() {{
            put("click", clickMap);
            put("impression", impressionMap);
        }};

        return ResponseEntity.status(HttpStatus.OK).body(reports);
    }
}
