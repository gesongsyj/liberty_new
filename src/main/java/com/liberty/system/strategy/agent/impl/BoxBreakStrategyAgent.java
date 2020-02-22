package com.liberty.system.strategy.agent.impl;

import com.liberty.common.utils.DateUtil;
import com.liberty.common.utils.MaUtil;
import com.liberty.common.utils.MailUtil;
import com.liberty.common.utils.MathUtil;
import com.liberty.system.bean.common.LsmParam;
import com.liberty.system.model.Currency;
import com.liberty.system.model.Kline;
import com.liberty.system.model.Strategy;
import com.liberty.system.model.Stroke;
import com.liberty.system.strategy.agent.StrategyAgent;

import java.util.*;

public class BoxBreakStrategyAgent extends StrategyAgent {
    // 笔的极值拟合直线斜率绝对值相差阈值
    private static final double K_DIFF_LIMIT = 0.003;

    // 笔的极值拟合直线的离散程度阈值
    private static final double DISPERSION_DEGREE_LIMIT = 0.08;

    // 箱体突破限定
    private static final double BOX_UP_LIMIT = 1.1;

    // 箱体上下极值的斜率的阈值
    private static final double K_LIMIT = -0.005;


    @Override
    public void executeSingle(Currency currency) {
        String exeDate = getExeDate().toString();
        List<Stroke> strokes = Stroke.dao.listBeforeByEndDate(currency.getCode(), Kline.KLINE_TYPE_K, getExeDate());
        // 寻找分界点:三笔不重合
//        Date dateLimit = DateUtil.getNext(getExeDate(), Calendar.YEAR, -2);
//        List<Stroke> dividedStrokes = divideStroke(strokes, dateLimit);
        List<Stroke> dividedStrokes = divideStrokeByK(strokes);
        // 集合大小判断
        if(dividedStrokes.size()<6){
            return;
        }
        List<Kline> klines = Kline.dao.getByDateRange(currency.getCode(), Kline.KLINE_TYPE_K, dividedStrokes.get(0).getStartDate(), getExeDate());
        // 笔的极值拟合直线判断----start
        List<Double> maxList = new ArrayList<>();
        List<Double> minList = new ArrayList<>();
        List<Double> maxxList = new ArrayList<>();
        List<Double> minxList = new ArrayList<>();
        for (int i = 0,j=0; i < klines.size()&&j<dividedStrokes.size()+2; i++) {
            if(j == dividedStrokes.size()){
                j=j-1;
            }
            if(klines.get(i).getDate().compareTo(dividedStrokes.get(j).getStartDate())==0){
                if(Stroke.STROKE_TYPE_UP.equals(dividedStrokes.get(j).getDirection())){
                    minxList.add(1.0*(i+1));
                    minList.add(klines.get(i).getMin());
                }
                if(Stroke.STROKE_TYPE_DOWN.equals(dividedStrokes.get(j).getDirection())){
                    maxxList.add(1.0*(i+1));
                    maxList.add(klines.get(i).getMax());
                }
            }
            if(klines.get(i).getDate().compareTo(dividedStrokes.get(j).getEndDate())==0){
                if(Stroke.STROKE_TYPE_UP.equals(dividedStrokes.get(j).getDirection())){
                    maxxList.add(1.0*(i+1));
                    maxList.add(klines.get(i).getMax());
                }
                if(Stroke.STROKE_TYPE_DOWN.equals(dividedStrokes.get(j).getDirection())){
                    minxList.add(1.0*(i+1));
                    minList.add(klines.get(i).getMin());
                }
                j = j+2;
            }

        }
        // 最小二乘法计算alpha和beta值
        LsmParam lsmParam1 = MathUtil.lsmCal(maxList,maxxList);
        LsmParam lsmParam2 = MathUtil.lsmCal(minList,minxList);
        if(lsmParam1.getBeta()<K_LIMIT&&lsmParam2.getBeta()<K_LIMIT){
            return;
        }
        double sigma1 = MathUtil.sigmaCal(maxList,maxxList, lsmParam1);
        double sigma2 = MathUtil.sigmaCal(minList,minxList, lsmParam2);
        if(sigma1>DISPERSION_DEGREE_LIMIT || sigma2>DISPERSION_DEGREE_LIMIT||Math.abs(Math.abs(lsmParam1.getBeta()) - Math.abs(lsmParam2.getBeta())) > K_DIFF_LIMIT){
            return;
        }
        // 笔的极值拟合直线判断----end

        // 当前K线突破箱体判断
        if(klines.get(klines.size()-1).getMax()>=BOX_UP_LIMIT*(lsmParam1.getBeta()*klines.size()+lsmParam1.getAlpha())){
            Kline kline = Kline.dao.getByDate(currency.getCode(),Kline.KLINE_TYPE_K, getExeDate());
            kline.setBosp("0");
            kline.update();
            MailUtil.addCurrency2Buy(getExeDate(),strategy,currency);
        }

        System.out.println("结束!");
    }

