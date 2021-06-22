package com.liberty.system.strategy.executor.job;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfplugin.mail.MailKit;
import com.liberty.common.utils.MailUtil;
import com.liberty.system.blackHouse.RemoveStrategyBh;
import com.liberty.system.model.Currency;
import com.liberty.system.model.Kline;
import com.liberty.system.model.Strategy;
import com.liberty.system.model.Stroke;
import com.liberty.system.strategy.executor.Executor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * 妖股第二介入点
 */
public class Strategy7Executor extends StrategyExecutor implements Executor {

    // 涨停阈值
    public static final double LIMIT_UP_VALUE = 0.09;
    // 换手率总量阈值
    public static final double TURNOVERRATESUM_LIMIT = 0.6;

    public Strategy7Executor() {
        this.strategy = Strategy.dao.findById(7);
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
                    if (successStrategy(currency)) {
                        stayCurrency.add(currency);
                    }
                }
//				else {
//					不自动从策略组中剔除,自动剔除容易错过符合条件的股票
//					Record record = Db.findFirst("select * from currency_strategy where cutLine is not null and currencyId=? and strategyId=?",
//							currency.getId(), strategy.getId());
//					if(record!=null) {
//						Db.delete("currency_strategy",record);
//					}
//				}
            }
        }
        if (stayCurrency.size() != 0) {
            try {
                MailUtil.sendMailToBuy(stayCurrency, super.getStrategy());
            } catch (Exception e) {
                System.out.println("邮件发送失败!");
                e.printStackTrace();
            }
        }
        System.out.println("策略7执行完毕!");
        long end = System.currentTimeMillis();
        double time = (end - start) * 1.0 / 1000 / 60;
        try {
            MailKit.send("530256489@qq.com", null, "策略[" + strategy.getDescribe() + "]执行耗时提醒!", "此次策略执行耗时:" + time + "分钟!");
        } catch (Exception e) {
            System.out.println("邮件发送失败!");
            e.printStackTrace();
        }
        return stayCurrency;
    }

    @Override
    public boolean executeSingle(Currency currency) {
        Stroke last1Stroke = Stroke.dao.getLastByCurrencyId(currency.getId(), Kline.KLINE_TYPE_K);
        // 最后一笔方向向上
        if (null == last1Stroke || Stroke.STROKE_TYPE_DOWN.equals(last1Stroke.getDirection())) {
            return false;
        }
        List<Kline> klinesByDateRange = Kline.dao.getByDateRange(currency.getId(), Kline.KLINE_TYPE_K, last1Stroke.getStartDate(), last1Stroke.getEndDate());
        // 笔中有涨停K线
        if (!limitUpCheck(klinesByDateRange)) {
            return false;
        }
        List<Kline> klinesAfter = Kline.dao.getByDateRange(currency.getId(), Kline.KLINE_TYPE_K, last1Stroke.getEndDate(), new Date());
        double turnoverRateSum = calcTurnoverRate(klinesAfter);
        // 换手率总量需大于阈值
        if (turnoverRateSum < TURNOVERRATESUM_LIMIT) {
            return false;
        }
        return true;
    }

    // 判断K线集合中是否要涨停K线
    private boolean limitUpCheck(List<Kline> klines) {
        for (Kline kline : klines) {
            if (kline.getAoi() > LIMIT_UP_VALUE) {
                return true;
            }
        }
        return false;
    }

    // 计算K线集合的换手率总量
    private double calcTurnoverRate(List<Kline> klines) {
        double turnoverRateSum = 0;
        for (Kline kline : klines) {
            turnoverRateSum += kline.getTurnoverRate();
        }
        return turnoverRateSum;
    }

    /**
     * 满足策略,判断记录是否存在,执行不同的操作
     *
     * @param currency
     * @return
     */
    public boolean successStrategy(Currency currency) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Record record = Db.findFirst("select * from currency_strategy where currencyId=? and strategyId=?",
                currency.getId(), this.strategy.getId());
        if (record == null) {
            record = new Record().set("currencyId", currency.getId()).set("strategyId", this.strategy.getId())
                    .set("startDate", format.format(new Date()));
            Db.save("currency_strategy", record);
            return true;
        } else {
            record.set("startDate", format.format(new Date()));
            Db.update("currency_strategy", record);
            // 如果已经存在该条记录,只是做更新时间的处理
            return false;
        }
    }
}
