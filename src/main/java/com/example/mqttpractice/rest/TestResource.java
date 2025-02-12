package com.example.mqttpractice.rest;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/test")
public class TestResource {
    //

    @GetMapping
    public String test() {
        //
        return UUID.randomUUID().toString();
    }
}
