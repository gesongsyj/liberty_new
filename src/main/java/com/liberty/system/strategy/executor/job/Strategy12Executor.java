package com.liberty.system.strategy.executor.job;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfplugin.mail.MailKit;
import com.liberty.common.constant.ConstantDefine;
import com.liberty.common.utils.MailUtil;
import com.liberty.common.utils.stock.MaUtil;
import com.liberty.common.utils.stock.MathUtil;
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
import java.util.stream.Collectors;

/**
 * 穿越M20
 */
public class Strategy12Executor extends StrategyExecutor implements Executor {

    public Strategy12Executor() {
        this.strategy = Strategy.dao.findById(12);
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
        int dayCount = 20;
        // 先找到起始点,连续多天整个在M20以上
        // 时间升序
        List<Kline> klines = Kline.dao.listAllByCurrencyId(currency.getId(), ConstantDefine.KLINE_TYPE_K);
        if (klines.size() < dayCount) {
            return false;
        }
        int currentIndex = klines.size() - 1;
        int preIndex = klines.size() - 2;
        // 当前要满足的条件
        Kline currentKline = klines.get(currentIndex);
        Kline preKline = klines.get(preIndex);
        Double ma20 = MaUtil.calculateMAPoint(klines.subList(currentIndex + 1 - dayCount, currentIndex + 1), dayCount);
        if (!(currentKline.getMin() <= ma20 * (1 + 0.02) && currentKline.getMax() >= ma20 * (1 + 0.02) && preKline.getMin() > ma20)) {
            return false;
        }
        currentKline.put("ma20", ma20);
        int index = 0;
        // 找上穿点
        for (int i = preIndex; i >= dayCount - 1; i--) {
            ma20 = MaUtil.calculateMAPoint(klines.subList(i + 1 - dayCount, i + 1), dayCount);
            if ((klines.get(i).getMax() + klines.get(i).getMin()) / 2 <= ma20) {
                index = i;
                break;
            }
            klines.get(i).put("ma20", ma20);
        }
        int compareNum = 5;
        if (index + 1 < compareNum + dayCount) {
            return false;
        }
        // 往前要连续5个k在M20以下
        int maxUnderNum = 0;
        for (int i = index; i >= index - compareNum; i--) {
            Double maPoint = MaUtil.calculateMAPoint(klines.subList(i - dayCount, i), dayCount);
            if (klines.get(i).getMin() > maPoint) {
                return false;
            }
            if (klines.get(i).getMax() <= maPoint) {
                maxUnderNum++;
            }
        }
        // compareNum个里面至少大半max在ma20以下
        if (maxUnderNum < compareNum / 2 + 1) {
            return false;
        }

        List<Kline> subList = klines.subList(index + 1, currentIndex + 1);
        List<Double> ma20List = subList.stream().map(e -> Double.parseDouble(e.get("ma20").toString())).collect(Collectors.toList());
        // 进行标准化处理
        MathUtil.normalization(ma20List);
        // 最小二乘法计算alpha和beta值
        LsmParam lsmParam = MathUtil.lsmCal(ma20List);
        // 判断拟合度和斜率是否满足要求
        boolean lineFit = MathUtil.lineFittingCheck(ma20List, lsmParam, 0.0014, 0.012);
        if (!lineFit) {
            return false;
        }
        boolean ret = MathUtil.doubleKlineFittingCheck(subList, 0.4);
        return ret;
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