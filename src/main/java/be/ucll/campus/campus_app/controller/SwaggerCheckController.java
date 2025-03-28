package be.ucll.campus.campus_app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SwaggerCheckController {

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}
