package com.healthydiet.system;

import com.healthydiet.system.service.FoodImportRuleEngine;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FoodImportRuleEngineTests {

    @Test
    void shouldParseFirstNumberFromMixedText() {
        assertEquals(new BigDecimal("78.9"), FoodImportRuleEngine.parseFirstNumber("78.9 0.5"));
    }

    @Test
    void shouldNotMarkWholeFruitAsHighSugar() {
        String category = FoodImportRuleEngine.inferCategory("香蕉");
        assertEquals("水果", category);
        assertEquals(0, FoodImportRuleEngine.inferHighSugar("香蕉", category, new BigDecimal("22.00")));
    }

    @Test
    void shouldNotMarkSkinlessChickenLegAsHighFat() {
        String category = FoodImportRuleEngine.inferCategory("鸡腿肉去皮");
        assertEquals("蛋白质", category);
        assertEquals(0, FoodImportRuleEngine.inferHighFat(category, new BigDecimal("11.00")));
    }

    @Test
    void shouldClassifyTeaEggAsProtein() {
        assertEquals("蛋白质", FoodImportRuleEngine.inferCategory("茶叶蛋"));
    }

    @Test
    void shouldClassifySoyMilkAsDrink() {
        assertEquals("饮品", FoodImportRuleEngine.inferCategory("无糖豆浆"));
    }

    @Test
    void shouldClassifyCakeAsSnack() {
        assertEquals("零食", FoodImportRuleEngine.inferCategory("鸡蛋糕"));
    }

    @Test
    void shouldClassifyPumpkinAsVegetable() {
        assertEquals("蔬菜", FoodImportRuleEngine.inferCategory("南瓜"));
    }

    @Test
    void shouldClassifyBeefNoodleAsStaple() {
        assertEquals("主食", FoodImportRuleEngine.inferCategory("牛肉面"));
    }
}
