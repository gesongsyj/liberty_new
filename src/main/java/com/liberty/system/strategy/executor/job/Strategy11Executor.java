package com.liberty.system.strategy.executor.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.liberty.common.constant.ConstantDefine;
import com.liberty.common.utils.DateUtil;
import com.liberty.system.blackHouse.RemoveStrategyBh;
import com.liberty.system.model.*;
import com.liberty.system.strategy.executor.Executor;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

/**
 * 蓄风雷之势
 */
public class Strategy11Executor extends StrategyExecutor implements Executor {

    public Strategy11Executor() {
        this.strategy = Strategy.dao.findById(11);
    }

    @Override
    public Vector<Currency> execute(String code) {
        long start = System.currentTimeMillis();
        Vector<Currency> stayCurrency = new Vector<>();
        if (code == null) {
            List<Currency> allCurrency = Currency.dao.listAll();
            for (Currency currency : allCurrency) {
                if (RemoveStrategyBh.inBlackHouse(currency)) {// 在小黑屋里面,跳过
                    allCurrency.remove(currency);
                }
            }
            multiProExe(allCurrency, stayCurrency);
        } else {
            Currency currency = Currency.dao.findByCode(code);
            if (!RemoveStrategyBh.inBlackHouse(code)) {// 不在小黑屋里且满足策略
                if (executeSingle(currency)) {
                    if (notExistsRecord(currency)) {
                        stayCurrency.add(currency);
                    }
                }
            }
        }
        sendMailToBuy(stayCurrency, this);
        System.out.println("策略[" + this.getStrategy().getDescribe() + "]执行完毕!");
        long end = System.currentTimeMillis();
        double time = (end - start) * 1.0 / 1000 / 60;
        sendMailTimecost(time);
        return stayCurrency;
    }

