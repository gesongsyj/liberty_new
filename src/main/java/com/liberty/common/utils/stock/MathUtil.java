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
        double dispersionDegreeLimit = 0.005;
        return lineFittingCheck(inputData, lsmParam, kLimit, dispersionDegreeLimit);
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
     * 两段直线拟合判断
     *
     * @return
     */
    public static boolean doubleKlineFittingCheck(List<Kline> inputData, double dispersionDegreeLimit) {
        double sigmaMin = Double.MAX_VALUE;
        List<Double> xData = new ArrayList<>(inputData.size() * 2);
        List<Double> yData = new ArrayList<>(inputData.size() * 2);
        for (int i = 0; i < inputData.size(); i++) {
            xData.add(i * 1.0);
            xData.add(i * 1.0);
            yData.add(inputData.get(i).getMax());
            yData.add(inputData.get(i).getMin());
        }
        for (int i = 1; i < inputData.size(); i++) {
            List<Double> xPart1 = xData.subList(0, (i + 1) * 2);
            List<Double> xPart2 = xData.subList(i * 2, inputData.size() * 2);
            List<Double> yPart1 = yData.subList(0, (i + 1) * 2);
            List<Double> yPart2 = yData.subList(i * 2, inputData.size() * 2);
            normalization(yPart1);
            normalization(yPart2);
            // 最小二乘法计算alpha和beta值
            LsmParam lsmParam1 = MathUtil.lsmCal(xPart1, yPart1);
            LsmParam lsmParam2 = MathUtil.lsmCal(xPart2, yPart2);
            double sigma1 = sigmaCal(xPart1, yPart1, lsmParam1);
            double sigma2 = sigmaCal(xPart2, yPart2, lsmParam2);
            double sigma = (sigma1 * (i + 1) * 2 + sigma2 * (inputData.size() - i) * 2) / ((inputData.size() + 1) * 2);
            if (sigma < sigmaMin) {
                sigmaMin = sigma;
            }
        }
        return sigmaMin <= dispersionDegreeLimit;
    }

    /**
     * 偏离程度计算
     *
     * @return
     */
    public static double sigmaCal(List<Double> inputData, LsmParam lsmParam) {
        double sumP = 0;
        int size = inputData.size();
        for (int i = 0; i < size; i++) {
            sumP += Math.abs(inputData.get(i) - (lsmParam.getAlpha() + lsmParam.getBeta() * i)) / (lsmParam.getAlpha() + lsmParam.getBeta() * i);
        }
        double result = sumP / size;
        return result;
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
            double sum0 = Math.abs(yList.get(i) - (lsmParam.getAlpha() + lsmParam.getBeta() * xList.get(i))) / (lsmParam.getAlpha() + lsmParam.getBeta() * xList.get(i));
            sumP += sum0;
        }
        double result = sumP / size;
        return result;
    }

    private static double avgCal(List<Double> inputData) {
        double sum = 0;
        for (int i = 0; i < inputData.size(); i++) {
            sum += inputData.get(i);
        }
        return sum / inputData.size();
    }
}
