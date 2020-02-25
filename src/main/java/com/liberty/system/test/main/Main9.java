package com.liberty.system.test.main;

import java.util.*;

/**
 * 提取不重复的整数
 */
public class Main9 {
    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        String in = s.nextLine();
        List<String> record = new ArrayList<>();
        for (int i = in.length()-1; i >=0 ; i--) {
            String item = in.substring(i, i + 1);
            if(!record.contains(item)){
                record.add(item);
            }
        }
        String ret="";
        for (String str : record) {
            ret = ret + str;
        }
        System.out.println(Integer.parseInt(ret));
    }
}
