package com.example.fieldcard.core.app.service;

import com.example.fieldcard.data.entity.PlantProtectionProduct;
import com.example.fieldcard.data.entity.User;
import com.example.fieldcard.data.entity.UserStock;
import com.example.fieldcard.data.repository.PlantProtectionProductRepository;
import com.example.fieldcard.data.repository.UserRepository;
import com.example.fieldcard.data.repository.UserStockRepository;
import com.example.fieldcard.dto.request.AddStockItemDto;
import com.example.fieldcard.dto.response.UserStockDto;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final UserStockRepository userStockRepository;
    private final UserRepository userRepository;
    private final PlantProtectionProductRepository productRepository;

    public InventoryService(UserStockRepository userStockRepository,
                            UserRepository userRepository,
                            PlantProtectionProductRepository productRepository) {
        this.userStockRepository = userStockRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<UserStockDto> getUserStock(String userEmail) {
        User user = getUser(userEmail);

        return userStockRepository.findAllByUser(user).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addStockItem(String userEmail, AddStockItemDto dto) {
        User user = getUser(userEmail);

        PlantProtectionProduct product = productRepository.findById(dto.productId())
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono produktu o ID: " + dto.productId()));

        UserStock stock = new UserStock();
        stock.setUser(user);
        stock.setProduct(product);
        stock.setQuantity(dto.quantity());
        stock.setUnit(dto.unit());
        stock.setBatchNumber(dto.batchNumber());
        stock.setExpirationDate(dto.expirationDate());
        stock.setPurchaseDate(dto.purchaseDate());

        userStockRepository.save(stock);
    }

    @Transactional
    public void removeStockItem(String userEmail, Long stockId) {
        User user = getUser(userEmail);

        UserStock stock = userStockRepository.findByIdAndUser(stockId, user)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono pozycji w magazynie"));

        userStockRepository.delete(stock);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono użytkownika"));
    }

    private UserStockDto mapToDto(UserStock stock) {
        return new UserStockDto(
                stock.getId(),
                stock.getProduct().getId(),
                stock.getProduct().getName(),
                stock.getProduct().getManufacturer(),
                stock.getQuantity(),
                stock.getUnit(),
                stock.getBatchNumber(),
                stock.getExpirationDate()
        );
    }
}