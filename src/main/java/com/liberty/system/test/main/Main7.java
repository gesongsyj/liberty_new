package com.liberty.system.test.main;

import java.util.*;

/**
 * 取近似值
 */
public class Main7 {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String inStr = in.nextLine();
        float inFloat = Float.parseFloat(inStr);
        int b = (int) (inFloat+0.5f);
        System.out.println(b);
    }
}
