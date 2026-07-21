package com.adprintops.pricing;

import com.adprintops.pricing.domain.PricingConfiguration;
import com.adprintops.pricing.domain.PricingConfigurationRepository;
import com.adprintops.pricing.domain.PricingMaterial;
import com.adprintops.pricing.domain.PricingMaterialRepository;
import com.adprintops.pricing.domain.PricingRule;
import com.adprintops.pricing.domain.PricingRuleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Profile("dev-seed")
public class PricingDataInitializer implements CommandLineRunner {

    private final PricingRuleRepository pricingRuleRepository;
    private final PricingConfigurationRepository pricingConfigurationRepository;
    private final PricingMaterialRepository pricingMaterialRepository;

    public PricingDataInitializer(PricingRuleRepository pricingRuleRepository,
                                  PricingConfigurationRepository pricingConfigurationRepository,
                                  PricingMaterialRepository pricingMaterialRepository) {
        this.pricingRuleRepository = pricingRuleRepository;
        this.pricingConfigurationRepository = pricingConfigurationRepository;
        this.pricingMaterialRepository = pricingMaterialRepository;
    }

    @Override
    public void run(String... args) {
        // Seed default rules if empty
        if (pricingRuleRepository.count() == 0) {
            pricingRuleRepository.save(new PricingRule("DECAL", "Nhỏ lẻ", BigDecimal.ZERO, new BigDecimal("0.100"), new BigDecimal("200000"), new BigDecimal("50000"), true, "Diện tích dưới 0.1m²"));
            pricingRuleRepository.save(new PricingRule("DECAL", "Khổ nhỏ", new BigDecimal("0.100"), new BigDecimal("1.000"), new BigDecimal("140000"), new BigDecimal("50000"), true, "Diện tích từ 0.1m² đến dưới 1m²"));
            pricingRuleRepository.save(new PricingRule("DECAL", "Khổ chuẩn", new BigDecimal("1.000"), null, new BigDecimal("120000"), new BigDecimal("50000"), true, "Diện tích từ 1m² trở lên"));

            pricingRuleRepository.save(new PricingRule("TEM", "Tem không bế", new BigDecimal("0.001"), new BigDecimal("9999.000"), new BigDecimal("100000"), BigDecimal.ZERO, true, "100k/m2"));
            pricingRuleRepository.save(new PricingRule("CAT", "Cắt decal chuẩn", new BigDecimal("0.001"), new BigDecimal("9999.000"), new BigDecimal("100000"), BigDecimal.ZERO, true, "Cắt viền"));
            pricingRuleRepository.save(new PricingRule("CARD", "Card Visit 1-4 hộp", new BigDecimal("1.000"), new BigDecimal("5.000"), new BigDecimal("70000"), BigDecimal.ZERO, true, "Card Visit 1-4 hộp (70k/hộp)"));
            pricingRuleRepository.save(new PricingRule("CARD", "Card Visit tròn 5 hộp", new BigDecimal("5.000"), new BigDecimal("6.000"), new BigDecimal("40000"), BigDecimal.ZERO, true, "Card Visit tròn 5 hộp (40k/hộp)"));
            pricingRuleRepository.save(new PricingRule("CARD", "Card Visit 6-9 hộp", new BigDecimal("6.000"), new BigDecimal("10.000"), new BigDecimal("70000"), BigDecimal.ZERO, true, "Card Visit 6-9 hộp (70k/hộp)"));
            pricingRuleRepository.save(new PricingRule("CARD", "Card Visit >=10 hộp", new BigDecimal("10.000"), null, new BigDecimal("24000"), BigDecimal.ZERO, true, "Card Visit >=10 hộp (24k/hộp)"));
            pricingRuleRepository.save(new PricingRule("HIFLEX", "Bạt Hiflex thường", new BigDecimal("0.001"), new BigDecimal("9999.000"), new BigDecimal("45000"), BigDecimal.ZERO, true, "Hiflex thường"));
        }

        // Seed default materials if empty
        if (pricingMaterialRepository.count() == 0) {
            pricingMaterialRepository.save(new PricingMaterial("DECAL", "thuong", "Decal in (khổ chuẩn)", BigDecimal.ONE, BigDecimal.ZERO, true));
            pricingMaterialRepository.save(new PricingMaterial("DECAL", "trong", "Decal trong / đẹp", new BigDecimal("1.50"), BigDecimal.ZERO, true));

            pricingMaterialRepository.save(new PricingMaterial("TEM", "thuong", "Decal tem thường", BigDecimal.ONE, BigDecimal.ZERO, true));
            pricingMaterialRepository.save(new PricingMaterial("TEM", "be", "Decal tem bế hình", BigDecimal.ONE, BigDecimal.ZERO, true));

            pricingMaterialRepository.save(new PricingMaterial("CAT", "decal_si", "Decal si (3 tháng) khổ 60", BigDecimal.ONE, BigDecimal.ZERO, true));
            pricingMaterialRepository.save(new PricingMaterial("CAT", "decal_pq", "Decal PQ khổ 60 (Dạ quang - 1.5 năm)", new BigDecimal("2.00"), BigDecimal.ZERO, true));
            pricingMaterialRepository.save(new PricingMaterial("CAT", "decal_in_be", "Decal in bế khổ 90/100/120", new BigDecimal("2.00"), BigDecimal.ZERO, true));
            pricingMaterialRepository.save(new PricingMaterial("CAT", "decal_tot_1", "Decal tốt 1 lớp khổ 120 (Nền trắng)", new BigDecimal("1.50"), BigDecimal.ZERO, true));
            pricingMaterialRepository.save(new PricingMaterial("CAT", "decal_tot_2", "Decal tốt 2 lớp khổ 120", new BigDecimal("3.00"), BigDecimal.ZERO, true));

            pricingMaterialRepository.save(new PricingMaterial("CARD", "couche_300", "Giấy Couche 300g chuẩn", BigDecimal.ONE, BigDecimal.ZERO, true));

            pricingMaterialRepository.save(new PricingMaterial("HIFLEX", "lua", "Bạt Hiflex lụa thường", BigDecimal.ONE, BigDecimal.ZERO, true));
            pricingMaterialRepository.save(new PricingMaterial("HIFLEX", "xuyen_den", "Vải hiflex 2 da xuyên đèn", new BigDecimal("1.50"), BigDecimal.ZERO, true));

            pricingMaterialRepository.save(new PricingMaterial("BANG", "tole-in", "Tole in decal dán", BigDecimal.ONE, BigDecimal.ZERO, true));
            pricingMaterialRepository.save(new PricingMaterial("BANG", "form-in", "Formex in decal dán", BigDecimal.ONE, BigDecimal.ZERO, true));
            pricingMaterialRepository.save(new PricingMaterial("BANG", "alu-in", "Alu in decal dán", BigDecimal.ONE, BigDecimal.ZERO, true));
            pricingMaterialRepository.save(new PricingMaterial("BANG", "mica-in", "Mica in decal dán", BigDecimal.ONE, BigDecimal.ZERO, true));

            pricingMaterialRepository.save(new PricingMaterial("GIAY", "couche_100", "Giấy Couche 100g", BigDecimal.ONE, BigDecimal.ZERO, true));
            pricingMaterialRepository.save(new PricingMaterial("GIAY", "couche_150", "Giấy Couche 150g", BigDecimal.ONE, BigDecimal.ZERO, true));
            pricingMaterialRepository.save(new PricingMaterial("GIAY", "couche_200", "Giấy Couche 200g", BigDecimal.ONE, BigDecimal.ZERO, true));

            pricingMaterialRepository.save(new PricingMaterial("TRANH", "led_a4", "Tranh LED A4 Ultra Slim", BigDecimal.ONE, BigDecimal.ZERO, true));
            pricingMaterialRepository.save(new PricingMaterial("TRANH", "sn_30x20", "Biển số nhà 3D (30x20cm)", BigDecimal.ONE, BigDecimal.ZERO, true));

            pricingMaterialRepository.save(new PricingMaterial("KHAC", "can", "Phụ phí cán màng phụ", BigDecimal.ONE, BigDecimal.ZERO, true));
            pricingMaterialRepository.save(new PricingMaterial("KHAC", "bang_lua", "Bảng lụa có chân", BigDecimal.ONE, BigDecimal.ZERO, true));
        }

        // Seed or update configurations
        saveConfigIfMissing("CARD", "BOX_1_4", "Card Visit 1-4 hộp", new BigDecimal("70000"), "70k/hộp");
        saveConfigIfMissing("CARD", "BOX_5", "Card Visit tròn 5 hộp", new BigDecimal("40000"), "40k/hộp");
        saveConfigIfMissing("CARD", "BOX_6_9", "Card Visit 6-9 hộp", new BigDecimal("70000"), "70k/hộp");
        saveConfigIfMissing("CARD", "BOX_10_PLUS", "Card Visit >=10 hộp", new BigDecimal("24000"), "24k/hộp");

        saveConfigIfMissing("BANG", "TOLE_IN_MINI", "Tole In (<=0.06m2)", new BigDecimal("40000"), "Tole In nhỏ");
        saveConfigIfMissing("BANG", "TOLE_IN_MID", "Tole In (<0.5m2)", new BigDecimal("550000"), "Tole In vừa");
        saveConfigIfMissing("BANG", "TOLE_IN_LARGE", "Tole In (>=0.5m2)", new BigDecimal("450000"), "Tole In lớn");
        saveConfigIfMissing("BANG", "TOLE_CAT_MINI", "Tole Cắt (<=0.06m2)", new BigDecimal("50000"), "Tole Cắt nhỏ");
        saveConfigIfMissing("BANG", "TOLE_CAT_MID", "Tole Cắt (<0.5m2)", new BigDecimal("700000"), "Tole Cắt vừa");
        saveConfigIfMissing("BANG", "TOLE_CAT_LARGE", "Tole Cắt (>=0.5m2)", new BigDecimal("550000"), "Tole Cắt lớn");

        saveConfigIfMissing("BANG", "FORM_IN_MINI", "Formex In (<=0.06m2)", new BigDecimal("40000"), "Formex In nhỏ");
        saveConfigIfMissing("BANG", "FORM_IN_MID", "Formex In (<0.5m2)", new BigDecimal("400000"), "Formex In vừa");
        saveConfigIfMissing("BANG", "FORM_IN_LARGE", "Formex In (>=0.5m2)", new BigDecimal("300000"), "Formex In lớn");
        saveConfigIfMissing("BANG", "FORM_CAT_MINI", "Formex Cắt (<=0.06m2)", new BigDecimal("50000"), "Formex Cắt nhỏ");
        saveConfigIfMissing("BANG", "FORM_CAT_MID", "Formex Cắt (<0.5m2)", new BigDecimal("600000"), "Formex Cắt vừa");
        saveConfigIfMissing("BANG", "FORM_CAT_LARGE", "Formex Cắt (>=0.5m2)", new BigDecimal("450000"), "Formex Cắt lớn");

        saveConfigIfMissing("BANG", "ALU_IN_MINI", "Alu In (<=0.06m2)", new BigDecimal("40000"), "Alu In nhỏ");
        saveConfigIfMissing("BANG", "ALU_IN_MID", "Alu In (<0.5m2)", new BigDecimal("650000"), "Alu In vừa");
        saveConfigIfMissing("BANG", "ALU_IN_LARGE", "Alu In (>=0.5m2)", new BigDecimal("500000"), "Alu In lớn");
        saveConfigIfMissing("BANG", "ALU_CAT_MINI", "Alu Cắt (<=0.06m2)", new BigDecimal("70000"), "Alu Cắt nhỏ");
        saveConfigIfMissing("BANG", "ALU_CAT_MID", "Alu Cắt (<0.5m2)", new BigDecimal("900000"), "Alu Cắt vừa");
        saveConfigIfMissing("BANG", "ALU_CAT_LARGE", "Alu Cắt (>=0.5m2)", new BigDecimal("700000"), "Alu Cắt lớn");

        saveConfigIfMissing("BANG", "MICA_IN_MINI", "Mica In (<=0.06m2)", new BigDecimal("40000"), "Mica In nhỏ");
        saveConfigIfMissing("BANG", "MICA_IN_MID", "Mica In (<0.5m2)", new BigDecimal("1250000"), "Mica In vừa");
        saveConfigIfMissing("BANG", "MICA_IN_LARGE", "Mica In (>=0.5m2)", new BigDecimal("900000"), "Mica In lớn");
        saveConfigIfMissing("BANG", "MICA_CAT_MINI", "Mica Cắt (<=0.06m2)", new BigDecimal("90000"), "Mica Cắt nhỏ");
        saveConfigIfMissing("BANG", "MICA_CAT_MID", "Mica Cắt (<0.5m2)", new BigDecimal("1500000"), "Mica Cắt vừa");
        saveConfigIfMissing("BANG", "MICA_CAT_LARGE", "Mica Cắt (>=0.5m2)", new BigDecimal("1000000"), "Mica Cắt lớn");

        // GIAY - ROI A4 (+20k updated from Image 1)
        saveConfigIfMissing("GIAY", "ROI_A4_100_500", "Tờ rơi A4 100g (500 tờ)", new BigDecimal("720000"), "A4 100g 500t");
        saveConfigIfMissing("GIAY", "ROI_A4_100_1000", "Tờ rơi A4 100g (1.000 tờ)", new BigDecimal("820000"), "A4 100g 1000t");
        saveConfigIfMissing("GIAY", "ROI_A4_100_2000", "Tờ rơi A4 100g (2.000 tờ)", new BigDecimal("1120000"), "A4 100g 2000t");
        saveConfigIfMissing("GIAY", "ROI_A4_100_3000", "Tờ rơi A4 100g (3.000 tờ)", new BigDecimal("1480000"), "A4 100g 3000t");
        saveConfigIfMissing("GIAY", "ROI_A4_100_5000", "Tờ rơi A4 100g (5.000 tờ)", new BigDecimal("2030000"), "A4 100g 5000t");
        saveConfigIfMissing("GIAY", "ROI_A4_100_10000", "Tờ rơi A4 100g (10.000 tờ)", new BigDecimal("3710000"), "A4 100g 10000t");

        saveConfigIfMissing("GIAY", "ROI_A4_150_500", "Tờ rơi A4 150g (500 tờ)", new BigDecimal("780000"), "A4 150g 500t");
        saveConfigIfMissing("GIAY", "ROI_A4_150_1000", "Tờ rơi A4 150g (1.000 tờ)", new BigDecimal("880000"), "A4 150g 1000t");
        saveConfigIfMissing("GIAY", "ROI_A4_150_2000", "Tờ rơi A4 150g (2.000 tờ)", new BigDecimal("1210000"), "A4 150g 2000t");
        saveConfigIfMissing("GIAY", "ROI_A4_150_3000", "Tờ rơi A4 150g (3.000 tờ)", new BigDecimal("1600000"), "A4 150g 3000t");
        saveConfigIfMissing("GIAY", "ROI_A4_150_5000", "Tờ rơi A4 150g (5.000 tờ)", new BigDecimal("2300000"), "A4 150g 5000t");
        saveConfigIfMissing("GIAY", "ROI_A4_150_10000", "Tờ rơi A4 150g (10.000 tờ)", new BigDecimal("4380000"), "A4 150g 10000t");

        saveConfigIfMissing("GIAY", "ROI_A4_200_500", "Tờ rơi A4 200g (500 tờ)", new BigDecimal("980000"), "A4 200g 500t");
        saveConfigIfMissing("GIAY", "ROI_A4_200_1000", "Tờ rơi A4 200g (1.000 tờ)", new BigDecimal("1130000"), "A4 200g 1000t");
        saveConfigIfMissing("GIAY", "ROI_A4_200_2000", "Tờ rơi A4 200g (2.000 tờ)", new BigDecimal("1520000"), "A4 200g 2000t");
        saveConfigIfMissing("GIAY", "ROI_A4_200_3000", "Tờ rơi A4 200g (3.000 tờ)", new BigDecimal("1940000"), "A4 200g 3000t");
        saveConfigIfMissing("GIAY", "ROI_A4_200_5000", "Tờ rơi A4 200g (5.000 tờ)", new BigDecimal("3080000"), "A4 200g 5000t");
        saveConfigIfMissing("GIAY", "ROI_A4_200_10000", "Tờ rơi A4 200g (10.000 tờ)", new BigDecimal("5980000"), "A4 200g 10000t");

        // GIAY - ROI A5 (+20k updated from Image 1)
        saveConfigIfMissing("GIAY", "ROI_A5_100_1000", "Tờ rơi A5 100g (1.000 tờ)", new BigDecimal("760000"), "A5 100g 1000t");
        saveConfigIfMissing("GIAY", "ROI_A5_100_2000", "Tờ rơi A5 100g (2.000 tờ)", new BigDecimal("900000"), "A5 100g 2000t");
        saveConfigIfMissing("GIAY", "ROI_A5_100_4000", "Tờ rơi A5 100g (4.000 tờ)", new BigDecimal("1280000"), "A5 100g 4000t");
        saveConfigIfMissing("GIAY", "ROI_A5_100_6000", "Tờ rơi A5 100g (6.000 tờ)", new BigDecimal("1580000"), "A5 100g 6000t");
        saveConfigIfMissing("GIAY", "ROI_A5_100_10000", "Tờ rơi A5 100g (10.000 tờ)", new BigDecimal("2100000"), "A5 100g 10000t");
        saveConfigIfMissing("GIAY", "ROI_A5_100_20000", "Tờ rơi A5 100g (20.000 tờ)", new BigDecimal("3450000"), "A5 100g 20000t");

        saveConfigIfMissing("GIAY", "ROI_A5_150_1000", "Tờ rơi A5 150g (1.000 tờ)", new BigDecimal("830000"), "A5 150g 1000t");
        saveConfigIfMissing("GIAY", "ROI_A5_150_2000", "Tờ rơi A5 150g (2.000 tờ)", new BigDecimal("970000"), "A5 150g 2000t");
        saveConfigIfMissing("GIAY", "ROI_A5_150_4000", "Tờ rơi A5 150g (4.000 tờ)", new BigDecimal("1350000"), "A5 150g 4000t");
        saveConfigIfMissing("GIAY", "ROI_A5_150_6000", "Tờ rơi A5 150g (6.000 tờ)", new BigDecimal("1800000"), "A5 150g 6000t");
        saveConfigIfMissing("GIAY", "ROI_A5_150_10000", "Tờ rơi A5 150g (10.000 tờ)", new BigDecimal("2450000"), "A5 150g 10000t");
        saveConfigIfMissing("GIAY", "ROI_A5_150_20000", "Tờ rơi A5 150g (20.000 tờ)", new BigDecimal("4330000"), "A5 150g 20000t");

        saveConfigIfMissing("GIAY", "ROI_A5_200_1000", "Tờ rơi A5 200g (1.000 tờ)", new BigDecimal("1060000"), "A5 200g 1000t");
        saveConfigIfMissing("GIAY", "ROI_A5_200_2000", "Tờ rơi A5 200g (2.000 tờ)", new BigDecimal("1200000"), "A5 200g 2000t");
        saveConfigIfMissing("GIAY", "ROI_A5_200_4000", "Tờ rơi A5 200g (4.000 tờ)", new BigDecimal("1650000"), "A5 200g 4000t");
        saveConfigIfMissing("GIAY", "ROI_A5_200_6000", "Tờ rơi A5 200g (6.000 tờ)", new BigDecimal("2150000"), "A5 200g 6000t");
        saveConfigIfMissing("GIAY", "ROI_A5_200_10000", "Tờ rơi A5 200g (10.000 tờ)", new BigDecimal("2980000"), "A5 200g 10000t");
        saveConfigIfMissing("GIAY", "ROI_A5_200_20000", "Tờ rơi A5 200g (20.000 tờ)", new BigDecimal("5180000"), "A5 200g 20000t");

        // TRANH
        saveConfigIfMissing("TRANH", "LED_A4_FULL", "Tranh LED A4 full", new BigDecimal("790000"), "LED A4 Full");
        saveConfigIfMissing("TRANH", "LED_A4_IN", "Tranh LED A4 chỉ in", new BigDecimal("90000"), "LED A4 In");
        saveConfigIfMissing("TRANH", "LED_A3_FULL", "Tranh LED A3 full", new BigDecimal("905000"), "LED A3 Full");
        saveConfigIfMissing("TRANH", "LED_A3_IN", "Tranh LED A3 chỉ in", new BigDecimal("100000"), "LED A3 In");
        saveConfigIfMissing("TRANH", "LED_A2_FULL", "Tranh LED A2 full", new BigDecimal("1120000"), "LED A2 Full");
        saveConfigIfMissing("TRANH", "LED_A2_IN", "Tranh LED A2 chỉ in", new BigDecimal("115000"), "LED A2 In");

        saveConfigIfMissing("TRANH", "SN_25X15_ANMON", "Số nhà 25x15 Ăn mòn", new BigDecimal("450000"), "25x15 Ăn mòn");
        saveConfigIfMissing("TRANH", "SN_25X15_NOI", "Số nhà 25x15 Số nổi", new BigDecimal("550000"), "25x15 Số nổi");
        saveConfigIfMissing("TRANH", "SN_25X15_CHUNOI", "Số nhà 25x15 Chữ + số nổi", new BigDecimal("650000"), "25x15 Chữ nổi");
        saveConfigIfMissing("TRANH", "SN_30X20_ANMON", "Số nhà 30x20 Ăn mòn", new BigDecimal("520000"), "30x20 Ăn mòn");
        saveConfigIfMissing("TRANH", "SN_30X20_NOI", "Số nhà 30x20 Số nổi", new BigDecimal("750000"), "30x20 Số nổi");
        saveConfigIfMissing("TRANH", "SN_30X20_CHUNOI", "Số nhà 30x20 Chữ + số nổi", new BigDecimal("1000000"), "30x20 Chữ nổi");
        saveConfigIfMissing("TRANH", "SN_35X25_ANMON", "Số nhà 35x25 Ăn mòn", new BigDecimal("650000"), "35x25 Ăn mòn");
        saveConfigIfMissing("TRANH", "SN_35X25_NOI", "Số nhà 35x25 Số nổi", new BigDecimal("850000"), "35x25 Số nổi");
        saveConfigIfMissing("TRANH", "SN_35X25_CHUNOI", "Số nhà 35x25 Chữ + số nổi", new BigDecimal("1040000"), "35x25 Chữ nổi");
        saveConfigIfMissing("TRANH", "SN_40X30_ANMON", "Số nhà 40x30 Ăn mòn", new BigDecimal("850000"), "40x30 Ăn mòn");
        saveConfigIfMissing("TRANH", "SN_40X30_NOI", "Số nhà 40x30 Số nổi", new BigDecimal("1100000"), "40x30 Số nổi");
        saveConfigIfMissing("TRANH", "SN_40X30_CHUNOI", "Số nhà 40x30 Chữ + số nổi", new BigDecimal("1350000"), "40x30 Chữ nổi");
        saveConfigIfMissing("TRANH", "SN_60X40_ANMON", "Số nhà 60x40 Ăn mòn", new BigDecimal("1250000"), "60x40 Ăn mòn");
        saveConfigIfMissing("TRANH", "SN_60X40_NOI", "Số nhà 60x40 Số nổi", new BigDecimal("1600000"), "60x40 Số nổi");
        saveConfigIfMissing("TRANH", "SN_60X40_CHUNOI", "Số nhà 60x40 Chữ + số nổi", new BigDecimal("1900000"), "60x40 Chữ nổi");

        // KHAC
        saveConfigIfMissing("KHAC", "BANG_LUA_CHAN", "Bảng lụa có chân", new BigDecimal("650000"), "Bảng lụa có chân (khoán 650k)");
        saveConfigIfMissing("KHAC", "LAMINATION_SERVICE", "Phí cán màng phụ thêm", new BigDecimal("50000"), "Cán màng (+50k/m2)");
    }

    private void saveConfigIfMissing(String category, String key, String name, BigDecimal price, String note) {
        if (pricingConfigurationRepository.findByCategoryCodeAndConfigKeyAndActiveTrue(category, key).isEmpty()) {
            pricingConfigurationRepository.save(new PricingConfiguration(category, key, name, price, BigDecimal.ONE, true, note));
        }
    }
}
