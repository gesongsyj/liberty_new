package com.liberty.common.utils.stock;

import com.liberty.system.model.Kline;

import java.util.List;

/**
 * 成交量工具类
 */
public class VolumeUtil {

    /**
     * 根据传入的K线数据和周期,计算某个点的平均成交量
     * @param data K线集合倒序排列
     * @param dayCount
     * @return
     */
    public static Double calculateVolumePoint(List<Kline> data, int dayCount){
        // 默认传入的data的size和dayCount的值相等
        if(data.size()!=dayCount){
            return null;
        }
        double sum = 0;
        for (int i = 0; i < dayCount; i++) {
            sum+=null == data.get(i).getVolume()?0.0:data.get(i).getVolume();
        }
        return sum/dayCount;
    }
}
