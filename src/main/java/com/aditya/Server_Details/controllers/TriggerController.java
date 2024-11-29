package com.aditya.Server_Details.controllers;

import com.aditya.Server_Details.service.smps_service.SMPS_Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/serverTrigger")
public class TriggerController {

    @Autowired
    SMPS_Service smpsService;

    @GetMapping()
    public String runOs() throws IOException, InterruptedException {
        smpsService.runBatchFile();
        return "Processed!";
    }

}
