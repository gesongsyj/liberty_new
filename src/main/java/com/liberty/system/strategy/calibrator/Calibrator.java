package com.liberty.system.strategy.calibrator;

import com.liberty.system.downloader.DownLoader;
import com.liberty.system.downloader.impl.CalibratorDownLoader;
import com.liberty.system.downloader.impl.DfcfDownLoader;
import com.liberty.system.model.Currency;
import com.liberty.system.model.Kline;
import com.liberty.system.model.Line;
import com.liberty.system.model.Stroke;
import com.liberty.system.strategy.executor.Executor;
import com.liberty.system.web.KlineController;

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
            calibrate(currency);
        }
    }

    public void calibrate(Currency currency) {
        CalibratorDownLoader calibratorDownLoader = new CalibratorDownLoader();
        DownLoader dfcfDownLoader = new DfcfDownLoader();

        Line.dao.deleteByCurrencyId(currency.getId());
        Stroke.dao.deleteByCurrencyId(currency.getId());
        Kline.dao.deleteByCurrencyId(currency.getId());

        List<Kline> klines = dfcfDownLoader.downLoad(currency,Kline.KLINE_TYPE_K,"get",null);
        KlineController klineController = new KlineController();
        klineController.setDownLoader(calibratorDownLoader);
        for (int i = 1; i <= klines.size(); i++) {
            List<Kline> calibrateKlines = klines.subList(i-1, i);
            calibratorDownLoader.setResultKlines(calibrateKlines);
            klineController.downloadData(currency.getCode());
            klineController.createStroke(currency.getCode());
            klineController.createLine(currency.getCode());
            Vector<Currency> result = executor.execute(currency.getCode());
            if (!result.isEmpty()) {
                Kline kline = Kline.dao.getByDate(currency.getCode(),Kline.KLINE_TYPE_K, klines.get(i).getDate());
                kline.setBosp("0");
                kline.update();
            }
        }
    }
}
