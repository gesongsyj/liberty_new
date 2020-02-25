package com.liberty.system.test.main;

import java.util.*;

/**
 * 字符个数统计
 */
public class Main10 {
    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        String in = s.nextLine();
        int result =0;
        List<Character> list = new ArrayList<>();
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            Character character = Character.valueOf(c);
            if(!list.contains(character)){
                list.add(character);
                Integer ret = Integer.valueOf(c);
                if(ret>=0 && ret<=127){
                    result+=1;
                }
            }
        }
        System.out.println(result);
    }
}
