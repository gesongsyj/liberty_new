package com.liberty.common.utils.stock;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.liberty.common.utils.DateUtil;
import com.liberty.common.utils.HTTPUtils;
import com.liberty.system.model.Currency;

import java.util.Date;

public class CurrencyUtil {
    // 查询最近一次股票增发日期
    public static Date queryLastSeoDate(Currency currency) {
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
}
