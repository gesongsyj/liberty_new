package com.liberty.system.test.main;

import java.util.*;

/**
 * 明明的随机数
 */
public class Main4 {
    public static void main(String[] args){
        Scanner s = new Scanner(System.in);
        String cout1 = s.nextLine();
        int c1 = Integer.parseInt(cout1);
        List<Integer> s1 = new ArrayList<Integer>();
        for(int i = 0 ; i <c1;i++){
            String in = s.nextLine();
            int i1 = Integer.parseInt(in);
            if(!s1.contains(i1)){
                s1.add(i1);
            }
        }

        String cout2 = s.nextLine();
        int c2 = Integer.parseInt(cout2);
        List<Integer> s2 = new ArrayList<Integer>();
        for(int i = 0 ; i <c2;i++){
            String in = s.nextLine();
            int i2 = Integer.parseInt(in);
            if(!s2.contains(i2)){
                s2.add(i2);
            }
        }
        Collections.sort(s1);
        Collections.sort(s2);
        for(int i = 0 ; i <s1.size();i++){
            System.out.println(s1.get(i));
        }
        for(int i = 0 ; i <s2.size();i++){
            System.out.println(s2.get(i));
        }

    }
}
