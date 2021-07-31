package com.liberty.common.utils.stock;

import com.liberty.system.bean.common.LsmParam;
import com.liberty.system.model.Kline;
import com.liberty.system.strategy.Judger.filterParam.impl.MaFittingParam;

import java.util.ArrayList;
import java.util.List;

public class MathUtil {

    /**
     * 归零,入参都是正数
     */
    public static void normalization(List<Double> inputData) {
        Double data0 = inputData.get(0);
        for (int i = 0; i < inputData.size(); i++) {
            inputData.set(i, inputData.get(i) / data0);
        }
    }

    /**
     * 归零,入参有正有负
     */
    public static void maxNormalization(List<Double> inputData) {
        double maxAbs = 0;
        for (int i = 0; i < inputData.size(); i++) {
            if (Math.abs(inputData.get(i)) > maxAbs) {
                maxAbs = inputData.get(i);
            }
        }
        Double data0 = inputData.get(0) + maxAbs;
        for (int i = 0; i < inputData.size(); i++) {
            inputData.set(i, inputData.get(i) + maxAbs / data0);
        }
    }

    /**
     * 最小二乘法直线拟合,求解参数
     *
     * @return
     */
    public static LsmParam lsmCal(List<Double> inputData) {
        int size = inputData.size();
        double xAvg = (size - 1) * size * 1.0 / 2 / size;
        double ySum = 0;
        // beta计算公式分子的和
        double betaMolecularSum = 0;
        // beta计算公式分母的和
        double betaDenominatorSum = 0;
        for (int i = 0; i < inputData.size(); i++) {
            ySum += inputData.get(i);
            betaMolecularSum += inputData.get(i) * (i - xAvg);
            betaDenominatorSum += (i * i - xAvg * xAvg);
        }
        LsmParam lsmParam = new LsmParam();
        lsmParam.setBeta(betaMolecularSum / betaDenominatorSum);
        lsmParam.setAlpha(ySum / size - lsmParam.getBeta() * xAvg);
        return lsmParam;
    }

    /**
     * 最小二乘法直线拟合,求解参数
     *
     * @return
     */
    public static LsmParam lsmCal(List<Double> yList, List<Double> xList) {
        int size = yList.size();
        double xAvg = calAvg(xList);
        double ySum = 0;
        // beta计算公式分子的和
        double betaMolecularSum = 0;
        // beta计算公式分母的和
        double betaDenominatorSum = 0;
        for (int i = 0; i < yList.size(); i++) {
            ySum += yList.get(i);
            betaMolecularSum += yList.get(i) * (xList.get(i) - xAvg);
            betaDenominatorSum += (xList.get(i) * xList.get(i) - xAvg * xAvg);
        }
        LsmParam lsmParam = new LsmParam();
        lsmParam.setBeta(betaMolecularSum / betaDenominatorSum);
        lsmParam.setAlpha(ySum / size - lsmParam.getBeta() * xAvg);
        return lsmParam;
    }

    /**
     * 计算集合的平均值
     *
     * @param inputData
     * @return
     */
    private static double calAvg(List<Double> inputData) {
        double sum = 0;
        for (int i = 0; i < inputData.size(); i++) {
            sum += inputData.get(i);
        }
        return sum / inputData.size();
    }

    /**
     * 直线拟合判断
     *
     * @return
     */
    public static boolean lineFittingCheck(List<Double> inputData, LsmParam lsmParam) {
        /**
         * 需要满足两个条件
         * 1.斜率满足阈值
         * 2.离散程度不超过阈值
         */
        // 斜率阈值
        double kLimit = 0.0015;
        // 离散程度阈值
//        double dispersionDegreeLimit = 0.005;
        double fittingLevel = 0.994;
        return lineFittingCheck(inputData, lsmParam, kLimit, fittingLevel);
    }

    /**
     * 直线拟合判断
     *
     * @return
     */
    public static boolean lineFittingCheck(List<Double> inputData, LsmParam lsmParam, double kLimit, double dispersionDegreeLimit) {
        /**
         * 需要满足两个条件
         * 1.斜率满足阈值
         * 2.离散程度不超过阈值
         */
        return lsmParam.getBeta() >= kLimit && sigmaCal(inputData, lsmParam) <= dispersionDegreeLimit;
    }

    /**
     * 直线拟合判断
     *
     * @return
     */
    public static boolean lineFittingCheck_new(List<Double> inputData, LsmParam lsmParam, double kLimit, double fittingLevel) {
        /**
         * 需要满足两个条件
         * 1.斜率满足阈值
         * 2.离散程度不超过阈值
         */
        return lsmParam.getBeta() >= kLimit && fittingLevel(inputData, lsmParam) >= fittingLevel;
    }

