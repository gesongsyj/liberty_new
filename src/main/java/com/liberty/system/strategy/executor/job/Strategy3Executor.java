package com.liberty.system.strategy.executor.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfplugin.mail.MailKit;
import com.liberty.common.utils.HTTPUtil;
import com.liberty.common.utils.MailUtil;
import com.liberty.common.utils.NumUtil;
import com.liberty.system.blackHouse.RemoveStrategyBh;
import com.liberty.system.model.Currency;
import com.liberty.system.model.Strategy;
import com.liberty.system.strategy.executor.Executor;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * 查找绩优股
 * 1:归属净利润近N年连续增长
 * 2:净利润增长率在阈值之上,如:20%
 */
public class Strategy3Executor extends StrategyExecutor implements Executor {
    // 判断的年数，连续两年或者三年或者几年
    private static final int YEAR_COUNT = 2;
    // 加权净资产收益率的阈值
    private static final double JQJZCSYL_LIMIT = 25.0;

    // 主要指标查询url，type：0-按报告期，1-按年度，2-按单季度；code后面跟SH/SZ+股票代码
    private static final String QUERY_METRIC_URL_YEAR = "http://f10.eastmoney.com/NewFinanceAnalysis/MainTargetAjax?type=1&code={0}";
    private static final String QUERY_METRIC_URL_REPORT = "http://f10.eastmoney.com/NewFinanceAnalysis/MainTargetAjax?type=0&code={0}";
    // 主要指标查询拼接股票代码前缀前缀
    private static final String QUERY_METRIC_SH = "SH";
    private static final String QUERY_METRIC_SZ = "SZ";

    public Strategy3Executor() {
        this.strategy = Strategy.dao.findById(3);
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
                    if (successStrategy(currency)) {
                        stayCurrency.add(currency);
                    }
                }
//				else {
//					不自动从策略组中剔除,自动剔除容易错过符合条件的股票
//					Record record = Db.findFirst("select * from currency_strategy where cutLine is not null and currencyId=? and strategyId=?",
//							currency.getId(), strategy.getId());
//					if(record!=null) {
//						Db.delete("currency_strategy",record);
//					}
//				}
            }
        }
        if (stayCurrency.size() != 0) {
            MailUtil.sendMailToBuy(stayCurrency, super.getStrategy());
        }
        System.out.println("策略3执行完毕!");
        long end = System.currentTimeMillis();
        double time = (end - start) * 1.0 / 1000 / 60;
        MailKit.send("530256489@qq.com", null, "策略[" + strategy.getDescribe() + "]执行耗时提醒!", "此次策略执行耗时:" + time + "分钟!");
        return stayCurrency;
    }

    @Override
    public boolean executeSingle(Currency currency) {
        String query_metric_url_year_full_url;
        String query_metric_url_report_full_url;
        if(currency.getCurrencyType().equals(Currency.CURRENCY_TYPE_SH)) {
            query_metric_url_year_full_url = MessageFormat.format(QUERY_METRIC_URL_YEAR, QUERY_METRIC_SH+currency.getCode());
            query_metric_url_report_full_url = MessageFormat.format(QUERY_METRIC_URL_REPORT, QUERY_METRIC_SH+currency.getCode());
        }else{//深证或科创板
            query_metric_url_year_full_url =MessageFormat.format(QUERY_METRIC_URL_YEAR, QUERY_METRIC_SZ+currency.getCode());
            query_metric_url_report_full_url = MessageFormat.format(QUERY_METRIC_URL_REPORT, QUERY_METRIC_SZ+currency.getCode());
        }
        String resp_year = HTTPUtil.http(query_metric_url_year_full_url, null, "get");
        System.out.println(query_metric_url_year_full_url+"-->\n"+currency.getCode()+"resp_year:"+resp_year);
        JSONArray jsonArray_year = JSON.parseArray(resp_year);
        String resp_report = HTTPUtil.http(query_metric_url_report_full_url, null, "get");
        JSONArray jsonArray_report = JSON.parseArray(resp_report);
        if(jsonArray_year.size()<YEAR_COUNT || jsonArray_report.size()<YEAR_COUNT){
            return false;
        }

        double gsjlr_val = Double.MAX_VALUE;
        for (int i = 0; i < YEAR_COUNT; i++) {
            JSONObject data_year1 = JSON.parseObject(JSON.toJSONString(jsonArray_year.get(i)));
            double gsjlr = NumUtil.parseNumFromStr(data_year1.getString("gsjlr"));
            if(gsjlr>gsjlr_val || data_year1.getDoubleValue("jqjzcsyl")<JQJZCSYL_LIMIT){
                return false;
            }
            gsjlr_val =gsjlr;
        }
        // 比较同比增长
        JSONObject data_report0 = JSON.parseObject(JSON.toJSONString(jsonArray_report.get(0)));
        JSONObject data_report4 = JSON.parseObject(JSON.toJSONString(jsonArray_report.get(4)));
        // 归属净利润和加权净资产收益率同比下降,误差5%
        if(NumUtil.parseNumFromStr(data_report0.getString("gsjlr")) < 0.95*NumUtil.parseNumFromStr(data_report4.getString("gsjlr"))|| data_report0.getDoubleValue("jqjzcsyl")<0.95*data_report4.getDoubleValue("jqjzcsyl")){
            return false;
        }

        return true;
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
