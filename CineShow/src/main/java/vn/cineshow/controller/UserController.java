package vn.cineshow.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/")
public class UserController {
    @GetMapping("/all-user")
    public String getAll() {
        return "Hello world";
    }

}
