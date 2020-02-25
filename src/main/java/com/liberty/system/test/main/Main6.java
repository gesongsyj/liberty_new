package com.liberty.system.test.main;

import java.util.*;

/**
 * 字符串最后一个单词的长度
 */
public class Main6 {
    public static void main(String[] args){
        Scanner in = new Scanner(System.in);
        String inStr = in.nextLine();
        System.out.println(getLastWordLength(inStr));
    }

    public static int getLastWordLength(String inStr){
        String lastWord;
        if(inStr.contains(" ")){
            lastWord = inStr.substring(inStr.lastIndexOf(" ")+1);
        }else{
            lastWord = inStr;
        }

        return lastWord.length();
    }
}
