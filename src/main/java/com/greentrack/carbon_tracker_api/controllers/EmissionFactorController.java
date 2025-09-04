package com.greentrack.carbon_tracker_api.controllers;

import com.greentrack.carbon_tracker_api.advice.ApiResponse;
import com.greentrack.carbon_tracker_api.dto.emissionFactorDto.EmissionFactorCreateRequest;
import com.greentrack.carbon_tracker_api.dto.emissionFactorDto.EmissionFactorResponse;
import com.greentrack.carbon_tracker_api.dto.userDto.UserResponse;
import com.greentrack.carbon_tracker_api.entities.EmissionFactor;
import com.greentrack.carbon_tracker_api.entities.enums.ActivityCategory;
import com.greentrack.carbon_tracker_api.repositories.EmissionFactorRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/emission-factor")
@RequiredArgsConstructor
public class EmissionFactorController {

    private final EmissionFactorRepository emissionFactorRepository;
    private final ModelMapper modelMapper;

    @PostMapping
    public ResponseEntity<ApiResponse<EmissionFactorResponse>> createEmissionFactor(
            @Valid @RequestBody EmissionFactorCreateRequest request) {

        UserResponse user = (UserResponse) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            //check if factor already exists
            if(emissionFactorRepository.existsByRegionAndCategoryAndSubTypeAndUnit(
                   request.getRegion(), request.getCategory(), request.getSubType(), request.getUnit())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Emission Factor already exists for this emission"));
            }

            EmissionFactor factor = EmissionFactor.builder()
                    .region(request.getRegion())
                    .category(request.getCategory())
                    .subType(request.getSubType())
                    .unit(request.getUnit())
                    .co2eFactor(request.getCo2eFactor())
                    .methodology(request.getMethodology())
                    .source(request.getSource())
                    .createdAt(LocalDateTime.now())
                    .createdBy(user.getEmail())
                    .updatedAt(LocalDateTime.now())
                    .updatedBy(user.getEmail())
                    .build();

            EmissionFactor savedFactor = emissionFactorRepository.save(factor);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Emission Factor created successfully",
                            modelMapper.map(savedFactor, EmissionFactorResponse.class)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create emission factor: " + e.getMessage()));
        }
    }


    @GetMapping
    public ResponseEntity<ApiResponse<List<EmissionFactorResponse>>> getAllEmissionFactors() {
        List<EmissionFactor> factors = emissionFactorRepository.findAll();
        List<EmissionFactorResponse> responses = factors.stream()
                .map(factor -> modelMapper.map(factor, EmissionFactorResponse.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/region/{region}")
    public ResponseEntity<ApiResponse<List<EmissionFactorResponse>>> getEmissionFactorsByRegion(@PathVariable String region) {
        List<EmissionFactor> factors = emissionFactorRepository.findByRegionOrderByCategory(region);
        List<EmissionFactorResponse> responses = factors.stream()
                .map(factor -> modelMapper.map(factor, EmissionFactorResponse.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<EmissionFactorResponse>>> getEmissionFactorsByCategory(@PathVariable ActivityCategory category) {
        List<EmissionFactor> factors = emissionFactorRepository.findByCategoryOrderBySubType(category);
        List<EmissionFactorResponse> responses = factors.stream()
                .map(factor -> modelMapper.map(factor, EmissionFactorResponse.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @DeleteMapping("/{factorId}")
    public ResponseEntity<ApiResponse<String>> deleteEmissionFactor(@PathVariable String factorId) {
        try {
            emissionFactorRepository.deleteById(factorId);
            return ResponseEntity.ok(ApiResponse.success("Emission factor deleted successfully"));

        } catch ( Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete the emission factor"));
        }
    }
}
