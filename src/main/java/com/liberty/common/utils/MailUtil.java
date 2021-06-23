package com.liberty.common.utils;

import java.util.*;

import com.alibaba.fastjson.JSON;
import com.jfinal.kit.PropKit;
import com.jfplugin.mail.MailKit;
import com.liberty.system.model.Currency;
import com.liberty.system.model.Strategy;
import com.liberty.system.strategy.executor.Executor;
import com.liberty.system.strategy.executor.job.StrategyExecutor;

public class MailUtil {

    private static Map<String, Map<String, Vector<Currency>>> strategyCurrencyMap = new LinkedHashMap<>();

    public static synchronized void addCurrency2Buy(Date date, Strategy s, Currency currency) {
        Map<String, Vector<Currency>> strategyCurrencyMap = MailUtil.strategyCurrencyMap.get(date.toLocaleString());
        if (null == strategyCurrencyMap) {
            strategyCurrencyMap = new LinkedHashMap<>();
            Vector<Currency> cs = new Vector<>();
            strategyCurrencyMap.put(s.getDescribe(), cs);
            MailUtil.strategyCurrencyMap.put(date.toLocaleString(), strategyCurrencyMap);
        } else {
            Vector<Currency> cs = strategyCurrencyMap.get(s.getDescribe());
            if (null == cs) {
                cs = new Vector<>();
                strategyCurrencyMap.put(s.getDescribe(), cs);
            }
            cs.add(currency);
        }
    }

    /**
     * 满足策略时发送邮件
     *
     * @param s
     */
    public synchronized static void sendMailToBuy(Date date, Strategy s) {
        try {
            Map<String, Vector<Currency>> strategyCurrencyMap = MailUtil.strategyCurrencyMap.get(date.toLocaleString());
            if (null != strategyCurrencyMap && !strategyCurrencyMap.isEmpty()) {
                Vector<Currency> cs = strategyCurrencyMap.get(s.getDescribe());
                if (null != cs && !cs.isEmpty()) {
                    System.out.println("满足策略[" + date.toLocaleString() + "#" + s.getDescribe() + "]的股票集合:" + JSON.toJSONString(cs));
                    StringBuilder sb = new StringBuilder();
                    sb.append("[");
                    for (int i = 0; i < cs.size(); i++) {
                        sb.append(cs.get(i).getCode()).append(":").append(cs.get(i).getName());
                        if (i != cs.size() - 1) {
                            sb.append(",");
                        }
                    }
                    sb.append("]");
                    sb.append("等股票满足策略:");
                    sb.append(date.toLocaleString() + "#" + s.getDescribe());
                    List<String> cc = new ArrayList<>();
                    // 第二个参数是抄送对象
                    try {
                        MailKit.send("530256489@qq.com", null, "买入提醒!", sb.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // 发送邮件后从map中移除
                    strategyCurrencyMap.remove(s.getDescribe());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 满足策略时发送邮件
     *
     * @param cs
     * @param s
     */
    public synchronized static void sendMailToBuy(Vector<Currency> cs, Strategy s) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < cs.size(); i++) {
                sb.append(cs.get(i).getCode()).append(":").append(cs.get(i).getName());
                if (i != cs.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("]");
            sb.append("等股票满足策略:");
            sb.append(s.getDescribe());
            List<String> cc = new ArrayList<>();
            // 第二个参数是抄送对象
            MailKit.send("530256489@qq.com", null, "买入提醒!", sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 满足策略时发送邮件
     *
     * @param cs
     * @param s
     */
    public synchronized static void sendMailToBuy(Vector<Currency> cs, Executor s) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < cs.size(); i++) {
                sb.append(cs.get(i).getCode()).append(":").append(cs.get(i).getName());
                if (i != cs.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("]");
            sb.append("等股票满足策略:");
            sb.append(s.getStrategy().getDescribe());
            List<String> cc = new ArrayList<>();
            // 第二个参数是抄送对象
            MailKit.send("530256489@qq.com", null, s.getExecuteDate() + "买入提醒!", sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 跌破止损线时发送邮件
     *
     * @param currency
     * @param s
     */
    public synchronized static void sendMailToSale(Currency currency, Strategy s, double cutLine, int csId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            sb.append(currency.getCode()).append(":").append(currency.getName());
            sb.append("]");
            sb.append("在策略:[");
            sb.append(s.getDescribe());
            sb.append("]下失败,跌破止损线:[");
            sb.append(cutLine);
            sb.append("].请及时卖出!");
            sb.append("如已知悉,点击链接清除止损线:");
            sb.append(PropKit.use("jfinal.properties").get("serverUrl"));
            sb.append("currency/cutLine?id=");
            sb.append(csId);
            sb.append("&cutLine=null");
            // 第二个参数是抄送对象
            MailKit.send("530256489@qq.com", null, "止损提醒!", sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
