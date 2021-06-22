package com.liberty.system.strategy.agent.impl;

import com.liberty.common.utils.DateUtil;
import com.liberty.common.utils.MailUtil;
import com.liberty.common.web.BaseController;
import com.liberty.system.model.*;
import com.liberty.system.strategy.agent.StrategyAgent;
import com.liberty.system.web.KlineController;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class ReverseCheckStrategyAgent extends StrategyAgent {
    @Override
    public void executeSingle(Currency currency) {
        Stroke lastStroke = Stroke.dao.getLastBeforeDate(currency.getId(), Kline.KLINE_TYPE_M30K, DateUtil.strDate(DateUtil.getDay(currency.getFollowedDate()),"yyyy-MM-dd"));
        Date strokeEndDate = null == lastStroke?null:lastStroke.getEndDate();
        List<Kline> klinesInDateRange = Kline.dao.getByDateRange(currency.getId(), Kline.KLINE_TYPE_M30K, strokeEndDate, getExeDate());
        boolean b = reverseCheck(currency, klinesInDateRange, lastStroke);
        if(b){
            Kline kline = Kline.dao.getByDate(currency.getId(),Kline.KLINE_TYPE_M30K, getExeDate());
            kline.setBosp("0");
            kline.update();
            MailUtil.addCurrency2Buy(getExeDate(),strategy,currency);
        }
    }

    @Override
    public void initInStrategyCurrencyList() {
        inStrategyCurrencyList = Currency.dao.listFollowed();
    }

    @Override
    public void initStrategy() {
        strategy = Strategy.dao.findById(12);
    }

    /**
     * 移除开始时间在标记日期前的笔
     * @param strokes
     * @param followedDate
     */
    private void removeStroke(List<Stroke> strokes,Date followedDate){
        Iterator<Stroke> iterator = strokes.iterator();
        while (iterator.hasNext()){
            Stroke next = iterator.next();
            if(next.getStartDate().compareTo(followedDate)<0){
                iterator.remove();
            }
        }
    }

    private boolean reverseCheck(Currency currency, List<Kline> klines, Stroke stroke) {
        List<Kline> sourceKlines = new ArrayList<>();
        sourceKlines.addAll(klines);
        // 最新的K线必须是绿柱,且最新的K线绿柱必须比次新的短
        if (klines.size()>=2 && klines.get(klines.size() - 1).getBar() < 0 && klines.get(klines.size()-2).getBar()<klines.get(klines.size() - 1).getBar()) {
            KlineController klineController = new KlineController();
            klineController.handleInclude(klines, stroke);
            List<Shape> shapes = klineController.handleShapes(klines, stroke,false);
            List<Stroke> strokes = klineController.handleStrokes(shapes, currency.getId(), klines.get(0).getType());
            removeStroke(strokes,currency.getFollowedDate());
            // 情况1:下上两笔,最后一笔还在生成中或已经生成
            if (strokes.size() == 2 || strokes.size()==3) {
                int part1StartIndex = 0;
                int part1EndIndex = getDateIndex(sourceKlines, strokes.get(0).getEndDate());
                int part2StartIndex = getDateIndex(sourceKlines, strokes.get(1).getEndDate());
                int part2EndIndex = sourceKlines.size() - 1;
                // 此情况下,向下笔的最高点必须高于向上笔的最高点且当前K线最低点必须低于向上笔的最低点
                if (strokes.get(0).getMax()>strokes.get(1).getMax()&& sourceKlines.get(part2EndIndex).getMin()<strokes.get(1).getMin()) {
                    // 比较绿柱面积
                    return barCompare(sourceKlines, part1StartIndex, part1EndIndex, part2StartIndex, part2EndIndex);
                }
            }
            // 情况2:下 上下上 下,最后一笔还在生成中或已经生成
            if(strokes.size() == 4 || strokes.size() == 5){
                int part1StartIndex = 0;
                int part1EndIndex = getDateIndex(sourceKlines, strokes.get(0).getEndDate());
                int part2StartIndex = getDateIndex(sourceKlines, strokes.get(3).getEndDate());
                int part2EndIndex = sourceKlines.size() - 1;
                // 此情况下,第一笔的最高点必须高于后面上下上三笔的最高点且当前K线最低点必须低于上下上三笔的最低点且其中上下上中的第一笔上的最低应该低于第二笔上的最高
                if(strokes.get(0).getMax()>strokes.get(1).getMax()&&strokes.get(0).getMax()>strokes.get(3).getMax()&&sourceKlines.get(part2EndIndex).getMin()<strokes.get(1).getMin()&&sourceKlines.get(part2EndIndex).getMin()<strokes.get(3).getMin()&&strokes.get(1).getMin()<strokes.get(3).getMax()){
                    // 比较绿柱面积
                    return barCompare(sourceKlines, part1StartIndex, part1EndIndex, part2StartIndex, part2EndIndex);
                }
            }
            // 情况3:下上下 上下上 下,最后一笔还在生成中或已经生成
            if(strokes.size() == 6 || strokes.size() == 7){
                int part1StartIndex = 0;
                int part1EndIndex = getDateIndex(sourceKlines, strokes.get(2).getEndDate());
                int part2StartIndex = getDateIndex(sourceKlines, strokes.get(5).getEndDate());
                int part2EndIndex = sourceKlines.size() - 1;
                // 此情况下,第一笔下的最低点必须高于中间三笔的最高点且当前K线的最低点必须低于中间三笔的最低点
                if(strokes.get(0).getMin()>strokes.get(3).getMax()&&strokes.get(0).getMin()>strokes.get(5).getMax()&&sourceKlines.get(part2EndIndex).getMin()<strokes.get(3).getMin()&&sourceKlines.get(part2EndIndex).getMin()<strokes.get(5).getMin()){
                    // 比较绿柱面积
                    return barCompare(sourceKlines, part1StartIndex, part1EndIndex, part2StartIndex, part2EndIndex);
                }
            }
        }
        return false;
    }

    /**
     * 比较绿柱面积,前一部分>后一部分的两倍,表示背驰
     * @param klines
     * @param part1StartIndex
     * @param part1EndIndex
     * @param part2StartIndex
     * @param part2EndIndex
     * @return
     */
    private boolean barCompare(List<Kline> klines, int part1StartIndex, int part1EndIndex, int part2StartIndex, int part2EndIndex) {
        // 计算前一部分的绿柱面积
        int barStartIndex1 = getBarStartIndex(klines, part1StartIndex, part1EndIndex);
        int barEndIndex1 = getBarEndIndex(klines, part1EndIndex, part2StartIndex);
        double result1 = calBar(klines, barStartIndex1, barEndIndex1);
        int barStartIndex2 = getBarStartIndex(klines, part2StartIndex, part2EndIndex);
        double result2 = calBar(klines, barStartIndex2, part2EndIndex);
        return result1 > result2*2;
    }

    /**
     * 计算绿柱面积,返回正值
     * @param klines
     * @param barStartIndex
     * @param barEndIndex
     * @return
     */
    private double calBar(List<Kline> klines,int barStartIndex,int barEndIndex){
        double sum = 0.0;
        for (int i = barStartIndex; i <= barEndIndex; i++) {
            if(klines.get(i).getBar()<0){
                sum += klines.get(i).getBar();
            }
        }
        return -sum;
    }

    /**
     * 获取part1部分计算绿柱的起始下标
     * @param klines
     * @param part1StartIndex
     * @param part1EndIndex
     * @return
     */
    private int getBarStartIndex(List<Kline> klines,int part1StartIndex,int part1EndIndex){
        boolean reverseFlag = klines.get(0).getBar()>0;
        int a = -1, b = -1;
        double barMax = -100000.0;
        for (int i = part1EndIndex; i >= part1StartIndex+1; i--) {
            if (klines.get(i).getBar()*klines.get(i-1).getBar() <= 0) {
                reverseFlag = !reverseFlag;
                if(reverseFlag){
                    a = i;
                    break;
                }
            }
            if (klines.get(i).getBar() > barMax) {
                barMax = klines.get(i).getBar();
                b = i;
            }
        }
        int part1BarStartIndex = -1!= a ? a : b;
        return part1BarStartIndex;
    }

    /**
     * 获取part1部分计算绿柱的起始下标
     * @param klines
     * @param part1EndIndex
     * @param part2StartIndex
     * @return
     */
    private int getBarEndIndex(List<Kline> klines,int part1EndIndex,int part2StartIndex){
        int a = -1, b = -1;
        double barMax = -100000.0;
        for (int i = part1EndIndex; i <= part2StartIndex; i++) {
            if (klines.get(i).getBar() > 0) {
                a = i-1;
                break;
            }
            if (klines.get(i).getBar() > barMax) {
                barMax = klines.get(i).getBar();
                b = i;
            }
        }
        int part1BarEndIndex = -1!= a ? a : b;
        return part1BarEndIndex;
    }

    /**
     * 找到日期在K线集合中的下标值
     *
     * @param klines
     * @param date
     * @return
     */
    private int getDateIndex(List<Kline> klines, Date date) {
        for (int i = 0; i < klines.size(); i++) {
            if (klines.get(i).getDate().compareTo(date) == 0) {
                return i;
            }
        }
        return -1;
    }


}
