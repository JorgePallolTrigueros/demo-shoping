package com.shoppingcart.demo.service.campaign;

import com.shoppingcart.demo.dto.CampaignResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "campaign-service", url = "http://localhost:8082")
public interface CampaingApiService {

    @GetMapping("/v1/notification/campaign/{userId}")
    List<CampaignResponseDto> getCampaignByUserId(@PathVariable("userId") String userId);
}
