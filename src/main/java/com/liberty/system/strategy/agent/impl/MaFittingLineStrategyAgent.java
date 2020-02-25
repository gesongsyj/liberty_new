package com.liberty.system.strategy.agent.impl;

import com.liberty.common.utils.stock.MaUtil;
import com.liberty.common.utils.MailUtil;
import com.liberty.common.utils.stock.MathUtil;
import com.liberty.system.bean.common.LsmParam;
import com.liberty.system.model.Currency;
import com.liberty.system.model.Kline;
import com.liberty.system.model.Strategy;
import com.liberty.system.strategy.Judger.filterParam.impl.VolumeParam;
import com.liberty.system.strategy.agent.StrategyAgent;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MaFittingLineStrategyAgent extends StrategyAgent {
    // 均线拟合直线样本数
    private static final int MA_COUNT =10;
    // 均线周期数
    private static final int MA_CYCLE_COUNT = 10;

    private static final int CROSS_DOWN_MA_KLINE_COUNT_LIMIT = 60;

    // 拟合直线的斜率阈值
    private static final double K_LIMIT = 0.4;
    // 离散程度阈值
    private static final double DISPERSION_DEGREE_LIMIT = 0.04;

    @Override
    public void executeSingle(Currency currency) {
        String exeDate = getExeDate().toLocaleString();
        VolumeParam volumeParam = new VolumeParam();
        volumeParam.setVolumeIncreaseTimes(1.5);
        // 当前K线的成交量判断
        boolean judge = volumeParam.judge(currency, getExeDate());
        if(judge){
            // 往前找到下穿均线的K线
            Kline crossDownMaKline = getCrossDownMaKline(currency, getExeDate(), CROSS_DOWN_MA_KLINE_COUNT_LIMIT);
            if(null !=crossDownMaKline){
                // 下穿均线处的K线往前的均线斜率判断
                List<Kline> klines = Kline.dao.listBeforeDate(currency.getCode(), Kline.KLINE_TYPE_K, crossDownMaKline.getDate(), MA_CYCLE_COUNT + MA_COUNT);
                if(klines.size()<MA_COUNT+MA_CYCLE_COUNT){
                    return;
                }
                Collections.reverse(klines);
                List<Double> data = MaUtil.calculateMA(klines, MA_CYCLE_COUNT);
                LsmParam lsmParam = MathUtil.lsmCal(data);
                // 均线拟合直线判断
                boolean b = MathUtil.lineFittingCheck(data, lsmParam, K_LIMIT, DISPERSION_DEGREE_LIMIT);
                if(b){
                    Kline lastKline = Kline.dao.getLastOneByCodeAndDate(currency.getCode(), Kline.KLINE_TYPE_K, getExeDate());
                    // 当前K线是阳线的最低点在下穿均线K线的最高点之下
                    if(lastKline.getClose()>lastKline.getOpen()&&lastKline.getMin()<crossDownMaKline.getMax()){
                        Kline kline = Kline.dao.getByDate(currency.getCode(),Kline.KLINE_TYPE_K, getExeDate());
                        kline.setBosp("0");
                        kline.update();
                        MailUtil.addCurrency2Buy(getExeDate(),strategy,currency);
                    }
                }
            }
        }
    }

    /**
     * 获取下穿ma均线的K线
     * @param currency
     * @param date
     * @param findCount
     * @return
     */
    private Kline getCrossDownMaKline(Currency currency,Date date,int findCount){
        List<Kline> klines = Kline.dao.listBeforeDate(currency.getCode(), Kline.KLINE_TYPE_K, date, findCount+MA_CYCLE_COUNT);
        Collections.reverse(klines);
        List<Double> doubles = MaUtil.calculateMA(klines, MA_CYCLE_COUNT);
        for (int i = doubles.size()-1; i >=1 ; i--) {
            if(klines.get(i+MA_CYCLE_COUNT).getMin()<doubles.get(i)&&klines.get(i+MA_CYCLE_COUNT).getMax()>doubles.get(i)||klines.get(i+MA_CYCLE_COUNT).getMin()<doubles.get(i)&&klines.get(i-1+MA_CYCLE_COUNT).getMax()>doubles.get(i-1)){
                return klines.get(i+MA_CYCLE_COUNT);
            }
        }
        return null;
    }

    @Override
    public void initInStrategyCurrencyList() {
        inStrategyCurrencyList = Currency.dao.listAll();
//        Currency c1 = Currency.dao.findByCode("002912");
//        inStrategyCurrencyList.add(c1);
    }

    @Override
    public void initStrategy() {
        strategy = Strategy.dao.findById(14);
    }
}
