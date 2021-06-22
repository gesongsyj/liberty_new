package com.liberty.system.strategy.calibrator;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.liberty.system.downloader.DownLoader;
import com.liberty.system.downloader.impl.CalibratorDownLoader;
import com.liberty.system.downloader.impl.DfcfDownLoader;
import com.liberty.system.model.Currency;
import com.liberty.system.model.Kline;
import com.liberty.system.model.Line;
import com.liberty.system.model.Stroke;
import com.liberty.system.strategy.executor.Executor;
import com.liberty.system.web.KlineController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class Calibrator {
    private Executor executor;

    public Calibrator(Executor executor) {
        this.executor = executor;
    }

    public void calibrate() {
        List<Currency> currencyList = Currency.dao.listAll();
        for (Currency currency : currencyList) {
            calibrate(currency, null);
        }
    }

    /**
     * 验证
     *
     * @param currency  currency
     * @param startDate 开始验证的时间点,如果为空,则从头开始验证
     */
    public void calibrate(Currency currency, Date startDate) {
        CalibratorDownLoader calibratorDownLoader = new CalibratorDownLoader();
        DownLoader dfcfDownLoader = new DfcfDownLoader();

        Db.tx(() -> {
            Db.delete("DELETE from currency_strategy WHERE currencyId = ? and strategyId= ?", currency.getId(), executor.getStrategy().getId());
            Line.dao.deleteByCurrencyId(currency.getId());
            Stroke.dao.deleteByCurrencyId(currency.getId());
            Kline.dao.deleteByCurrencyId(currency.getId());
            return true;
        });
        List<Date> retDate = new ArrayList<>();
        List<Kline> klines = dfcfDownLoader.downLoad(currency, Kline.KLINE_TYPE_K, "get", null);
        KlineController klineController = new KlineController();
        klineController.setDownLoader(calibratorDownLoader);
        if (startDate == null) {
            for (int i = 0; i < klines.size(); i++) {
                List<Kline> calibrateKlines = klines.subList(i, i + 1);
                calibratorDownLoader.setResultKlines(calibrateKlines);
                klineController.downloadData(currency.getCode());
                klineController.createStroke(currency.getCode());
//                klineController.createLine(currency.getCode());
                Vector<Currency> result = executor.execute(currency.getCode());
                if (!result.isEmpty()) {
                    Kline kline = Kline.dao.getByDate(currency.getId(), Kline.KLINE_TYPE_K, klines.get(i).getDate());
                    Db.tx(() -> {
                        kline.setBosp("0");
                        kline.update();
                        return true;
                    });
                }
            }
        } else {
            boolean beforeDateFlag = true;
            for (int i = 0, j = 0; i < klines.size(); i++) {
                if (beforeDateFlag && klines.get(i).getDate().before(startDate)) {
                    if (klines.get(i).getDate().before(startDate)) {
                        continue;
                    }
                    // 到达预设时间点后,就不用再比较时间了
                    beforeDateFlag = false;
                }
                List<Kline> calibrateKlines = klines.subList(j, i + 1);
                calibratorDownLoader.setResultKlines(calibrateKlines);
                //手动提交事务,返回true提交
                Db.tx(() -> {
                    klineController.downloadData(currency.getCode());
                    return true;
                });
                Db.tx(() -> {
                    klineController.createStroke(currency.getCode());
                    return true;
                });
//              klineController.createLine(currency.getCode());
                Vector<Currency> result = executor.execute(currency.getCode());
                if (!result.isEmpty()) {
                    retDate.add(klines.get(i).getDate());
                }
                j++;
            }
        }
        for (Date date : retDate) {
            Kline kline = Kline.dao.getByDate(currency.getId(), Kline.KLINE_TYPE_K, date);
            if(kline!=null){
                Db.tx(() -> {
                    kline.setBosp("0");
                    kline.update();
                    return true;
                });
            }
        }
    }
}
