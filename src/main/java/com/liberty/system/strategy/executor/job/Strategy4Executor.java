package com.liberty.system.strategy.executor.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfplugin.mail.MailKit;
import com.liberty.common.utils.HTTPUtil;
import com.liberty.common.utils.MaUtil;
import com.liberty.common.utils.MailUtil;
import com.liberty.common.utils.NumUtil;
import com.liberty.system.blackHouse.RemoveStrategyBh;
import com.liberty.system.model.Currency;
import com.liberty.system.model.Kline;
import com.liberty.system.model.Strategy;
import com.liberty.system.model.Stroke;
import com.liberty.system.strategy.executor.Executor;

import java.text.MessageFormat;
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
        System.out.println("策略3执行完毕!");
        long end = System.currentTimeMillis();
        double time = (end - start) * 1.0 / 1000 / 60;
        MailKit.send("530256489@qq.com", null, "策略[" + strategy.getDescribe() + "]执行耗时提醒!", "此次策略执行耗时:" + time + "分钟!");
        return stayCurrency;
    }

    @Override
    public boolean executeSingle(Currency currency) {
        // 最近的一笔
        Stroke stroke = Stroke.dao.getLastByCode(currency.getCode(), Kline.KLINE_TYPE_K);
        // 最近一笔必须是向下笔
        if(stroke.getType() == Stroke.stroke_type_up){
            return false;
        }
        // 计算移动平均值
        int dayCount = 250;
        List<Kline> klines = Kline.dao.list250ByDate(currency.getCode(),Kline.KLINE_TYPE_K,stroke.getEndDate(),dayCount);
        if(klines.size()<250){
            return false;
        }
        // 得到计算的移动平均线的值
        Double maPoint = MaUtil.calculateMAPoint(klines, dayCount);
        // 最近一笔最低点必须在移动平均值之下,误差1%
        if(stroke.getMin()>maPoint*1.01){
            return false;
        }
        List<Kline> last2Klines = Kline.dao.getLast2ByCode(currency.getCode(), Kline.KLINE_TYPE_K);
        // 当前价必须上穿移动平均线
        if(last2Klines.get(0).getMax()<maPoint || last2Klines.get(1).getMax()>=maPoint){
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
