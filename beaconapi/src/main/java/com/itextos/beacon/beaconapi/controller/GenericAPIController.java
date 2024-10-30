package com.itextos.beacon.beaconapi.controller;



import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/genericapi")  // Base path for all endpoints in this controller
public class GenericAPIController {

    @RequestMapping(value = "/customerdn", method = {RequestMethod.GET, RequestMethod.POST})
    public Mono<String> sayHello() {
        return Mono.just("Hello, WebFlux!");
    }
}
