package com.chefai.security.history;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.chefai.security.user.User;
import java.util.List;

@RestController
@RequestMapping("/api/v1/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService service;

    @PostMapping
    public ResponseEntity<?> save(
            @RequestBody HistoryRequest request
    ) {
        service.save(request);
        return ResponseEntity.accepted().build();
    }

    @GetMapping
    public ResponseEntity<List<History>> findHistory(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(service.findByUserId(user.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id,
            @AuthenticationPrincipal User user
    ) {
        service.delete(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<History>> findAllHistory() {
        return ResponseEntity.ok(service.findAll());
    }
}
