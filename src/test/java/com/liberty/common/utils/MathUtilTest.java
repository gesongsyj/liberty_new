package com.liberty.common.utils;

import com.jfinal.kit.PathKit;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.OrderedFieldContainerFactory;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfplugin.mail.MailPlugin;
import com.liberty.common.jfinal._MappingKit;
import com.liberty.common.utils.stock.MaUtil;
import com.liberty.common.utils.stock.MathUtil;
import com.liberty.system.bean.common.LsmParam;
import com.liberty.system.model.Kline;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class MathUtilTest {
    @Before
    public void before() {
        // 读取jdbc配置
        final String url = "jdbc:mysql://127.0.0.1:3306/liberty?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull";
        final String username = "root";
        final String password = "123456";
        final String driverClass = "com.mysql.jdbc.Driver";

        DruidPlugin druidPlugin = new DruidPlugin(url, username, password, driverClass);
        // 实体映射
        ActiveRecordPlugin arp = new ActiveRecordPlugin(druidPlugin);
        arp.setShowSql(true);
        arp.setContainerFactory(new OrderedFieldContainerFactory());// 字段有序，保持和查询的顺序一致
        // 设置sql存放的根路径
        String a= PathKit.getRootClassPath();
        String b= PathKit.getWebRootPath()+"/target/classes";
        arp.setBaseSqlTemplatePath(b + "/sql");
        arp.addSqlTemplate("all.sql");
        // DB映射
        _MappingKit.mapping(arp);

        // 邮件插件
        MailPlugin mailPlugin = new MailPlugin(PropKit.use("mail.properties").getProperties());
        mailPlugin.start();
        druidPlugin.start();
        arp.start();
    }

    @Test
    public void test01(){
        List<Kline> byDateRange = Kline.dao.listAllByCurrencyId(977, Kline.KLINE_TYPE_K);

//        List<Kline> byDateRange = Kline.dao.getByDateRange("002547",Kline.KLINE_TYPE_K,DateUtil.strDate("2016-11-11 00:00:00","yyyy-MM-dd HH:mm:ss"),DateUtil.strDate("2019-11-11 00:00:00","yyyy-MM-dd HH:mm:ss"));
        List<Double> data = MaUtil.calculateMA(byDateRange, 250);
        Collections.reverse(data);
        List<Double> doubles = data.subList(0, 50);
        Collections.reverse(doubles);
        MathUtil.normalization(doubles);
        LsmParam lsmParam = MathUtil.lsmCal(doubles);
        boolean b = MathUtil.lineFittingCheck(doubles, lsmParam);

//        Vector<Currency> stayCurrency = new Vector<>();
//        Currency currency = new Currency();
//        currency.setCode("002351");
//        currency.setName("漫步者");
//        stayCurrency.add(currency);
//        Strategy byId = Strategy.dao.findById(8);
//        MailUtil.sendMailToBuy(stayCurrency, byId);
    }
}
