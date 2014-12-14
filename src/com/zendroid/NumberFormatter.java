package com.zendroid;


public class NumberFormatter {
    public static String formatDouble(double value, int numsAfterComma) {
        if(numsAfterComma > 0) {
            return String.format("%."+numsAfterComma+"f", value);
        } else {
            return null;
        }
    }

}