    @Override
    public boolean executeSingle(Currency currency) {
        List<Kline> klines = Kline.dao.listAllByCurrencyId(currency.getId(), ConstantDefine.KLINE_TYPE_K);
        if (klines.size() < 20) {
            return false;
        }
        if (!Shape.dao.isLowShape(klines.get(klines.size() - 1), klines.get(klines.size() - 2), klines.get(klines.size() - 3))) {
            return false;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("currency_id", currency.getId());
        boolean isFirstFitting = true;
        int index = 0;
        FittingResult fittingResult = new FittingResult();
        for (int i = klines.size() - 2; i >= 2; i--) {
            if (Shape.dao.isLowShape(klines.get(i - 2), klines.get(i - 1), klines.get(i)) && klines.size() - i >= 20) {
                jsonObject.put("start_date", DateUtil.getDay(klines.get(i - 1).getDate()));
                jsonObject.put("end_date", DateUtil.getDay(klines.get(klines.size() - 1).getDate()));
                jsonObject.put("expect_fitting", 0.4);
                if (isFirstFitting) {
                    // 第一次拟合就不满足条件
                    fittingResult = curve_fitting(jsonObject.toJSONString());
                    if (!fittingResult.isFitting()) {
                        return false;
                    } else {
                        index = i - 1;
                    }
                    isFirstFitting = false;
                } else {
                    fittingResult = curve_fitting(jsonObject.toJSONString());
                    if (!fittingResult.isFitting()) {
                        break;
                    } else {
                        index = i - 1;
                    }
                }
            }
            if (i == 0) {
                return false;
            }
        }
        if (fittingResult.getFitLevel() < 0.5 || fittingResult.getLoopIndex() < 2) {
            return false;
        }
        if (klines.get(index).getMin() > klines.get(klines.size() - 1).getMin()) {
            return false;
        }
        if (index < 7) {
            return false;
        }
        List<Kline> after = klines.subList(index, klines.size());
        List<Kline> before = klines.subList(index - 7, index);


//        List<Stroke> strokes = Stroke.dao.listAllByCurrencyId(currency.getId(), ConstantDefine.KLINE_TYPE_K);
//        if (strokes.size() < 4) {
//            return false;
//        }
//        List<Kline> klinesAfterLastStroke = Kline.dao.getListAfterDate(currency.getId(), ConstantDefine.KLINE_TYPE_K, strokes.get(strokes.size() - 1).getEndDate());
//        // 判断时机
//        if (!checkPoint(klinesAfterLastStroke, strokes.get(strokes.size() - 1))) {
//            return false;
//        }
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("currency_id", currency.getId());
//        jsonObject.put("start_date", DateUtil.getDay(strokes.get(strokes.size() - 4).getStartDate()));
//        jsonObject.put("end_date", DateUtil.getDay(strokes.get(strokes.size() - 1).getEndDate()));
//        jsonObject.put("expect_fitting", 0.4);
//        if (!curve_fitting(jsonObject.toJSONString())) {
//            return false;
//        }
//        int index = 0;
//        if (strokes.size() < 6) {
//            index = strokes.size() - 4;
//            if (strokes.get(index).getMin() > strokes.get(index + 2).getMin()) {
//                return false;
//            }
//        } else {
//            for (int i = strokes.size() - 6; i >= 0; i = i - 2) {
//                jsonObject.put("start_date", DateUtil.getDay(strokes.get(i).getStartDate()));
//                jsonObject.put("end_date", DateUtil.getDay(strokes.get(strokes.size() - 1).getEndDate()));
//                if (!curve_fitting(jsonObject.toJSONString())) {
//                    index = i + 2;
//                    break;
//                }
//            }
//            if (strokes.get(index).getMin() > strokes.get(index + 2).getMin()) {
//                return false;
//            }
//        }
//        List<Kline> after = Kline.dao.getByDateRange(currency.getId(), ConstantDefine.KLINE_TYPE_K, strokes.get(index).getStartDate(), strokes.get(strokes.size() - 1).getEndDate());
//        List<Kline> before = Kline.dao.listBeforeDate(currency.getId(), ConstantDefine.KLINE_TYPE_K, strokes.get(index).getStartDate(), 7);
        if (getVolumeAvg(after) >= getVolumeAvg(before) * 2) {
            return true;
        }
        return false;
    }

    public double getVolumeAvg(List<Kline> klines) {
        double sum = 0;
        for (Kline kline : klines) {
            sum += kline.getVolume();
        }
        return sum / klines.size();
    }

    public static class FittingResult {
        private boolean fitting;
        private double fitLevel;
        private double loopIndex;

        public boolean isFitting() {
            return fitting;
        }

        public void setFitting(boolean fitting) {
            this.fitting = fitting;
        }

        public double getFitLevel() {
            return fitLevel;
        }

        public void setFitLevel(double fitLevel) {
            this.fitLevel = fitLevel;
        }

        public double getLoopIndex() {
            return loopIndex;
        }

        public void setLoopIndex(double loopIndex) {
            this.loopIndex = loopIndex;
        }
    }

    public FittingResult curve_fitting(String msg) {
        Socket socket = null;
        try {
            InetAddress addr = InetAddress.getLocalHost();
            String host = addr.getHostName();
            //String ip=addr.getHostAddress().toString(); //获取本机ip
            //log.info("调用远程接口:host=>"+ip+",port=>"+12345);

            // 初始化套接字，设置访问服务的主机和进程端口号，HOST是访问python进程的主机名称，可以是IP地址或者域名，PORT是python进程绑定的端口号
            socket = new Socket(host, 12345);

            // 获取输出流对象
            OutputStream os = socket.getOutputStream();
            PrintStream out = new PrintStream(os);
            // 发送内容
            out.print(msg);
            // 告诉服务进程，内容发送完毕，可以开始处理
            out.print("over");

            // 获取服务进程的输入流
            InputStream is = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));
            String tmp = null;
            StringBuilder sb = new StringBuilder();
            // 读取内容
            while ((tmp = br.readLine()) != null) {
                sb.append(tmp).append('\n');
            }
            System.out.print(sb);
            FittingResult fittingResult = JSON.parseObject(sb.toString().trim(), FittingResult.class);
            if (fittingResult.isFitting()) {
                System.out.println("满足条件!");
            }
            return fittingResult;
        } catch (IOException e) {
            e.printStackTrace();
            return new FittingResult();
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                return new FittingResult();
            }
            System.out.print("远程接口调用结束.");
        }
    }

}
