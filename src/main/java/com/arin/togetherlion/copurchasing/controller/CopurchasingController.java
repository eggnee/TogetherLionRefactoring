package com.arin.togetherlion.copurchasing.controller;

import com.arin.togetherlion.copurchasing.domain.dto.CopurchasingCreateRequest;
import com.arin.togetherlion.copurchasing.service.CopurchasingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/copurchasings")
@RequiredArgsConstructor
public class CopurchasingController {

    private final CopurchasingService copurchasingService;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid CopurchasingCreateRequest request) {
        final Long copurchasingId = copurchasingService.create(request);
        return ResponseEntity.created(URI.create("/copurchasings/" + copurchasingId)).build();
    }

    @DeleteMapping("/{copurchasingId}")
    public ResponseEntity<String> delete(@PathVariable(name = "copurchasingId") Long copurchasingId, @RequestBody Long userId) {
        copurchasingService.delete(userId, copurchasingId);
        return ResponseEntity.noContent().build();
    }
}

