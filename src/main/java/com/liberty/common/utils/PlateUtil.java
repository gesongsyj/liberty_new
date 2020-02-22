package com.liberty.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.json.Json;
import com.liberty.system.model.Currency;

import java.text.MessageFormat;
import java.util.*;

/**
 * 板块查询工具类
 */
public class PlateUtil {

    /**
     * 根据股票代码查询所属板块集合
     * @param currency 股票
     * @return 所属板块集合
     */
    public static List<Currency> queryOwnerPlate(Currency currency){
//        String url_bak = "http://push2.eastmoney.com/api/qt/slist/get?ut=fa5fd1943c7b386f172d6893dbfba10b&spt=3&pi=0&po=1&invt=2&fields=f14,f3,f128,f12,f13,f100,f102,f103&secid=1.600267&cb=jQuery1123011181689453008203_1580961765174&_=1580961765205";
        String url = "http://push2.eastmoney.com/api/qt/slist/get?ut=fa5fd1943c7b386f172d6893dbfba10b&spt=3&pi=0&po=1&invt=2&fields=f14,f3,f128,f12,f13,f100,f102,f103&secid={0}.{1}&cb=jQuery1123011181689453008203_{2}&_={3}";
        long timeMillis = System.currentTimeMillis();
        if(Currency.CURRENCY_TYPE_SH.equals(currency.getCurrencyType())){
            url = MessageFormat.format(url,"1",currency.getCode(),timeMillis,timeMillis);
        }else{
            url = MessageFormat.format(url,"0",currency.getCode(),timeMillis,timeMillis);
        }
        // 调用工具类访问接口
        String response = HTTPUtil.http(url, null, "get");
        // 解析返回数据
        response = response.substring(response.indexOf("(") + 1, response.lastIndexOf(")"));
        JSONObject jsonObjResp = JSON.parseObject(response);
        Object dataObj = jsonObjResp.get("data");
        JSONObject dataJsonObj = JSON.parseObject(JSON.toJSONString(dataObj));
        Object diffObj = dataJsonObj.get("diff");
        JSONObject diffJsonObj = JSON.parseObject(JSON.toJSONString(diffObj));
        // new出返回的集合
        List<Currency> cs = new ArrayList<>();
        // 解析好的数据封装成currency
        for (Map.Entry<String, Object> diffEntry : diffJsonObj.entrySet()) {
            Object value = diffEntry.getValue();
            JSONObject valueJsonObj = JSON.parseObject(JSON.toJSONString(value));
            Currency c = new Currency();
            c.setName(valueJsonObj.getString("f14"));
            c.setCode(valueJsonObj.getString("f12"));
            // 添加到集合中
            cs.add(c);
        }
        // 返回集合对象
        return cs;
    }

    /**
     *
     * @param plates 板块集合
     * @return 符合板块条件的股票,去重
     */
    public static List<Currency> queryCurrencyInPlates(List<Currency> plates){
//        String url_bak = "http://65.push2.eastmoney.com/api/qt/clist/get?cb=jQuery112405116315266983614_1580965561613&pn=1&pz=20&po=1&np=1&ut=bd1d9ddb04089700cf9c27f6f7426281&fltt=2&invt=2&fid=f3&fs=b:BK0465&fields=f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f12,f13,f14,f15,f16,f17,f18,f20,f21,f23,f24,f25,f22,f11,f62,f128,f136,f115,f152&_=1580965561614";
        String url = "http://65.push2.eastmoney.com/api/qt/clist/get?cb=jQuery112405116315266983614_{0}&pn=1&pz=5000&po=1&np=1&ut=bd1d9ddb04089700cf9c27f6f7426281&fltt=2&invt=2&fid=f3&fs=b:{1}&fields=f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f12,f13,f14,f15,f16,f17,f18,f20,f21,f23,f24,f25,f22,f11,f62,f128,f136,f115,f152&_={2}";
        long timeMillis = System.currentTimeMillis();
        List<Currency> cs = new ArrayList<>();
        List<String> currencyCodes = new ArrayList<>();
        for (Currency plate : plates) {
            // 替换url中的占位符
            String formatUrl = MessageFormat.format(url, timeMillis, plate.getCode(), timeMillis);
            // 调用http工具类访问接口
            String response = HTTPUtil.http(formatUrl, null, "get");
            // 返回数据解析
            response = response.substring(response.indexOf("(") + 1, response.lastIndexOf(")"));
            JSONObject respJsonObj = JSON.parseObject(response);
            Object dataObj = respJsonObj.get("data");
            JSONObject dataJsonObj = JSON.parseObject(JSON.toJSONString(dataObj));
            Object diffObj = dataJsonObj.get("diff");
            JSONArray diffJsonArray = JSON.parseArray(JSON.toJSONString(diffObj));
            for (Object diffItem : diffJsonArray) {
                JSONObject itemObj = JSON.parseObject(JSON.toJSONString(diffItem));
                // 判断是否已经存在该股票
                if(!currencyCodes.contains(itemObj.getString("f12"))){
                    currencyCodes.add(itemObj.getString("f12"));
                    Currency c = new Currency();
                    if("1".equals(itemObj.getString("f13"))){
                        c.setCurrencyType(Currency.CURRENCY_TYPE_SH);
                    }else{
                        c.setCurrencyType(Currency.CURRENCY_TYPE_SZ);
                    }
                    c.setCode(itemObj.getString("f12"));
                    c.setName(itemObj.getString("f14"));
                    cs.add(c);
                }
            }
        }
        return cs;
    }
}
