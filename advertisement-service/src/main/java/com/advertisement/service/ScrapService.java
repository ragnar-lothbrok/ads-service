package com.advertisement.service;

import com.advertisement.dtos.Ads;
import java.io.IOException;

public interface ScrapService {
    Ads scrap(String url) throws IOException;
}
