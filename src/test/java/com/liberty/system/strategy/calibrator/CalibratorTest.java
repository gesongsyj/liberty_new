package com.liberty.system.strategy.calibrator;

import com.jfinal.kit.PathKit;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.OrderedFieldContainerFactory;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfplugin.mail.MailPlugin;
import com.liberty.common.jfinal._MappingKit;
import com.liberty.common.utils.DateUtil;
import com.liberty.system.model.Currency;
import com.liberty.system.strategy.executor.Executor;
import com.liberty.system.strategy.executor.job.ExecutorFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class CalibratorTest {
    private Calibrator calibrator;

    @Before
    public void setUp() throws Exception {
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

        Executor executor = ExecutorFactory.buildExecutor(12);
        executor.setOnlyK(true);
        calibrator = new Calibrator(executor);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void calibrate() {
        // true
//        Currency currency = Currency.dao.findByCode("600990");
//        Date startDate = DateUtil.strDate("2019-03-14", "yyyy-MM-dd");
        // false
//        Currency currency = Currency.dao.findByCode("600990");
//        Date startDate = DateUtil.strDate("2018-09-27", "yyyy-MM-dd");
        Currency currency = Currency.dao.findByCode("600990");
        Date startDate = DateUtil.strDate("2018-04-27", "yyyy-MM-dd");
        calibrator.calibrate(currency,startDate);
        System.out.println(111);
    }
}
