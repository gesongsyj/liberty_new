package com.liberty.system.downloader.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.liberty.common.utils.DateUtil;
import com.liberty.common.utils.HTTPUtil;
import com.liberty.common.utils.HTTPUtils;
import com.liberty.common.utils.NumUtil;
import com.liberty.system.downloader.DownLoader;
import com.liberty.system.model.*;

/**
 * 东方财富股票价格下载
 *
 * @author Administrator
 */
public class DfcfDownLoader implements DownLoader {
    // 日K
    private String ex_url3 = "http://pdfm.eastmoney.com/EM_UBG_PDTI_Fast/api/js?rtntype=5&id=" + "002113"
            + "2&type=k&_=1538045294612";
    // 5分钟
    private String ex_url2 = "http://pdfm.eastmoney.com/EM_UBG_PDTI_Fast/api/js?rtntype=5&token=4f1862fc3b5e77c150a2b985b12db0fd&cb=jQuery183039199008983664574_1538044367388&id="
            + "002113" + "2&type=" + "m5k&authorityType=&_=1538044665578";

    private String ex_url4 = "http://nuff.eastmoney.com/EM_Finance2015TradeInterface/JS.ashx?id=6000861&token=4f1862fc3b5e77c150a2b985b12db0fd&cb=jQuery18306421890141422466_1551235703502&_=1551235703549";

    private String ex_url5 = "http://pdfm.eastmoney.com/EM_UBG_PDTI_Fast/api/js?rtntype=5&token=4f1862fc3b5e77c150a2b985b12db0fd&cb=jQuery18306421890141422466_1551235703515&id=6000861&type=r&iscr=false&_=1551235705100";

    public static void main(String[] args) {
        Date date = new Date((long) 199008983664574.0);
        System.out.println(date.toLocaleString());
    }

    @Override
    public List<Kline> downLoad(Currency currency, String type, String method, Kline lastKline) {
        Date lastSeoDate = queryLastSeoDate(currency);

        if (null != lastSeoDate) {
            if (null == currency.getLastSeoDate() || 0 != currency.getLastSeoDate().compareTo(lastSeoDate)) {
                // 如果最近有派股派息，删除原有数据重新抓取
                Line.dao.deleteByCurrencyId(currency.getId());
                Stroke.dao.deleteByCurrencyId(currency.getId());
                Kline.dao.deleteByCurrencyId(currency.getId());
                lastKline = null;

                currency.setLastSeoDate(lastSeoDate);
                currency.update();
            }
        }

        // "&authorityType="表示不复权;"&authorityType=fa"表示前复权
        String url = "http://pdfm.eastmoney.com/EM_UBG_PDTI_Fast/api/js?rtntype=5&id=" + currency.getCode()
                + currency.getCurrencyType() + "&type=" + type + "&authorityType=fa&_=" + System.currentTimeMillis();
        System.out.println(url);
        String response = HTTPUtils.http(url, null, method);
        System.out.println("response:" + response);
        response = response.substring(response.indexOf("(") + 1, response.lastIndexOf(")"));
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
        return klines;
    }

    // 查询最近一次股票增发日期
    private Date queryLastSeoDate(Currency currency) {
		/* http返回结果示例
		jsonp1578649158587({"rc":0,"rt":8,"svr":181669437,"lt":1,"full":0,"data":{"code":"300628","market":0,"records":[{"date":"2017-05-12","type":1,"pxbl":1.799999,"sgbl":0.0,"cxbl":0.0,"pgbl":0.0,"pgjg":0.0,"pghg":0.0,"zfbl":0.0,"zfgs":0.0,"zfjg":0.0,"ggflag":0},{"date":"2017-09-29","type":2,"pxbl":0.0,"sgbl":1.0,"cxbl":0.0,"pgbl":0.0,"pgjg":0.0,"pghg":0.0,"zfbl":0.0,"zfgs":0.0,"zfjg":0.0,"ggflag":0},{"date":"2018-05-28","type":3,"pxbl":1.6,"sgbl":1.0,"cxbl":0.0,"pgbl":0.0,"pgjg":0.0,"pghg":0.0,"zfbl":0.0,"zfgs":0.0,"zfjg":0.0,"ggflag":0},{"date":"2018-11-30","type":16,"pxbl":0.0,"sgbl":0.0,"cxbl":0.0,"pgbl":0.0,"pgjg":0.0,"pghg":0.0,"zfbl":0.2899,"zfgs":86.599998,"zfjg":30.95,"ggflag":0},{"date":"2019-07-09","type":3,"pxbl":1.2,"sgbl":1.0,"cxbl":0.0,"pgbl":0.0,"pgjg":0.0,"pghg":0.0,"zfbl":0.0,"zfgs":0.0,"zfjg":0.0,"ggflag":0},{"date":"2019-11-06","type":16,"pxbl":0.0,"sgbl":0.0,"cxbl":0.0,"pgbl":0.0,"pgjg":0.0,"pghg":0.0,"zfbl":0.0658,"zfgs":39.400001,"zfjg":28.659999,"ggflag":0}]}});
		*/
        Date dateResult = null;
        String currencyTypeFlag;
        if (Currency.CURRENCY_TYPE_SH.equals(currency.getCurrencyType())) {
            currencyTypeFlag = "SH";
        } else {
            currencyTypeFlag = "SZ";
        }

        String url = "http://push2.eastmoney.com/api/qt/stock/cqcx/get?id=" + currencyTypeFlag + currency.getCode() + "&ut=e1e6871893c6386c5ff6967026016627&cb=jsonp1578649158587";
        String resp = HTTPUtils.http(url, null, "get");

        String respSubMap = null;
        try {
            respSubMap = resp.substring(resp.indexOf("(") + 1, resp.lastIndexOf(")"));
            Object dataObj = JSON.parseObject(respSubMap).get("data");
            Object recordsObj = JSON.parseObject(JSON.toJSONString(dataObj)).get("records");
            JSONArray jsonArray = JSON.parseArray(JSON.toJSONString(recordsObj));
            Object lastOne = jsonArray.get(jsonArray.size() - 1);
            JSONObject lastOneJson = JSON.parseObject(JSON.toJSONString(lastOne));
            String dateStr = lastOneJson.getString("date");
            dateResult = DateUtil.strDate(dateStr, "yyyy-MM-dd");
        } catch (Exception e) {
            System.out.println("返回数据解析错误！");
            return null;
        }
        return dateResult;
    }

