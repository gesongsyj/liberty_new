package com.liberty.common.utils;

import com.liberty.system.bean.common.LsmParam;
import com.liberty.system.strategy.Judger.filterParam.impl.MaFittingParam;

import java.util.List;

public class MathUtil {

    /**
     * 归零,入参都是正数
     */
    public static void normalization(List<Double> inputData){
        Double data0 = inputData.get(0);
        for (int i = 0; i < inputData.size(); i++) {
            inputData.set(i,inputData.get(i) / data0);
        }
    }

    /**
     * 归零,入参有正有负
     */
    public static void maxNormalization(List<Double> inputData){
        double maxAbs = 0;
        for (int i = 0; i <inputData.size() ; i++) {
            if(Math.abs(inputData.get(i))>maxAbs){
                maxAbs = inputData.get(i);
            }
        }
        Double data0 = inputData.get(0)+maxAbs;
        for (int i = 0; i < inputData.size(); i++) {
            inputData.set(i,inputData.get(i)+maxAbs / data0);
        }
    }

    /**
     * 最小二乘法直线拟合,求解参数
     * @return
     */
    public static LsmParam lsmCal(List<Double> inputData){
        int size = inputData.size();
        double xAvg = (size+1)*size*1.0/2/size;
        double ySum = 0;
        // beta计算公式分子的和
        double betaMolecularSum = 0;
        // beta计算公式分母的和
        double betaDenominatorSum = 0;
        for (int i = 0; i < inputData.size(); i++) {
            ySum += inputData.get(i);
            betaMolecularSum += inputData.get(i)*(i+1-xAvg);
            betaDenominatorSum += ((i+1)*(i+1)-xAvg*xAvg);
        }
        LsmParam lsmParam = new LsmParam();
        lsmParam.setBeta(betaMolecularSum/betaDenominatorSum);
        lsmParam.setAlpha(ySum/size-lsmParam.getBeta()*xAvg);
        return lsmParam;
    }

    /**
     * 最小二乘法直线拟合,求解参数
     * @return
     */
    public static LsmParam lsmCal(List<Double> yList,List<Double> xList){
        int size = yList.size();
        double xAvg = calAvg(xList);
        double ySum = 0;
        // beta计算公式分子的和
        double betaMolecularSum = 0;
        // beta计算公式分母的和
        double betaDenominatorSum = 0;
        for (int i = 0; i < yList.size(); i++) {
            ySum += yList.get(i);
            betaMolecularSum += yList.get(i)*(xList.get(i)-xAvg);
            betaDenominatorSum += (xList.get(i)*xList.get(i)-xAvg*xAvg);
        }
        LsmParam lsmParam = new LsmParam();
        lsmParam.setBeta(betaMolecularSum/betaDenominatorSum);
        lsmParam.setAlpha(ySum/size-lsmParam.getBeta()*xAvg);
        return lsmParam;
    }

    /**
     * 计算集合的平均值
     * @param inputData
     * @return
     */
    private static double calAvg(List<Double> inputData){
        double sum = 0;
        for (int i = 0; i < inputData.size(); i++) {
            sum += inputData.get(i);
        }
        return sum/inputData.size();
    }

    /**
     * 直线拟合判断
     * @return
     */
    public static boolean lineFittingCheck(List<Double> inputData,LsmParam lsmParam){
        /**
         * 需要满足两个条件
         * 1.斜率满足阈值
         * 2.离散程度不超过阈值
         */
        // 斜率阈值
        double kLimit = 0.0015;
        // 离散程度阈值
        double dispersionDegreeLimit = 0.005;
        return lsmParam.getBeta()>=kLimit && sigmaCal(inputData,lsmParam)<=dispersionDegreeLimit;
    }

    /**
     * 直线拟合判断
     * @return
     */
    public static boolean lineFittingCheck(List<Double> inputData, LsmParam lsmParam,double kLimit,double dispersionDegreeLimit){
        /**
         * 需要满足两个条件
         * 1.斜率满足阈值
         * 2.离散程度不超过阈值
         */
        return lsmParam.getBeta()>=kLimit && sigmaCal(inputData,lsmParam)<=dispersionDegreeLimit;
    }

    /**
     * 偏离程度计算
     * @return
     */
    public static double sigmaCal(List<Double> inputData,LsmParam lsmParam){
        double sumP = 0;
        int size = inputData.size();
        for (int i = 0; i < size; i++) {
            sumP += Math.abs(inputData.get(i)-(lsmParam.getAlpha()+lsmParam.getBeta()*i))/(lsmParam.getAlpha()+lsmParam.getBeta()*i);
        }
        double result = sumP/size;
        return result;
    }

    /**
     * 偏离程度计算
     * @return
     */
    public static double sigmaCal(List<Double> yList,List<Double> xList,LsmParam lsmParam){
        double sumP = 0;
        int size = yList.size();
        for (int i = 0; i < size; i++) {
            double sum0 = Math.abs(yList.get(i)-(lsmParam.getAlpha()+lsmParam.getBeta()*xList.get(i)))/(lsmParam.getAlpha()+lsmParam.getBeta()*xList.get(i));
            sumP += sum0;
        }
        double result = sumP/size;
        return result;
    }

    private static double avgCal(List<Double> inputData){
        double sum = 0;
        for (int i = 0; i < inputData.size(); i++) {
            sum += inputData.get(i);
        }
        return sum/inputData.size();
    }
}
