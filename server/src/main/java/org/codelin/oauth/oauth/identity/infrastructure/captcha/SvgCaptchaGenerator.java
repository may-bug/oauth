package org.codelin.oauth.oauth.identity.infrastructure.captcha;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * SVG验证码生成器 - 纯SVG实现，兼容GraalVM
 */
@Slf4j
@Component
public class SvgCaptchaGenerator {

    private static final String CHARS = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 生成验证码
     */
    public CaptchaResult generate(CaptchaConfig config) {
        // 生成随机码
        String code = generateCode(config.getLength());

        // 生成SVG
        String svg = buildSvg(code, config);

        // 生成token
        String token = UUID.randomUUID().toString().replace("-", "");

        return new CaptchaResult(svg, token, code);
    }

    /**
     * 生成随机码
     */
    private String generateCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    /**
     * 构建SVG
     */
    private String buildSvg(String code, CaptchaConfig config) {
        int width = 120;
        int height = 40;

        StringBuilder svg = new StringBuilder();
        svg.append(String.format(
                "<svg xmlns='http://www.w3.org/2000/svg' width='%d' height='%d' viewBox='0 0 %d %d'>",
                width, height, width, height));

        // 背景
        svg.append(String.format("<rect width='%d' height='%d' fill='#f0f0f0'/>", width, height));

        // 噪点
        if (config.getNoiseDots() > 0) {
            appendNoiseDots(svg, width, height, config.getNoiseDots());
        }

        // 干扰线
        if (config.getNoiseLines() > 0) {
            appendNoiseLines(svg, width, height, config.getNoiseLines());
        }

        // 字符
        appendCharacters(svg, code, width, height, config);

        svg.append("</svg>");
        return svg.toString();
    }

    /**
     * 添加噪点
     */
    private void appendNoiseDots(StringBuilder svg, int width, int height, int count) {
        for (int i = 0; i < count; i++) {
            int x = RANDOM.nextInt(width);
            int y = RANDOM.nextInt(height);
            String color = getRandomColor();
            svg.append(String.format("<circle cx='%d' cy='%d' r='1' fill='%s'/>", x, y, color));
        }
    }

    /**
     * 添加干扰线
     */
    private void appendNoiseLines(StringBuilder svg, int width, int height, int count) {
        for (int i = 0; i < count; i++) {
            int x1 = RANDOM.nextInt(width);
            int y1 = RANDOM.nextInt(height);
            int x2 = RANDOM.nextInt(width);
            int y2 = RANDOM.nextInt(height);
            String color = getRandomColor();
            svg.append(String.format("<line x1='%d' y1='%d' x2='%d' y2='%d' stroke='%s' stroke-width='1'/>",
                    x1, y1, x2, y2, color));
        }
    }

    /**
     * 添加字符
     */
    private void appendCharacters(StringBuilder svg, String code, int width, int height,
                                   CaptchaConfig config) {
        int charWidth = width / (code.length() + 1);

        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            int x = charWidth * (i + 1);
            int y = height / 2 + RANDOM.nextInt(10) - 5;
            int rotation = config.isRotation() ? RANDOM.nextInt(30) - 15 : 0;
            String color = getRandomColor();

            svg.append(String.format(
                    "<text x='%d' y='%d' font-family='Arial' font-size='%d' fill='%s' " +
                    "transform='rotate(%d,%d,%d)' text-anchor='middle' dominant-baseline='middle'>%c</text>",
                    x, y, 24 + RANDOM.nextInt(8), color, rotation, x, y, c));
        }
    }

    /**
     * 获取随机颜色
     */
    private String getRandomColor() {
        return String.format("#%06x", RANDOM.nextInt(0xFFFFFF));
    }

    /**
     * 验证码结果
     */
    @Data
    public static class CaptchaResult {
        private final String svg;
        private final String token;
        private final String code;
    }

    /**
     * 验证码配置
     */
    @Data
    public static class CaptchaConfig {
        private int length = 4;
        private boolean caseSensitive = false;
        private int noiseLines = 5;
        private int noiseDots = 50;
        private boolean rotation = true;
        private boolean wave = true;
    }
}
