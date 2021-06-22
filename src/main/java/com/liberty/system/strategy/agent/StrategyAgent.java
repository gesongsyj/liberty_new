package com.liberty.system.strategy.agent;

import com.liberty.common.plugins.threadPoolPlugin.ThreadPoolKit;
import com.liberty.common.utils.DateUtil;
import com.liberty.common.utils.MailUtil;
import com.liberty.system.model.Currency;
import com.liberty.system.model.Kline;
import com.liberty.system.model.Strategy;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class StrategyAgent implements AgentSyn {

    // 待执行的股票集合,外部可修改
    protected List<Currency> inStrategyCurrencyList = new ArrayList<>();
    // 关联的策略,外部可修改
    protected Strategy strategy;

    // 初始化日期
    private String initDay;

    // 执行日期
    private Date exeDate = new Date();

    public Date getExeDate() {
        return exeDate;
    }

    public void setExeDate(Date exeDate) {
        this.exeDate = exeDate;
    }

    @Override
    public void execute() {
        long start = System.currentTimeMillis();
        init();
        executeSyn();
        System.out.println("策略执行完毕!");
        long end = System.currentTimeMillis();
        double time = (end - start) * 1.0 / 1000 / 60;
        System.out.println("此次策略执行耗时:" + time + "分钟!");
        MailUtil.sendMailToBuy(getExeDate(),strategy);
    }

    @Override
    public void calibrateCustomize(Date startDate,Date endDate,String klineType) {
        long start = System.currentTimeMillis();
        init();
        List<Kline> klines = Kline.dao.getByDateRange(inStrategyCurrencyList.get(0).getId(), klineType, startDate, endDate);
        Date exeDate;
        for (Kline kline : klines) {
            exeDate = kline.getDate();
            setExeDate(exeDate);
            executeSyn();
            System.out.println("日期:"+exeDate.toLocaleString()+"验证完毕!");
            try {
                MailUtil.sendMailToBuy(getExeDate(),strategy);
            } catch (Exception e) {
                e.printStackTrace();
            }
            exeDate = DateUtil.getNext(exeDate, Calendar.MINUTE, 30);
        }

        System.out.println("验证执行完毕!");
        long end = System.currentTimeMillis();
        double time = (end - start) * 1.0 / 1000 / 60;
        System.out.println("此次验证执行耗时:" + time + "分钟!");
        MailUtil.sendMailToBuy(getExeDate(),strategy);
    }

    @Override
    public void calibrate(Date startDate,Date endDate) {
        long start = System.currentTimeMillis();
        Date exeDate = startDate;
        long dateCount = DateUtil.getNumberBetween(DateUtil.date2Day(startDate), DateUtil.date2Day(endDate), 86400000);
        init();
        for (int i = 0; i < dateCount; i++) {
            exeDate = DateUtil.getNextDay(exeDate);
            setExeDate(exeDate);
            executeSyn();
            System.out.println("日期:"+exeDate.toLocaleString()+"验证完毕!");

            try {
                MailUtil.sendMailToBuy(getExeDate(),strategy);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("验证执行完毕!");
        long end = System.currentTimeMillis();
        double time = (end - start) * 1.0 / 1000 / 60;
        System.out.println("此次验证执行耗时:" + time + "分钟!");
        MailUtil.sendMailToBuy(getExeDate(),strategy);
    }

    public void executeSyn() {
        ThreadPoolExecutor executor = ThreadPoolKit.getExecutor();
        int queueSize = executor.getQueue().remainingCapacity();
        for (int i = 0; i < inStrategyCurrencyList.size(); i++) {
            List<Future> futureList = new ArrayList<>();
            for (int j = 0; j < queueSize && i < inStrategyCurrencyList.size(); j++, i++) {
                int index = i;
                Future<?> future = executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        Currency currency = inStrategyCurrencyList.get(index);
                        executeSingle(currency);
                    }
                });
                futureList.add(future);
                System.out.println("当前线程池信息: \n" + "存活线程数===" + executor.getActiveCount() + ";\n完成任务数===" + executor.getCompletedTaskCount() + ";\n总任务数===" + executor.getTaskCount());
            }
            for (Future future : futureList) {
                try {
                    future.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            i--;
        }
    }

    public abstract void executeSingle(Currency currency);

    // 初始化操作
    public void init() {
        if(!hasInit()){
            initInStrategyCurrencyList();
            initStrategy();
        }
    }

    public abstract void initInStrategyCurrencyList();

    public abstract void initStrategy();

    private boolean hasInit() {
        if (null == initDay || !initDay.equals(DateUtil.getDay())) {
            return false;
        } else {
            initDay = DateUtil.getDay();
            return true;
        }
    }

}
