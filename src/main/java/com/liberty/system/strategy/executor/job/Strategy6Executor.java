package com.liberty.system.strategy.executor.job;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfplugin.mail.MailKit;
import com.liberty.common.utils.stock.MaUtil;
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
 * 第二类买点自己判断,此处找出满足条件的位置:在250日均线下
 */
public class Strategy6Executor extends StrategyExecutor implements Executor {
    // 上穿250日均线的笔涨幅达标阈值
    public static final double STROKE_GAIN_LIMIT = 0.2;

    public Strategy6Executor() {
        this.strategy = Strategy.dao.findById(6);
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
        System.out.println("策略6执行完毕!");
        long end = System.currentTimeMillis();
        double time = (end - start) * 1.0 / 1000 / 60;
        MailKit.send("530256489@qq.com", null, "策略[" + strategy.getDescribe() + "]执行耗时提醒!", "此次策略执行耗时:" + time + "分钟!");
        return stayCurrency;
    }

    @Override
    public boolean executeSingle(Currency currency) {
        // 获取最近的三笔(下上下),倒序
        List<Stroke> last3strokes = Stroke.dao.getLastSomeByCurrencyId(currency.getId(), Kline.KLINE_TYPE_K, 3);
        if (last3strokes.size() < 3) {
            return false;
        }
        // 其中的上笔方向判断
        if (Stroke.STROKE_TYPE_DOWN.equals(last3strokes.get(1).getDirection())) {
            return false;
        }

        // 计算向上笔起始处的移动平均值
        int dayCount = 250;
        List<Kline> klinesOfLast2thStrokeStart = Kline.dao.listBeforeDate(currency.getId(), Kline.KLINE_TYPE_K, last3strokes.get(1).getStartDate(), dayCount);
        if (klinesOfLast2thStrokeStart.size() < 250) {
            return false;
        }
        // 得到计算的移动平均线的值
        Double maPointOfLast2thStrokeStart = MaUtil.calculateMAPoint(klinesOfLast2thStrokeStart, dayCount);
        // 计算向上笔结束处的移动平均值
        List<Kline> klinesOfLast2thStrokeEnd = Kline.dao.listBeforeDate(currency.getId(), Kline.KLINE_TYPE_K, last3strokes.get(1).getEndDate(), dayCount);
        // 得到计算的移动平均线的值
        Double maPointOfLast2thStrokeEnd = MaUtil.calculateMAPoint(klinesOfLast2thStrokeEnd, dayCount);

        // 向上笔必须上穿移动平均线
        if (last3strokes.get(1).getMin() > maPointOfLast2thStrokeStart || last3strokes.get(1).getMax()<maPointOfLast2thStrokeEnd) {
            return false;
        }
        // 向上笔的涨幅不能小于阈值
        if((last3strokes.get(1).getMax()-last3strokes.get(1).getMin())/last3strokes.get(1).getMin()<STROKE_GAIN_LIMIT){
            return false;
        }

        // 第一笔下的最高点需在250日均线下
        // 计算向上笔起始处的移动平均值
        List<Kline> klinesOfLast3thStrokeStart = Kline.dao.listBeforeDate(currency.getId(), Kline.KLINE_TYPE_K, last3strokes.get(2).getStartDate(), dayCount);
        if (klinesOfLast3thStrokeStart.size() < 250) {
            return false;
        }
        // 得到计算的移动平均线的值
        Double maPointOfLast3thStrokeStart = MaUtil.calculateMAPoint(klinesOfLast3thStrokeStart, dayCount);
        if(last3strokes.get(2).getMax()>maPointOfLast3thStrokeStart){
            return false;
        }

        // 第二笔下的最低点必须高于向上笔的最低点
        if(last3strokes.get(0).getMin()<last3strokes.get(1).getMin()){
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
