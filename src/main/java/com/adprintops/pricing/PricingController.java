package com.adprintops.pricing;

import com.adprintops.pricing.domain.PricingMaterial;
import com.adprintops.pricing.dto.CalculatePriceRequest;
import com.adprintops.pricing.dto.CalculatePriceResponse;
import com.adprintops.pricing.dto.DecalPriceRequest;
import com.adprintops.pricing.dto.DecalPriceResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pricing")
public class PricingController {

    private final PricingService pricingService;

    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @PostMapping("/calculate")
    public ResponseEntity<CalculatePriceResponse> calculatePrice(@Valid @RequestBody CalculatePriceRequest request) {
        CalculatePriceResponse response = pricingService.calculatePrice(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{category}")
    public ResponseEntity<CalculatePriceResponse> calculateCategoryPrice(
            @PathVariable String category,
            @Valid @RequestBody CalculatePriceRequest request
    ) {
        CalculatePriceRequest fullRequest = new CalculatePriceRequest(
                category.toUpperCase(),
                request.widthM(),
                request.heightM(),
                request.quantity(),
                request.materialCode(),
                request.hasLamination(),
                request.hasDieCut(),
                request.boxCount(),
                request.frameTubeSize(),
                request.paperGsm(),
                request.sheetCount(),
                request.customFee(),
                request.cutMode(),
                request.maxSideM(),
                request.rollWidthTac(),
                request.hiflexType(),
                request.marginCm(),
                request.hasLeg(),
                request.paperSubtype(),
                request.paperSides(),
                request.tranhType(),
                request.tranhPreset(),
                request.tranhPackage()
        );
        CalculatePriceResponse response = pricingService.calculatePrice(fullRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/decal")
    public ResponseEntity<DecalPriceResponse> calculateDecalPrice(@Valid @RequestBody DecalPriceRequest request) {
        DecalPriceResponse response = pricingService.calculateDecalPrice(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/materials")
    public ResponseEntity<List<PricingMaterial>> getActiveMaterials(@RequestParam(required = false, defaultValue = "DECAL") String categoryCode) {
        List<PricingMaterial> materials = pricingService.getActiveMaterials(categoryCode);
        return ResponseEntity.ok(materials);
    }

    @GetMapping("/decal/materials")
    public ResponseEntity<List<PricingMaterial>> getDecalActiveMaterials() {
        List<PricingMaterial> materials = pricingService.getActiveMaterials("DECAL");
        return ResponseEntity.ok(materials);
    }
}
