package com.luoboduner.moo.tool.util;

import java.awt.*;

/**
 * <pre>
 * ColorUtil
 * </pre>
 *
 * @author <a href="https://github.com/rememberber">RememBerBer</a>
 * @since 2019/11/18.
 */
public class ColorUtil {

    /**
     * colorHex to Color
     *
     * @param colorHex
     * @return
     */
    public static Color fromHex(String colorHex) {
        if (colorHex.startsWith("#")) {
            colorHex = colorHex.substring(1);
        }

        if (colorHex.length() == 3) {
            return new Color(17 * Integer.valueOf(String.valueOf(colorHex.charAt(0)), 16), 17 * Integer.valueOf(String.valueOf(colorHex.charAt(1)), 16), 17 * Integer.valueOf(String.valueOf(colorHex.charAt(2)), 16));
        } else if (colorHex.length() == 6) {
            return Color.decode("0x" + colorHex);
        } else {
            throw new IllegalArgumentException("Should be String of 3 or 6 chars length.");
        }
    }

    /**
     * color object to Hex String
     *
     * @param color
     * @return
     */
    public static String toHex(Color color) {
        String r, g, b;
        StringBuilder stringBuilder = new StringBuilder();
        r = Integer.toHexString(color.getRed());
        g = Integer.toHexString(color.getGreen());
        b = Integer.toHexString(color.getBlue());
        r = r.length() == 1 ? "0" + r : r;
        g = g.length() == 1 ? "0" + g : g;
        b = b.length() == 1 ? "0" + b : b;
        stringBuilder.append(r);
        stringBuilder.append(g);
        stringBuilder.append(b);
        return stringBuilder.toString().toUpperCase();
    }

    /**
     * color RGB String to Hex String
     *
     * @param rgb
     * @return
     */
    public static String rgbStrToHex(String rgb) {
        rgb = rgb.replace("，", ",").replace(" ", "");
        String[] rgbArray = rgb.split(",");
        String r, g, b;
        StringBuilder stringBuilder = new StringBuilder();
        r = Integer.toHexString(Integer.parseInt(rgbArray[0]));
        g = Integer.toHexString(Integer.parseInt(rgbArray[1]));
        b = Integer.toHexString(Integer.parseInt(rgbArray[2]));
        r = r.length() == 1 ? "0" + r : r;
        g = g.length() == 1 ? "0" + g : g;
        b = b.length() == 1 ? "0" + b : b;
        stringBuilder.append(r);
        stringBuilder.append(g);
        stringBuilder.append(b);
        return stringBuilder.toString().toUpperCase();
    }

    /**
     * color object to RGB String
     *
     * @param color
     * @return
     */
    public static String toRgbStr(Color color) {
        String r, g, b;
        StringBuilder stringBuilder = new StringBuilder();
        r = String.valueOf(color.getRed());
        g = String.valueOf(color.getGreen());
        b = String.valueOf(color.getBlue());
        stringBuilder.append(r).append(",");
        stringBuilder.append(g).append(",");
        stringBuilder.append(b);
        return stringBuilder.toString().toUpperCase();
    }

}
