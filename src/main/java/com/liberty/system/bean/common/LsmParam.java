package com.liberty.system.bean.common;

/**
 * 最小二乘法参数值
 */
public class LsmParam {
    // α参数值
    private double alpha;
    // β参数值,斜率
    private double beta;

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double getBeta() {
        return beta;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }
}
