package com.liberty.common.utils;

public class NumUtil {
    /**
     *
     * @param inputStr 如：1.3亿，469.30万
     * @return
     */
    public static double parseNumFromStr(String inputStr){
        double output;
        if(inputStr.contains("亿")){
            String num = inputStr.replaceAll("亿", "");
            output = Double.valueOf(num)*10000*10000;
        }else if(inputStr.contains("万")){
            String num = inputStr.replaceAll("万", "");
            output = Double.valueOf(num)*10000;
        }else{
            output = Double.valueOf(inputStr);
        }
        return output;
    }

}
