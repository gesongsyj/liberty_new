package com.liberty.common.utils;

import com.liberty.system.model.Kline;

import java.util.ArrayList;
import java.util.List;

/**
 * 移动平均线计算工具
 */
public class MaUtil {
    
    private MaUtil(){
        
    }

    /**
     * 根据传入的K线数据和移动平均数周期,计算移动平均线数据
     * @param data K线集合正序排列
     * @param dayCount
     * @return
     */
    public static List<Double> calculateMA(List<Kline> data,int dayCount){
        List<Double> result = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            if(i<dayCount){
                continue;
            }
            double sum = 0;
            for (int j = 0; j < dayCount; j++) {
                sum+=data.get(i-j).getClose();
            }
            result.add(sum/dayCount);
        }
        return result;
    }

    /**
     * 根据传入的K线数据和移动平均线周期,计算某个点的移动平均线的值
     * @param data K线集合倒叙排列
     * @param dayCount
     * @return
     */
    public static Double calculateMAPoint(List<Kline> data, int dayCount){
        // 默认传入的data的size和dayCount的值相等
        if(data.size()!=dayCount){
            return null;
        }
        double sum = 0;
        for (int i = 0; i < dayCount; i++) {
            sum+=data.get(i).getClose();
        }
        return sum/dayCount;
    }
			
}
