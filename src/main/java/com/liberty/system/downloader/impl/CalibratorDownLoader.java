package com.liberty.system.downloader.impl;

import com.liberty.system.downloader.DownLoader;
import com.liberty.system.model.Currency;
import com.liberty.system.model.Kline;

import java.util.List;

public class CalibratorDownLoader implements DownLoader {
    private List<Kline> resultKlines;

    public void setResultKlines(List<Kline> resultKlines){
        this.resultKlines = resultKlines;
    }

    @Override
    public List<Kline> downLoad(Currency currency, String type, String method, Kline lastKline) {
        return resultKlines;
    }

    @Override
    public List<Kline> downLoad(Currency currency, String type, String method, int size) {
        return null;
    }

    @Override
    public void downLoadRealTimeData(Currency currency, String type, String method) {

    }
}
