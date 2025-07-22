package com.ashu.chatapp.avatars;


import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/proxy")
public class Avatars {

    @GetMapping("/neko-image")
    public ResponseEntity<?> getNekoImage(@RequestParam(value = "contactsCount", required = false) Integer contactsCount) {
        int limit = (contactsCount != null && contactsCount > 0) ? contactsCount : 1;
        String apiUrl = "https://api.nekosapi.com/v4/images/random?limit=" + limit + "&rating=safe";
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(apiUrl, String.class);
        return ResponseEntity
                .status(response.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(response.getBody());
    }
}
