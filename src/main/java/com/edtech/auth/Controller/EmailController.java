package com.edtech.auth.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.edtech.auth.DTO.EmailDetails;
import com.edtech.auth.Services.EmailService;

@RestController
public class EmailController {
    
    @Autowired 
    private EmailService emailService;

    @PostMapping("/sendMail")
    public String
    sendMail(@RequestBody EmailDetails details)
    {
        String status = emailService.sendSimpleMail(details);

        return status;
    }

    // Sending email with attachment
    @PostMapping("/sendMailWithAttachment")
    public String sendMailWithAttachment(
        @RequestBody EmailDetails details)
    {
        String status
            = emailService.sendMailWithAttachment(details);

        return status;
    }

}
