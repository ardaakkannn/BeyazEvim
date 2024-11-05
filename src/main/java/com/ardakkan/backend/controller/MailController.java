package com.ardakkan.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ardakkan.backend.service.MailService;

@RestController
public class MailController {

    private final MailService mailService;

    @Autowired
    public MailController(MailService mailService) {
        this.mailService = mailService;
    }

    @GetMapping("/sendMail")
    public String sendMail(@RequestParam String to, @RequestParam String subject, @RequestParam String text) {
        mailService.sendSimpleMail(to, subject, text);
        return "Mail sent successfully!";
    }
}