    @Override
    public List<Kline> downLoad(Currency currency, String type, String method, int size) {
        String url = "http://pdfm.eastmoney.com/EM_UBG_PDTI_Fast/api/js?rtntype=5&id=" + currency.getCode()
                + currency.getCurrencyType() + "&type=" + type + "&_=" + System.currentTimeMillis();
        System.out.println(url);
        String response = HTTPUtils.http(url, null, method);
        response = response.substring(response.indexOf("(") + 1, response.lastIndexOf(")"));
        Map responseMap = JSON.parseObject(response, Map.class);
        Object data = responseMap.get("data");
        List<String> dataArr = JSON.parseArray(data.toString(), String.class);
        List<Kline> klines = new ArrayList<Kline>();
        for (int i = dataArr.size() - 1, j = 0; i >= 0 && j < size; i--, j++) {
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
            kline.setVolume(Double.valueOf(str[5]));
            kline.setTurnover(Double.valueOf(str[6]));
            kline.setTurnoverRate(currency.getTotalStockCount() > 0 ? Double.valueOf(str[5]) * 100 / currency.getTotalStockCount() : null);
            klines.add(kline);
        }
        Collections.reverse(klines);
        return klines;
    }

    @Override
    public void downLoadRealTimeData(Currency currency, String type, String method) {
        String url = "http://nuff.eastmoney.com/EM_Finance2015TradeInterface/JS.ashx?id=" + currency.getCode()
                + currency.getCurrencyType() + "&token=4f1862fc3b5e77c150a2b985b12db0fd&cb=jQuery18306421890141422466_"
                + System.currentTimeMillis();
        url = url + "&_=" + System.currentTimeMillis();
        System.out.println(url);
        String response = HTTPUtils.http(url, null, method);
        response = response.substring(response.indexOf("(") + 1, response.lastIndexOf(")"));
        Map responseMap = JSON.parseObject(response, Map.class);
        Object data = responseMap.get("Value");
        List<String> dataArr = JSON.parseArray(data.toString(), String.class);
        if (currency.getCode().equals(dataArr.get(1))) {
            currency.put("b1", dataArr.get(3));    //买一
            currency.put("b2", dataArr.get(4));    //买二
            currency.put("b3", dataArr.get(5));    //买三
            currency.put("b4", dataArr.get(6));    //买四
            currency.put("b5", dataArr.get(7));    //买五
            currency.put("s1", dataArr.get(8));    //卖一
            currency.put("s2", dataArr.get(9));    //卖二
            currency.put("s3", dataArr.get(10));   //卖三
            currency.put("s4", dataArr.get(11));   //卖四
            currency.put("s5", dataArr.get(12));   //卖五

            currency.put("b1n", dataArr.get(13));  //买一数量
            currency.put("b2n", dataArr.get(14));  //买二数量
            currency.put("b3n", dataArr.get(15));  //买三数量
            currency.put("b4n", dataArr.get(16));  //买四数量
            currency.put("b5n", dataArr.get(17));  //买五数量
            currency.put("s1n", dataArr.get(18));  //卖一数量
            currency.put("s2n", dataArr.get(19));  //卖二数量
            currency.put("s3n", dataArr.get(20));  //卖三数量
            currency.put("s4n", dataArr.get(21));  //卖四数量
            currency.put("s5n", dataArr.get(22));  //卖五数量
        }
    }

}
