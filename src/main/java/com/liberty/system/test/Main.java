package com.liberty.system.test;

import java.util.*;

public class Main {
    public static void main(String[] args){
        Scanner s = new Scanner(System.in);
        String str1 = s.nextLine();
        String str2 = s.nextLine();
        String[] arr1= work(str1);
        String[] arr2= work(str2);
        for(int i = 0;i<arr1.length;i++){
            System.out.println(arr1[i]);
        }
        for(int i = 0;i<arr2.length;i++){
            System.out.println(arr2[i]);
        }
    }

    public static String[] work(String inStr){
        List<String> result = new ArrayList<>();
        int a = inStr.length()/8;
        a = inStr.length()%8==0?a:a+1;
        for (int i = inStr.length(); i < a*8; i++) {
            inStr= inStr+"0";
        }
        for(int i =0 ;i<a;i++){
//            int endIndex = (i+1)*8<inStr.length()?(i+1)*8:inStr.length();
            String subStr = inStr.substring(i*8,(i+1)*8);
            result.add(subStr);
        }
        String[] strings = new String[result.size()];
        return result.toArray(strings);
    }
}
