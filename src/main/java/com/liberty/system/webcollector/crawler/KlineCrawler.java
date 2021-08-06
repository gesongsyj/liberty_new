package com.liberty.system.webcollector.crawler;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatum;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;
import cn.edu.hfut.dmic.webcollector.plugin.ram.RamCrawler;
import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.liberty.common.constant.ConstantDefine;
import com.liberty.common.utils.DateUtil;
import com.liberty.common.utils.NumUtil;
import com.liberty.common.utils.stock.CurrencyUtil;
import com.liberty.system.model.Currency;
import com.liberty.system.model.Kline;
import com.liberty.system.model.Line;
import com.liberty.system.model.Stroke;
import com.liberty.system.test.callback.Li;

import java.util.*;

public class KlineCrawler extends RamCrawler {
    public KlineCrawler(boolean autoParse) {
        super(autoParse);
        List<Currency> cs = Currency.dao.listAll();
        cs.stream().parallel().forEach(currency->{
            Date lastSeoDate = CurrencyUtil.queryLastSeoDate(currency);

            if (null != lastSeoDate) {
                if (null == currency.getLastSeoDate() || 0 != currency.getLastSeoDate().compareTo(lastSeoDate)) {
                    // 如果最近有派股派息，删除原有数据重新抓取
                    Line.dao.deleteByCurrencyId(currency.getId());
                    Stroke.dao.deleteByCurrencyId(currency.getId());
                    Kline.dao.deleteByCurrencyId(currency.getId());
                    currency.setLastSeoDate(lastSeoDate);
                    currency.update();
                }
            }
        });
        String type = ConstantDefine.KLINE_TYPE_K;
        for (Currency currency : cs) {
            String url = "http://pdfm.eastmoney.com/EM_UBG_PDTI_Fast/api/js?rtntype=5&id=" + currency.getCode()
                    + currency.getCurrencyType() + "&type=" + type + "&authorityType" +
                    "=fa&_=" + System.currentTimeMillis();
            CrawlDatum datum = new CrawlDatum(url).meta("type", type).meta(new JsonParser().parse(new Gson().toJson(currency)).getAsJsonObject());
            this.addSeed(datum);
        }

    }

    @Override
    public void visit(Page page, CrawlDatums crawlDatums) {
        String type = page.meta("type");
        JsonObject currencyJson = page.meta();
        Gson gson = new Gson();
        Currency currency = gson.fromJson(gson.toJson(currencyJson), Currency.class);
        Kline lastKline = Kline.dao.getLastOneByCurrencyId(currency.getId(), ConstantDefine.KLINE_TYPE_K);
        System.out.println("response:" + page.html());
        String response = page.html().substring(page.html().indexOf("(") + 1, page.html().lastIndexOf(")"));
        Map responseMap = JSON.parseObject(response, Map.class);
        Object data = responseMap.get("data");
        List<String> dataArr = JSON.parseArray(data.toString(), String.class);
        List<Kline> klines = new ArrayList<Kline>();
        if (lastKline == null) {
            for (int i = dataArr.size() - 1; i >= 0; i--) {
                String[] str = dataArr.get(i).split(",");
                Date date = null;
                if (str[0].contains(" ")) {
                    date = DateUtil.strDate(str[0], "yyyy-MM-dd HH:mm");
                } else {
                    date = DateUtil.strDate(str[0], "yyyy-MM-dd");
                }
                Kline kline = new Kline();
                kline.setDate(date);
                kline.setOpen(Double.valueOf(str[1]));
                kline.setClose(Double.valueOf(str[2]));
                kline.setMax(Double.valueOf(str[3]));
                kline.setMin(Double.valueOf(str[4]));
                kline.setVolume(NumUtil.parseNumFromStr(str[5]));
                kline.setTurnover(NumUtil.parseNumFromStr(str[6]));
                kline.setTurnoverRate(currency.getTotalStockCount() > 0 ? kline.getVolume() * 100 / currency.getTotalStockCount() : null);
                kline.setCurrencyId(currency.getId());
                kline.setType(type);
                klines.add(kline);
            }
        } else {
            for (int i = dataArr.size() - 1; i >= 0; i--) {
                String[] str = dataArr.get(i).split(",");
                Date date = null;
                if (str[0].contains(" ")) {
                    date = DateUtil.strDate(str[0], "yyyy-MM-dd HH:mm");
                } else {
                    date = DateUtil.strDate(str[0], "yyyy-MM-dd");
                }
                if (date.getTime() > lastKline.getDate().getTime()) {
                    Kline kline = new Kline();
                    kline.setDate(date);
                    kline.setOpen(Double.valueOf(str[1]));
                    kline.setClose(Double.valueOf(str[2]));
                    kline.setMax(Double.valueOf(str[3]));
                    kline.setMin(Double.valueOf(str[4]));
                    kline.setVolume(NumUtil.parseNumFromStr(str[5]));
                    kline.setTurnover(NumUtil.parseNumFromStr(str[6]));
                    kline.setTurnoverRate(currency.getTotalStockCount() > 0 ? kline.getVolume() * 100 / currency.getTotalStockCount() : null);
                    kline.setCurrencyId(currency.getId());
                    kline.setType(type);
                    klines.add(kline);
                } else {
                    break;
                }
            }
        }
        Collections.reverse(klines);

        if (klines == null || klines.size() == 0) {
            return;
        }
        Map<String, List<Kline>> klineMap = new HashMap<String, List<Kline>>();
        Map<String, Kline> lastKlineMap = new HashMap<String, Kline>();
        klineMap.put(currency.getCode() + "_" + type, klines);
        lastKlineMap.put(currency.getCode() + "_" + type, lastKline);
        Kline.dao.saveMany(klineMap, lastKlineMap);
        klineMap.clear();
        lastKlineMap.clear();
    }


}
