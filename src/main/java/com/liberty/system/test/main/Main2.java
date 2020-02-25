package com.liberty.system.test.main;
import java.util.*;

/**
 * 进制转换
 */
public class Main2 {
    public static void main(String[] args){
        Scanner s = new Scanner(System.in);
        while (s.hasNext()){
            String str = s.nextLine();
            String substring = str.substring(2);
            long l = parse(substring);
            System.out.println(l);
        }
    }

    public static long parse(String inStr){
        long sum = 0;
        for (int i = inStr.length()-1; i >=0; i--) {
            sum += Long.parseLong(inStr.substring(i,i+1),16)*Math.pow(16,(inStr.length()-1-i));
        }
        return sum;
    }

}