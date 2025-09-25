package vn.cineshow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import vn.cineshow.dto.request.EmailRegisterRequest;
import vn.cineshow.service.RegisterService;

@RestController
@RequestMapping("/register")
@RequiredArgsConstructor
public class RegisterController {
    private final RegisterService registerService;

    @PostMapping("/email")
    public ResponseEntity<?> registerByEmail(@RequestBody @Valid EmailRegisterRequest req) {
        if (!req.password().equals(req.confirmPassword())) {
            return ResponseEntity.badRequest().body("password != confirmPassword");
        }
        long id = registerService.registerByEmail(req);
        return ResponseEntity.created(URI.create("/users/" + id)).build();
    }
}
