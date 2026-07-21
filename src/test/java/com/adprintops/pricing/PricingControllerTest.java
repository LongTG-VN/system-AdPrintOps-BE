package com.adprintops.pricing;

import com.adprintops.pricing.domain.PricingMaterial;
import com.adprintops.pricing.domain.PricingMaterialRepository;
import com.adprintops.pricing.domain.PricingRule;
import com.adprintops.pricing.domain.PricingRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PricingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PricingRuleRepository pricingRuleRepository;

    @Autowired
    private PricingMaterialRepository pricingMaterialRepository;

    @BeforeEach
    void seedDecalPricing() {
        pricingMaterialRepository.deleteAll();
        pricingRuleRepository.deleteAll();

        pricingRuleRepository.save(new PricingRule(
                "DECAL", "Nhỏ lẻ", new BigDecimal("0.000"), new BigDecimal("0.100"),
                new BigDecimal("200000"), new BigDecimal("50000"), true, "Diện tích dưới 0.1m²"
        ));
        pricingRuleRepository.save(new PricingRule(
                "DECAL", "Khổ nhỏ", new BigDecimal("0.100"), new BigDecimal("1.000"),
                new BigDecimal("140000"), new BigDecimal("50000"), true, "Diện tích từ 0.1m² đến dưới 1m²"
        ));
        pricingRuleRepository.save(new PricingRule(
                "DECAL", "Khổ chuẩn", new BigDecimal("1.000"), null,
                new BigDecimal("120000"), new BigDecimal("50000"), true, "Diện tích từ 1m² trở lên"
        ));
        pricingMaterialRepository.save(new PricingMaterial(
                "DECAL", "thuong", "Decal thường", BigDecimal.ONE, BigDecimal.ZERO, true
        ));
        pricingMaterialRepository.save(new PricingMaterial(
                "DECAL", "trong", "Decal trong / đẹp", new BigDecimal("1.50"), BigDecimal.ZERO, true
        ));
    }

    @Test
    void testCalculateDecalPrice_NormalDecal() throws Exception {
        String jsonPayload = """
                {
                    "widthM": 0.5,
                    "heightM": 0.5,
                    "quantity": 2,
                    "decalType": "thuong",
                    "hasLamination": false
                }
                """;

        mockMvc.perform(post("/api/v1/pricing/decal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.singleAreaSqm").value(0.25))
                .andExpect(jsonPath("$.singleUnitPrice").value(63000))
                .andExpect(jsonPath("$.totalPrice").value(126000));
    }

    @Test
    void testCalculateDecalPrice_DecalTrongWithLamination() throws Exception {
        String jsonPayload = """
                {
                    "widthM": 1.0,
                    "heightM": 2.0,
                    "quantity": 1,
                    "decalType": "trong",
                    "hasLamination": true
                }
                """;

        mockMvc.perform(post("/api/v1/pricing/decal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.singleAreaSqm").value(2.0))
                .andExpect(jsonPath("$.ratePerSqm").value(180000))
                .andExpect(jsonPath("$.laminationCost").value(100000))
                .andExpect(jsonPath("$.totalPrice").value(460000));
    }

    @Test
    void testCalculateDecalPrice_UsesDatabaseConfiguredMaterialAndRule() throws Exception {
        pricingMaterialRepository.save(new PricingMaterial(
                "DECAL", "premium", "Decal premium", new BigDecimal("1.20"), BigDecimal.ZERO, true
        ));

        String jsonPayload = """
                {
                    "widthM": 1.0,
                    "heightM": 2.0,
                    "quantity": 2,
                    "decalType": "premium",
                    "hasLamination": true
                }
                """;

        mockMvc.perform(post("/api/v1/pricing/decal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ratePerSqm").value(144000))
                .andExpect(jsonPath("$.laminationCost").value(100000))
                .andExpect(jsonPath("$.singleUnitPrice").value(388000))
                .andExpect(jsonPath("$.totalPrice").value(776000));
    }

    @Test
    void testCalculateDecalPrice_RejectsMissingPricingConfiguration() throws Exception {
        pricingRuleRepository.deleteAll();

        String jsonPayload = """
                {
                    "widthM": 1.0,
                    "heightM": 1.0,
                    "quantity": 1,
                    "decalType": "thuong",
                    "hasLamination": false
                }
                """;

        mockMvc.perform(post("/api/v1/pricing/decal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("PRICING_CONFIGURATION_UNAVAILABLE"));
    }

    @Test
    void unifiedPricingRejectsNegativeDimensions() throws Exception {
        mockMvc.perform(post("/api/v1/pricing/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryCode": "DECAL",
                                  "widthM": -1,
                                  "heightM": 1,
                                  "quantity": 1,
                                  "materialCode": "thuong",
                                  "hasLamination": false
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.widthM").exists());
    }

    @Test
    void unifiedPricingRejectsMissingRequiredDimensions() throws Exception {
        mockMvc.perform(post("/api/v1/pricing/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryCode": "DECAL",
                                  "quantity": 1,
                                  "materialCode": "thuong",
                                  "hasLamination": false
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.categoryInputValid").exists());
    }
}
