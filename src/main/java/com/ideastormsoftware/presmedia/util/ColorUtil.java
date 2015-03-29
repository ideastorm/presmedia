package com.ideastormsoftware.presmedia.util;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {

    private static final Pattern pattern = Pattern.compile("[a-fA-F0-9]+");

    private ColorUtil() {
    }

    public static String colorToHex(Color color) {
        return String.format("#%02X%02X%02X%02X", color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue());
    }

    public static Color hexToColor(String input) {
        String hex = input;
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        Matcher matcher = pattern.matcher(hex);
        if (matcher.matches()) {

            if (hex.length() == 1) {
                int value = Integer.parseInt(hex, 16);
                value += value * 16;
                return new Color(value, value, value);
            } else if (hex.length() == 2) {
                int value = Integer.parseInt(hex, 16);
                return new Color(value, value, value);
            } else if (hex.length() == 3) {
                int r = Integer.parseInt(hex.substring(0, 1), 16);
                int g = Integer.parseInt(hex.substring(1, 2), 16);
                int b = Integer.parseInt(hex.substring(2, 3), 16);
                r += r * 16;
                g += g * 16;
                b += b * 16;
                return new Color(r, g, b);
            } else if (hex.length() == 4) {
                int a = Integer.parseInt(hex.substring(0, 1), 16);
                int r = Integer.parseInt(hex.substring(1, 2), 16);
                int g = Integer.parseInt(hex.substring(2, 3), 16);
                int b = Integer.parseInt(hex.substring(3, 4), 16);
                a += a * 16;
                r += r * 16;
                g += g * 16;
                b += b * 16;
                return new Color(r, g, b, a);
            } else if (hex.length() == 6) {
                int r = Integer.parseInt(hex.substring(0, 2), 16);
                int g = Integer.parseInt(hex.substring(2, 4), 16);
                int b = Integer.parseInt(hex.substring(4, 6), 16);
                return new Color(r, g, b);
            } else if (hex.length() == 8) {
                int a = Integer.parseInt(hex.substring(0, 2), 16);
                int r = Integer.parseInt(hex.substring(2, 4), 16);
                int g = Integer.parseInt(hex.substring(4, 6), 16);
                int b = Integer.parseInt(hex.substring(6, 8), 16);
                return new Color(r, g, b, a);
            }
        }
        throw new NumberFormatException(String.format("%s is not a valid color string", input));
    }
}
