package com.liberty.system.test.main;

import java.util.*;

/**
 * 质数因子
 */
public class Main1 {
    public static void main(String[] args){
        Scanner s = new Scanner(System.in);
        String str = s.nextLine();
        long in = Long.parseLong(str);
        System.out.println(getResult(in));
    }

    public static String getResult(long ulDataInput){
        String result = "";
        if(ulDataInput==1){
            return  result;
        }

        for (long i = 2; i <= ulDataInput ; i++) {
            if(isPrime(i)){
                while(ulDataInput%i==0){
                    result = result+String.valueOf(i)+" ";
                    ulDataInput = ulDataInput/i;
                }
            }
        }
        return result;
    }

    public static boolean isPrime(long in){
        if(in==0 || in == 1){
            return false;
        }
        if(in == 2){
            return true;
        }
        int a = (int) Math.sqrt(in) + 1;
        for (long i = 2; i <=a; i++) {
            if(in % i ==0){
                return false;
            }
        }
        return true;
    }

}
