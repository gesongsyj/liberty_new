package com.liberty.system.test.main;

import java.util.*;

/**
 * 合并表记录
 */
public class Main8 {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String s1 = in.nextLine();
        int inSize = Integer.parseInt(s1);
        Map<Integer,Integer> outMap = new LinkedHashMap<>();
        for (int i = 0; i < inSize ; i++) {
            String s = in.nextLine();
            String[] arr = s.split(" ");
            if(outMap.containsKey(Integer.parseInt(arr[0]))){
                outMap.put(Integer.parseInt(arr[0]),outMap.get(Integer.parseInt(arr[0]))+Integer.parseInt(arr[1]));
            }else{
                outMap.put(Integer.parseInt(arr[0]),Integer.parseInt(arr[1]));
            }
        }
        Set<Integer> integers = outMap.keySet();
        List<Integer> keys = new ArrayList<>();
        keys.addAll(integers);
        Collections.sort(keys);
        for (Integer key : keys) {
            System.out.println(key +" "+outMap.get(key));
        }
    }
}
