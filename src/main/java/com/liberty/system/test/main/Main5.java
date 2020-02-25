package com.liberty.system.test.main;

import java.util.*;

/**
 * 计算字符个数
 */
public class Main5{
    public static void main(String[] args){
        Scanner s = new Scanner(System.in);
        String str = s.nextLine().toLowerCase();
        String c = s.nextLine().toLowerCase();
        int sum = 0;
        for(int i =0 ; i< str.length();i++){
            String ci = str.substring(i,i+1);
            if(c.equals(ci)){
                sum+=1;
            }
        }
        System.out.println(sum);
    }
}
