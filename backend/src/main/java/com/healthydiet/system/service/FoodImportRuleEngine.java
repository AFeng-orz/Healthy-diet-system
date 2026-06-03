package com.healthydiet.system.service;

import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FoodImportRuleEngine {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?\\d+(?:\\.\\d+)?");

    private static final List<String> SNACK_KEYWORDS = List.of(
            "饼干", "蛋糕", "糕点", "月饼", "巧克力", "薯片", "糖果", "冰淇淋", "雪糕", "果脯", "蜜饯", "甜点"
    );
    private static final List<String> OIL_KEYWORDS = List.of(
            "橄榄油", "花生油", "菜籽油", "大豆油", "豆油", "玉米油", "葵花籽油", "芝麻油", "调和油", "黄油", "猪油", "牛油", "奶油"
    );
    private static final List<String> DAIRY_KEYWORDS = List.of(
            "牛奶", "羊奶", "酸奶", "奶酪", "芝士", "干酪", "乳粉", "奶粉", "乳酪"
    );
    private static final List<String> DRINK_KEYWORDS = List.of(
            "饮料", "可乐", "雪碧", "汽水", "茶", "咖啡", "豆浆", "果汁", "奶茶", "啤酒", "葡萄酒"
    );
    private static final List<String> FRUIT_KEYWORDS = List.of(
            "苹果", "香蕉", "橙", "橘", "梨", "桃", "葡萄", "草莓", "蓝莓", "西瓜", "哈密瓜", "芒果", "菠萝", "柚", "猕猴桃", "柠檬", "荔枝", "龙眼", "枣", "牛油果"
    );
    private static final List<String> VEGETABLE_KEYWORDS = List.of(
            "菜", "瓜", "番茄", "西红柿", "黄瓜", "菠菜", "生菜", "白菜", "油菜", "芹菜", "萝卜", "胡萝卜", "西兰花", "花菜", "蘑菇", "香菇", "海带", "木耳", "茄子", "青椒", "冬瓜", "南瓜", "洋葱"
    );
    private static final List<String> NUT_KEYWORDS = List.of(
            "杏仁", "核桃", "花生", "腰果", "榛子", "松子", "开心果", "瓜子", "芝麻", "坚果"
    );
    private static final List<String> STAPLE_KEYWORDS = List.of(
            "米", "面", "麦", "饭", "粥", "粉", "馒头", "面包", "饼", "玉米", "红薯", "紫薯", "土豆", "马铃薯", "藜麦", "荞麦", "高粱", "小米", "谷", "薯", "淀粉"
    );
    private static final List<String> PROTEIN_KEYWORDS = List.of(
            "鸡", "鸭", "鹅", "猪", "牛", "羊", "鱼", "虾", "蟹", "贝", "蛋", "豆腐", "豆干", "肉", "肝", "瘦肉", "火腿"
    );

    private FoodImportRuleEngine() {
    }

    public static String normalizeName(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim().replaceAll("\\s+", "");
    }

    public static BigDecimal parseFirstNumber(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        Matcher matcher = NUMBER_PATTERN.matcher(value.replace(",", ""));
        if (!matcher.find()) {
            return null;
        }
        return new BigDecimal(matcher.group());
    }

    public static String inferCategory(String name) {
        if (!StringUtils.hasText(name)) {
            return "其他";
        }
        if (containsAny(name, OIL_KEYWORDS) || name.endsWith("油")) {
            return "油脂";
        }
        if (containsAny(name, DAIRY_KEYWORDS)) {
            return "乳制品";
        }
        if (containsAny(name, SNACK_KEYWORDS)) {
            return "零食";
        }
        if (containsAny(name, NUT_KEYWORDS)) {
            return "坚果";
        }
        if (containsAny(name, FRUIT_KEYWORDS)) {
            return "水果";
        }
        if (containsAny(name, STAPLE_KEYWORDS)) {
            return "主食";
        }
        if (containsAny(name, PROTEIN_KEYWORDS)) {
            return "蛋白质";
        }
        if (containsAny(name, DRINK_KEYWORDS)) {
            return "饮品";
        }
        if (containsAny(name, VEGETABLE_KEYWORDS)) {
            return "蔬菜";
        }
        return "其他";
    }

    public static String inferMealTags(String name, String category) {
        if (containsAny(name, List.of("牛奶", "酸奶", "面包", "麦片", "燕麦", "豆浆"))) {
            return "早餐,加餐";
        }
        return switch (category) {
            case "主食" -> "早餐,午餐,晚餐";
            case "蛋白质", "蔬菜", "油脂" -> "午餐,晚餐";
            case "水果", "坚果", "乳制品", "饮品", "零食", "补充品" -> "早餐,加餐";
            default -> "午餐,晚餐";
        };
    }

    public static int inferHighSugar(String name, String category, BigDecimal carbs) {
        if (!StringUtils.hasText(name)) {
            return 0;
        }
        if ("水果".equals(category) || "主食".equals(category)) {
            return 0;
        }
        boolean obviousSugar = containsAny(name, List.of("糖", "蜂蜜", "蜜饯", "果脯", "可乐", "雪碧", "奶茶", "甜饮料", "巧克力", "蛋糕"));
        boolean highCarbsSnack = ("饮品".equals(category) || "零食".equals(category)) && carbs != null && carbs.compareTo(new BigDecimal("10")) >= 0;
        return obviousSugar || highCarbsSnack ? 1 : 0;
    }

    public static int inferHighFat(String category, BigDecimal fat) {
        if ("油脂".equals(category) || "坚果".equals(category)) {
            return 1;
        }
        return fat != null && fat.compareTo(new BigDecimal("20")) >= 0 ? 1 : 0;
    }

    public static String buildRemark() {
        return "来自中国食物成分表，数据按每100g计算，分类和标签由系统规则生成";
    }

    private static boolean containsAny(String text, List<String> keywords) {
        return keywords.stream().anyMatch(text::contains);
    }
}
