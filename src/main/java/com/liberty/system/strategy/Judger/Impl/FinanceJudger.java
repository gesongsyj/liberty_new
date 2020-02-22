package com.liberty.system.strategy.Judger.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.liberty.common.utils.HTTPUtil;
import com.liberty.common.utils.NumUtil;
import com.liberty.system.strategy.Judger.filterParam.impl.FinanceParam;
import com.liberty.system.model.Currency;
import com.liberty.system.strategy.Judger.Judger;

import java.text.MessageFormat;
import java.util.Date;

public class FinanceJudger implements Judger {
    // 主要指标查询url，type：0-按报告期，1-按年度，2-按单季度；code后面跟SH/SZ+股票代码
    private static final String QUERY_METRIC_URL_YEAR = "http://f10.eastmoney.com/NewFinanceAnalysis/MainTargetAjax?type=1&code={0}";
    private static final String QUERY_METRIC_URL_REPORT = "http://f10.eastmoney.com/NewFinanceAnalysis/MainTargetAjax?type=0&code={0}";
    // 主要指标查询拼接股票代码前缀前缀
    private static final String QUERY_METRIC_SH = "SH";
    private static final String QUERY_METRIC_SZ = "SZ";

    // 持有财务状况参数对象
    private FinanceParam financeParam;

    // 带参数构造器
    public FinanceJudger(FinanceParam financeParam) {
        this.financeParam = financeParam;
    }

    @Override
    public boolean judgeItem(Currency currency, Date date) {
        String query_metric_url_year_full_url;
        String query_metric_url_report_full_url;
        if (currency.getCurrencyType().equals(Currency.CURRENCY_TYPE_SH)) {
            query_metric_url_year_full_url = MessageFormat.format(QUERY_METRIC_URL_YEAR, QUERY_METRIC_SH + currency.getCode());
            query_metric_url_report_full_url = MessageFormat.format(QUERY_METRIC_URL_REPORT, QUERY_METRIC_SH + currency.getCode());
        } else {//深证或科创板
            query_metric_url_year_full_url = MessageFormat.format(QUERY_METRIC_URL_YEAR, QUERY_METRIC_SZ + currency.getCode());
            query_metric_url_report_full_url = MessageFormat.format(QUERY_METRIC_URL_REPORT, QUERY_METRIC_SZ + currency.getCode());
        }
        String resp_year = HTTPUtil.http(query_metric_url_year_full_url, null, "get");
        System.out.println(query_metric_url_year_full_url + "-->\n" + currency.getCode() + "resp_year:" + resp_year);
        JSONArray jsonArray_year = JSON.parseArray(resp_year);
        String resp_report = HTTPUtil.http(query_metric_url_report_full_url, null, "get");
        JSONArray jsonArray_report = JSON.parseArray(resp_report);
        if (jsonArray_year.size() < financeParam.getJudgeYearCount() || jsonArray_report.size() < 5) {
            return false;
        }

        double gsjlr_val = Double.MAX_VALUE;
        for (int i = 0; i < financeParam.getJudgeYearCount(); i++) {
            JSONObject data_year1 = JSON.parseObject(JSON.toJSONString(jsonArray_year.get(i)));

            if (financeParam.getIncreaseEnsure()) {
                double gsjlr = NumUtil.parseNumFromStr(data_year1.getString("gsjlr"));
                if (gsjlr > gsjlr_val) {
                    return false;
                }
                gsjlr_val = gsjlr;
            }
            if (NumUtil.parseNumFromStr(data_year1.getString("jqjzcsyl")) < financeParam.getJzcsylLimit() && NumUtil.parseNumFromStr(data_year1.getString("tbjzcsyl")) < financeParam.getJzcsylLimit()) {
                return false;
            }
        }
        // 比较同比增长
        JSONObject data_report0 = JSON.parseObject(JSON.toJSONString(jsonArray_report.get(0)));
        JSONObject data_report4 = JSON.parseObject(JSON.toJSONString(jsonArray_report.get(4)));
        System.out.println("data_report0:" + data_report0);
        System.out.println("data_report4:" + data_report4);
        // 归属净利润和加权净资产收益率同比下降,误差5%
        if (NumUtil.parseNumFromStr(data_report0.getString("gsjlr")) < (1 - financeParam.getErrorRangeLimit()) * NumUtil.parseNumFromStr(data_report4.getString("gsjlr")) || NumUtil.parseNumFromStr(data_report0.getString("jqjzcsyl")) < 0.95 * NumUtil.parseNumFromStr(data_report4.getString("jqjzcsyl")) && NumUtil.parseNumFromStr(data_report0.getString("tbjzcsyl")) < 0.95 * NumUtil.parseNumFromStr(data_report4.getString("tbjzcsyl"))) {
            return false;
        }

        return true;
    }
}
