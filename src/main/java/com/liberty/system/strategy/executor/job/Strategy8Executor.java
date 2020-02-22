package com.liberty.system.strategy.executor.job;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfplugin.mail.MailKit;
import com.liberty.common.utils.MaUtil;
import com.liberty.common.utils.MailUtil;
import com.liberty.common.utils.MathUtil;
import com.liberty.system.bean.common.LsmParam;
import com.liberty.system.blackHouse.RemoveStrategyBh;
import com.liberty.system.model.Currency;
import com.liberty.system.model.Kline;
import com.liberty.system.model.Strategy;
import com.liberty.system.model.Stroke;
import com.liberty.system.strategy.executor.Executor;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * 均线拟合直线
 */
public class Strategy8Executor extends StrategyExecutor implements Executor {

    public Strategy8Executor() {
        this.strategy = Strategy.dao.findById(8);
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
        Stroke lastStroke = Stroke.dao.getLastByCode(currency.getCode(), Kline.KLINE_TYPE_K);
        if(!Stroke.STROKE_TYPE_DOWN.equals(lastStroke.getDirection())){
            return false;
        }
        // 判断的样本值
        int count = 50;
        List<Kline> klines = Kline.dao.listAllByCode(currency.getCode(), Kline.KLINE_TYPE_K);
        List<Double> mas = MaUtil.calculateMA(klines, 250);
        if(mas.size()<count){
            return false;
        }
        // 对集合进行翻转
        Collections.reverse(mas);
        // 取前100条记录
        List<Double> masSub = mas.subList(0, count);
        // 在翻转回来
        Collections.reverse(masSub);
        // 进行标准化处理
        MathUtil.normalization(masSub);
        // 最小二乘法计算alpha和beta值
        LsmParam lsmParam = MathUtil.lsmCal(masSub);
        // 判断拟合度和斜率是否满足要求
        boolean b = MathUtil.lineFittingCheck(masSub, lsmParam);
        return b;
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