    /**
     * 点到直线距离的离散判断
     *
     * @param klines
     * @param lsmParam
     * @return
     */
    private boolean point2LineDistanceCheck(List<Kline> klines, LsmParam lsmParam) {
        double distanceSum = 0;
        for (int i = 0; i < klines.size(); i++) {
            double distance_fz = lsmParam.getBeta() * i - (klines.get(i).getMax() + klines.get(i).getMin()) / 2 + lsmParam.getAlpha();
            distanceSum += distance_fz;
        }
        double result = distanceSum / Math.sqrt(lsmParam.getBeta() * lsmParam.getBeta() + 1) / klines.size();
        return result < 0.1;
    }

    /**
     * 笔的极值拟合直线判断
     *
     * @param strokes
     * @return
     */
    private boolean extremumFittingLineCheck(List<Stroke> strokes) {
        if(strokes.size()<3){
            return false;
        }
        List<Double> maxList = new ArrayList<>();
        List<Double> minList = new ArrayList<>();
        List<Double> xList = new ArrayList<>();
        for (int i = 0; i < strokes.size(); i = i + 2) {
            xList.add(1.0*(i+1));
            maxList.add(strokes.get(i).getMax());
            minList.add(strokes.get(i).getMin());
        }
        // 最小二乘法计算alpha和beta值
        LsmParam lsmParam1 = MathUtil.lsmCal(maxList,xList);
        LsmParam lsmParam2 = MathUtil.lsmCal(minList,xList);
        double sigma1 = MathUtil.sigmaCal(maxList,xList, lsmParam1);
        double sigma2 = MathUtil.sigmaCal(minList,xList, lsmParam2);
        return sigma1 <= DISPERSION_DEGREE_LIMIT && sigma2 <= DISPERSION_DEGREE_LIMIT && Math.abs(Math.abs(lsmParam1.getBeta()) - Math.abs(lsmParam2.getBeta())) < K_DIFF_LIMIT;
    }

    /**
     * 寻找分界点,截取集合
     *
     * @param strokes
     * @return
     */
    private List<Stroke> divideStroke(List<Stroke> strokes, Date dateLimit) {
        for (int i = strokes.size() - 1; i >= 2; i--) {
            int overlap = Stroke.dao.overlap(strokes.get(i - 2), strokes.get(i - 1), strokes.get(i));
            if (strokes.get(i).getStartDate().compareTo(dateLimit) < 0) {
                return Collections.emptyList();
            }
            if (overlap > 0) {
                return strokes.subList(i, strokes.size());
            }
        }
        return Collections.emptyList();
    }

    /**
     * 按极值点斜率来寻找分界点,截取集合
     *
     * @param strokes
     * @return
     */
    private List<Stroke> divideStrokeByK(List<Stroke> strokes) {
        Stroke lastStroke = strokes.get(strokes.size()-1);
        for (int i = strokes.size()-1;i>=0;i--){
            List<Double> mv = new ArrayList<>();
            if(Stroke.STROKE_TYPE_UP.equals(strokes.get(i).getDirection())){
                for (int j = 0; j < 5; j++) {
                    if(i-2*j<0){
                        break;
                    }
                    mv.add(strokes.get(i-2*j).getMax());
                }
            }else{
                for (int j = 0; j < 5; j++) {
                    if(i-2*j<0){
                        break;
                    }
                    mv.add(strokes.get(i-2*j).getMin());
                }
            }
            if(mv.size()<5){
                return Collections.emptyList();
            }
            LsmParam lsmParam = MathUtil.lsmCal(mv);
            if(lsmParam.getBeta()<-0.2){
                return strokes.subList(i,strokes.size());
            }
        }

        return Collections.emptyList();
    }

    @Override
    public void initInStrategyCurrencyList() {
//        inStrategyCurrencyList = Currency.dao.listAll();
//        Currency c1 = Currency.dao.findByCode("002214");
//        Currency c2 = Currency.dao.findByCode("002079");
//        Currency c3 = Currency.dao.findByCode("002459");
        Currency c4 = Currency.dao.findByCode("600667");
//        inStrategyCurrencyList.add(c1);
//        inStrategyCurrencyList.add(c2);
//        inStrategyCurrencyList.add(c3);
        inStrategyCurrencyList.add(c4);
    }

    @Override
    public void initStrategy() {
        strategy = Strategy.dao.findById(13);
    }
}
