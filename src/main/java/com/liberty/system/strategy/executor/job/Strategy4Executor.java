package com.liberty.system.strategy.executor.job;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfplugin.mail.MailKit;
import com.liberty.common.utils.*;
import com.liberty.common.utils.stock.MaUtil;
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
 * 前一笔的最低点在250日均线下,目前股价上穿站稳250日均线
 */
public class Strategy4Executor extends StrategyExecutor implements Executor {
    // 上穿250日均线的笔涨幅达标阈值
    public static final double STROKE_GAIN_LIMIT = 0.2;

    public Strategy4Executor() {
        this.strategy = Strategy.dao.findById(4);
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
            MailUtil.sendMailToBuy(stayCurrency, super.getStrategy());
        }
        System.out.println("策略4执行完毕!");
        long end = System.currentTimeMillis();
        double time = (end - start) * 1.0 / 1000 / 60;
        MailKit.send("530256489@qq.com", null, "策略[" + strategy.getDescribe() + "]执行耗时提醒!", "此次策略执行耗时:" + time + "分钟!");
        return stayCurrency;
    }

    @Override
    public boolean executeSingle(Currency currency) {
        // 获取最后一笔
        Stroke last1ByCode = Stroke.dao.getLastByCode(currency.getCode(), Kline.KLINE_TYPE_K);
        if(null == last1ByCode){
            return false;
        }
        // 最后一笔不能向上
        if(Stroke.STROKE_TYPE_UP.equals(last1ByCode.getDirection())){
            return false;
        }

        // 计算移动平均值
        int dayCount = 250;
        List<Kline> klinesOfLast1Stroke = Kline.dao.listBeforeDate(currency.getCode(), Kline.KLINE_TYPE_K, last1ByCode.getEndDate(), dayCount);
        if (klinesOfLast1Stroke.size() < 250) {
            return false;
        }
        // 得到计算的移动平均线的值
        Double maPointOfLast1Stroke = MaUtil.calculateMAPoint(klinesOfLast1Stroke, dayCount);
        // 最后一笔的最小值必须在移动平均值之下
        if(last1ByCode.getMin()>maPointOfLast1Stroke){
            return false;
        }

        // 当前K线
        Kline last1 = Kline.dao.getLastOneByCode(currency.getCode(), Kline.KLINE_TYPE_K);
        if(last1.getDiff()<0 || last1.getDea()<0){
            return false;
        }
        List<Kline> klines = Kline.dao.listBeforeDate(currency.getCode(), Kline.KLINE_TYPE_K, last1.getDate(), dayCount);
        // 得到计算的移动平均线的值
        Double maPointOfKlines = MaUtil.calculateMAPoint(klines, dayCount);
        // 当前K线的最高点必须在移动平均值之上
        if(last1.getMax()<maPointOfKlines){
            return false;
        }

        return true;
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
