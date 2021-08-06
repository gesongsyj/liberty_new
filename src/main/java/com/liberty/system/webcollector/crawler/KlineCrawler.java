package com.liberty.system.webcollector.crawler;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatum;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;
import cn.edu.hfut.dmic.webcollector.plugin.ram.RamCrawler;
import com.liberty.common.constant.ConstantDefine;
import com.liberty.common.utils.stock.CurrencyUtil;
import com.liberty.system.model.Currency;
import com.liberty.system.model.Kline;
import com.liberty.system.model.Line;
import com.liberty.system.model.Stroke;

import java.util.Date;
import java.util.List;

public class KlineCrawler extends RamCrawler {
    public KlineCrawler() {
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
        for (Currency currency : cs) {
            String url = "http://pdfm.eastmoney.com/EM_UBG_PDTI_Fast/api/js?rtntype=5&id=" + currency.getCode()
                    + currency.getCurrencyType() + "&type=" + ConstantDefine.KLINE_TYPE_K + "&authorityType" +
                    "=fa&_=" + System.currentTimeMillis();
            CrawlDatum datum = new CrawlDatum(url).meta("currencyCode",currency.getCode());
            this.addSeed(datum);
        }

    }

    @Override
    public void visit(Page page, CrawlDatums crawlDatums) {
        String currencyCode = page.meta("currencyCode");
        System.out.println(111);
    }


}
