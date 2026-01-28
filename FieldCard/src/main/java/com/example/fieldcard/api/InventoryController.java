package com.example.fieldcard.api;

import com.example.fieldcard.core.app.service.InventoryService;
import com.example.fieldcard.dto.request.AddStockItemDto;
import com.example.fieldcard.dto.response.UserStockDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ResponseEntity<List<UserStockDto>> getUserStock(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(inventoryService.getUserStock(userDetails.getUsername()));
    }

    @PostMapping
    public ResponseEntity<Void> addStockItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody AddStockItemDto dto
    ) {
        inventoryService.addStockItem(userDetails.getUsername(), dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStockItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        inventoryService.removeStockItem(userDetails.getUsername(), id);
        return ResponseEntity.ok().build();
    }
}