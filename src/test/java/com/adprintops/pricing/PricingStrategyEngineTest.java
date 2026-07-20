package com.adprintops.pricing;

import com.adprintops.pricing.domain.*;
import com.adprintops.pricing.dto.CalculatePriceRequest;
import com.adprintops.pricing.dto.CalculatePriceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class PricingStrategyEngineTest {

    @Autowired
    private PricingService pricingService;

    @Autowired
    private PricingRuleRepository pricingRuleRepository;

    @Autowired
    private PricingMaterialRepository pricingMaterialRepository;

    @Autowired
    private PricingConfigurationRepository pricingConfigurationRepository;

    @BeforeEach
    void seedTestData() {
        pricingMaterialRepository.deleteAll();
        pricingRuleRepository.deleteAll();
        pricingConfigurationRepository.deleteAll();

        pricingRuleRepository.save(new PricingRule("DECAL", "Khổ chuẩn", new BigDecimal("0.000"), null, new BigDecimal("120000"), new BigDecimal("50000"), true, "Decal chuẩn"));
        pricingMaterialRepository.save(new PricingMaterial("DECAL", "thuong", "Decal thường", BigDecimal.ONE, BigDecimal.ZERO, true));

        // Seed test configurations matching Cacl source exactly
        pricingConfigurationRepository.save(new PricingConfiguration("BANG", "FORM_IN_LARGE", "Formex In (>=0.5m2)", new BigDecimal("300000"), BigDecimal.ONE, true, "Formex In"));
        pricingConfigurationRepository.save(new PricingConfiguration("CARD", "BOX_1_4", "Card 1-4", new BigDecimal("70000"), BigDecimal.ONE, true, ""));
        pricingConfigurationRepository.save(new PricingConfiguration("CARD", "BOX_5", "Card 5", new BigDecimal("40000"), BigDecimal.ONE, true, ""));
        pricingConfigurationRepository.save(new PricingConfiguration("CARD", "BOX_6_9", "Card 6-9", new BigDecimal("70000"), BigDecimal.ONE, true, ""));
        pricingConfigurationRepository.save(new PricingConfiguration("CARD", "BOX_10_PLUS", "Card 10+", new BigDecimal("24000"), BigDecimal.ONE, true, ""));
        pricingConfigurationRepository.save(new PricingConfiguration("GIAY", "ROI_A4_100_1000", "Tờ rơi A4 100g 1000 tờ", new BigDecimal("800000"), BigDecimal.ONE, true, "A4 100g"));
        pricingConfigurationRepository.save(new PricingConfiguration("TRANH", "LED_A4_FULL", "Tranh LED A4 full", new BigDecimal("790000"), BigDecimal.ONE, true, "LED A4"));
        
        // Seed 15 Biển Số Nhà combinations matching Cacl source snGia table
        pricingConfigurationRepository.save(new PricingConfiguration("TRANH", "SN_25X15_CHUNOI", "Số nhà 25x15 Chữ + số nổi", new BigDecimal("650000"), BigDecimal.ONE, true, "650k"));
        pricingConfigurationRepository.save(new PricingConfiguration("TRANH", "SN_30X20_ANMON", "Số nhà 30x20 Ăn mòn", new BigDecimal("520000"), BigDecimal.ONE, true, "520k"));
        pricingConfigurationRepository.save(new PricingConfiguration("TRANH", "SN_30X20_NOI", "Số nhà 30x20 Số nổi", new BigDecimal("750000"), BigDecimal.ONE, true, "750k"));
        pricingConfigurationRepository.save(new PricingConfiguration("TRANH", "SN_30X20_CHUNOI", "Số nhà 30x20 Chữ + số nổi", new BigDecimal("1000000"), BigDecimal.ONE, true, "1000k"));
        pricingConfigurationRepository.save(new PricingConfiguration("TRANH", "SN_35X25_NOI", "Số nhà 35x25 Số nổi", new BigDecimal("850000"), BigDecimal.ONE, true, "850k"));
        pricingConfigurationRepository.save(new PricingConfiguration("TRANH", "SN_35X25_CHUNOI", "Số nhà 35x25 Chữ + số nổi", new BigDecimal("1040000"), BigDecimal.ONE, true, "1040k"));
        pricingConfigurationRepository.save(new PricingConfiguration("TRANH", "SN_40X30_ANMON", "Số nhà 40x30 Ăn mòn", new BigDecimal("850000"), BigDecimal.ONE, true, "850k"));
        pricingConfigurationRepository.save(new PricingConfiguration("TRANH", "SN_60X40_ANMON", "Số nhà 60x40 Ăn mòn", new BigDecimal("1250000"), BigDecimal.ONE, true, "1250k"));

        pricingConfigurationRepository.save(new PricingConfiguration("KHAC", "BANG_LUA_CHAN", "Bảng lụa có chân", new BigDecimal("650000"), BigDecimal.ONE, true, "650k"));
    }

    @Test
    void testCalculateDecalMiniArea() {
        CalculatePriceRequest request = new CalculatePriceRequest(
                "DECAL", new BigDecimal("0.2"), new BigDecimal("0.2"), 1, "thuong", false, false, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null
        );
        CalculatePriceResponse response = pricingService.calculatePrice(request);
        assertNotNull(response);
        assertEquals("DECAL", response.categoryCode());
        assertFalse(response.vatIncluded());
        assertFalse(response.lineItems().isEmpty());
    }

    @Test
    void testCalculateCardVisitBoundaries() {
        // 5 boxes exact promo rule -> 40k/box = 200k
        CalculatePriceRequest r5 = new CalculatePriceRequest(
                "CARD", null, null, 5, null, false, false, 5, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null
        );
        CalculatePriceResponse resp5 = pricingService.calculatePrice(r5);
        assertEquals(new BigDecimal("200000"), resp5.totalPrice());

        // 6 boxes rule -> 70k/box = 420k
        CalculatePriceRequest r6 = new CalculatePriceRequest(
                "CARD", null, null, 6, null, false, false, 6, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null
        );
        CalculatePriceResponse resp6 = pricingService.calculatePrice(r6);
        assertEquals(new BigDecimal("420000"), resp6.totalPrice());

        // 10 boxes rule -> 24k/box = 240k
        CalculatePriceRequest r10 = new CalculatePriceRequest(
                "CARD", null, null, 10, null, false, false, 10, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null
        );
        CalculatePriceResponse resp10 = pricingService.calculatePrice(r10);
        assertEquals(new BigDecimal("240000"), resp10.totalPrice());
    }

    @Test
    void testCalculateBangLargeAreaLumpsum() {
        CalculatePriceRequest request = new CalculatePriceRequest(
                "BANG", new BigDecimal("1.0"), new BigDecimal("1.0"), 1, "form-in", false, false, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null
        );
        CalculatePriceResponse response = pricingService.calculatePrice(request);
        assertNotNull(response);
        assertEquals("BANG", response.categoryCode());
        assertEquals(new BigDecimal("300000"), response.totalPrice()); // Formex in >=0.5m² = 300k lumpsum
    }

    @Test
    void testCalculateHiflexFrameV16WithQuantity2AndLegs() {
        // Quantity = 2, Frame 16mm (65k/m), Legs = true -> 2 items x 4m legs = 8m leg iron
        CalculatePriceRequest request = new CalculatePriceRequest(
                "HIFLEX", new BigDecimal("1.0"), new BigDecimal("1.0"), 2, "lua", false, false, null, 16, null, null, null,
                null, null, null, "lua", 5, true, null, null, null, null, null
        );
        CalculatePriceResponse response = pricingService.calculatePrice(request);
        assertNotNull(response);
        assertEquals("HIFLEX", response.categoryCode());
        // Verify legs calculation: 8m @ 65k = 520,000đ for legs
        assertTrue(response.lineItems().stream().anyMatch(item -> item.code().equals("FRAME_LEGS") && item.amount().compareTo(new BigDecimal("520000")) == 0));
    }

    @Test
    void testCalculateSoNhaPrecedenceAndNullCombinationThrows422() {
        // Valid 30x20 ANMON
        CalculatePriceRequest reqValid = new CalculatePriceRequest(
                "TRANH", null, null, 1, "anmon", false, false, null, null, null, null, null,
                null, null, null, null, null, null, null, null, "so_nha", "30X20", "anmon"
        );
        CalculatePriceResponse respValid = pricingService.calculatePrice(reqValid);
        assertEquals(new BigDecimal("520000"), respValid.totalPrice());

        // Null combination 25x15 ANMON -> must throw 422 PricingConfigurationException
        CalculatePriceRequest reqNull = new CalculatePriceRequest(
                "TRANH", null, null, 1, "anmon", false, false, null, null, null, null, null,
                null, null, null, null, null, null, null, null, "so_nha", "25X15", "anmon"
        );
        assertThrows(PricingConfigurationException.class, () -> pricingService.calculatePrice(reqNull));
    }

    @Test
    void testCalculateKhacMissingConfigThrows422() {
        CalculatePriceRequest request = new CalculatePriceRequest(
                "KHAC", new BigDecimal("1.0"), new BigDecimal("1.0"), 1, "non_existent_service", false, false, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null
        );
        assertThrows(PricingConfigurationException.class, () -> pricingService.calculatePrice(request));
    }
}