    /**
     * 两段直线拟合判断
     *
     * @return
     */
    public static boolean doubleKlineFittingCheck(List<Kline> inputData, double dispersionDegreeLimit) {
        // 仅用min拟合
//        double fittingLevelMax = 0;
        double sigmaMin = Double.MAX_VALUE;
        List<Double> xData = new ArrayList<>(inputData.size());
        List<Double> yData = new ArrayList<>(inputData.size());
        for (int i = 0; i < inputData.size(); i++) {
            xData.add(i * 1.0);
            yData.add(inputData.get(i).getMin());
        }
        for (int i = 1; i < inputData.size(); i++) {
            List<Double> xPart1 = xData.subList(0, i + 1);
            List<Double> xPart2 = xData.subList(i, inputData.size());
            List<Double> yPart1 = new ArrayList<>(yData.subList(0, i + 1));
            List<Double> yPart2 = new ArrayList<>(yData.subList(i, inputData.size()));
            normalization(yPart1);
            normalization(yPart2);
            // 最小二乘法计算alpha和beta值
            LsmParam lsmParam1 = MathUtil.lsmCal(yPart1, xPart1);
            LsmParam lsmParam2 = MathUtil.lsmCal(yPart2, xPart2);
            double sigma1 = sigmaCal(yPart1, xPart1, lsmParam1);
            double sigma2 = sigmaCal(yPart2, xPart2, lsmParam2);
            double sigma = (sigma1 * (i + 1) + sigma2 * (inputData.size() - i)) / (inputData.size() + 1);
            if (sigma < sigmaMin) {
                sigmaMin = sigma;
            }
        }
        return sigmaMin <= dispersionDegreeLimit;
    }

    /**
     * 两段直线拟合判断
     *
     * @return
     */
    public static boolean doubleKlineFittingCheck_new(List<Kline> inputData, double expFittingLevel) {
        // 仅用min拟合
//        double fittingLevelMax = 0;
        double sigmaMin = Double.MAX_VALUE;
        List<Double> xData = new ArrayList<>(inputData.size());
        List<Double> yData = new ArrayList<>(inputData.size());
        for (int i = 0; i < inputData.size(); i++) {
            xData.add(i * 1.0);
            yData.add(inputData.get(i).getMin());
        }
        for (int i = 1; i < inputData.size(); i++) {
            List<Double> xPart1 = xData.subList(0, i + 1);
            List<Double> xPart2 = xData.subList(i, inputData.size());
            List<Double> yPart1 = new ArrayList<>(yData.subList(0, i + 1));
            List<Double> yPart2 = new ArrayList<>(yData.subList(i, inputData.size()));
            normalization(yPart1);
            normalization(yPart2);
            // 最小二乘法计算alpha和beta值
            LsmParam lsmParam1 = MathUtil.lsmCal(yPart1, xPart1);
            LsmParam lsmParam2 = MathUtil.lsmCal(yPart2, xPart2);
            double sigma1 = fittingLevel(yPart1, xPart1, lsmParam1);
            double sigma2 = fittingLevel(yPart2, xPart2, lsmParam2);
            double sigma = (sigma1 * (i + 1) + sigma2 * (inputData.size() - i)) / (inputData.size() + 1);
            if (sigma < sigmaMin) {
                sigmaMin = sigma;
            }
        }
        return sigmaMin >= expFittingLevel;
    }

    /**
     * 偏离程度计算
     *
     * @return
     */
    public static double sigmaCal(List<Double> inputData, LsmParam lsmParam) {
        List<Double> xList = new ArrayList<>(inputData.size());
        for (int i = 0; i < inputData.size(); i++) {
            xList.add(i * 1.0);
        }
        return sigmaCal(inputData,xList,lsmParam);
    }

    /**
     * 偏离程度计算
     *
     * @return
     */
    public static double sigmaCal(List<Double> yList, List<Double> xList, LsmParam lsmParam) {
        double sumP = 0;
        int size = yList.size();
        for (int i = 0; i < size; i++) {
            sumP += Math.abs((yList.get(i) - (lsmParam.getAlpha() + lsmParam.getBeta() * xList.get(i))) / (lsmParam.getAlpha() + lsmParam.getBeta() * xList.get(i)));
        }
        double result = sumP / size;
        return result;
    }


    /**
     * 判断拟合程度,越接近1拟合越好
     *
     * @param inputData
     * @param lsmParam
     * @return
     */
    public static double fittingLevel(List<Double> inputData, LsmParam lsmParam) {
        List<Double> xList = new ArrayList<>(inputData.size());
        for (int i = 0; i < inputData.size(); i++) {
            xList.add(i * 1.0);
        }
        return fittingLevel(inputData, xList, lsmParam);
    }

    /**
     * 判断拟合程度
     *
     * @param yList
     * @param xList
     * @param lsmParam
     * @return
     */
    public static double fittingLevel(List<Double> yList, List<Double> xList, LsmParam lsmParam) {
        // 和方差
        double sse = 0;
        double sst = 0;
        double ySum = 0;
        for (int i = 0; i < yList.size(); i++) {
            ySum += yList.get(i);
        }
        double yAvg = ySum / yList.size();
        for (int i = 0; i < yList.size(); i++) {
            sse += Math.pow(yList.get(i) - (lsmParam.getAlpha() + lsmParam.getBeta() * xList.get(i)), 2);
            sst += Math.pow(yList.get(i) - yAvg, 2);
        }

        double ssr = sst - sse;
        double rSquare = ssr / sst;
        return rSquare;
    }

    private static double avgCal(List<Double> inputData) {
        double sum = 0;
        for (int i = 0; i < inputData.size(); i++) {
            sum += inputData.get(i);
        }
        return sum / inputData.size();
    }
}
