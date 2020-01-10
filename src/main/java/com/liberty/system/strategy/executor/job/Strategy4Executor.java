package com.liberty.system.strategy.executor.job;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfplugin.mail.MailKit;
import com.liberty.common.utils.*;
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
        // 获取最近的三笔,倒序
        List<Stroke> last3strokes = Stroke.dao.getLastSomeByCode(currency.getCode(), Kline.KLINE_TYPE_K, 3);
        if (last3strokes.size() < 3) {
            return false;
        }
        // 倒数第三笔方向不能向下
        if (Stroke.STROKE_TYPE_DOWN.equals(last3strokes.get(2).getDirection())) {
            return false;
        }

        // 计算移动平均值
        int dayCount = 250;
        List<Kline> klinesOfStroke = Kline.dao.list250ByDate(currency.getCode(), Kline.KLINE_TYPE_K, last3strokes.get(2).getStartDate(), dayCount);
        if (klinesOfStroke.size() < 250) {
            return false;
        }
        // 得到计算的移动平均线的值
        Double maPointOfStroke = MaUtil.calculateMAPoint(klinesOfStroke, dayCount);
        // 最近一笔最低点必须在移动平均值之下,误差1%
        if (last3strokes.get(2).getMin() > maPointOfStroke * 1.01 || last3strokes.get(2).getMax()<maPointOfStroke) {
            return false;
        }

        // 倒数两笔的上下差距不能过大,应该保持大致相当幅度的震荡,因为是同一个最低点,那么只需要比较最高点即可
        double min = Math.min(last3strokes.get(0).getMax(), last3strokes.get(1).getMax());
        if (Math.abs(last3strokes.get(0).getMax() - last3strokes.get(1).getMax()) / min > 0.02) {
            return false;
        }

        // 计算当前点的移动平均线
        List<Kline> klinesOfToday = Kline.dao.list250ByDate(currency.getCode(), Kline.KLINE_TYPE_K, DateUtil.strDate(DateUtil.getDay(), "yyyy-MM-dd"), dayCount);
        // 得到计算的移动平均线的值
        Double maPointOfToday = MaUtil.calculateMAPoint(klinesOfToday, dayCount);
        Kline last1 = Kline.dao.getLastOneByCode(currency.getCode(), Kline.KLINE_TYPE_K);
        // 当前价必须上穿移动平均线
        if (last1.getMax() < maPointOfToday) {
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
