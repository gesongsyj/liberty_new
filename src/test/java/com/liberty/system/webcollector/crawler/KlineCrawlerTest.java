package com.liberty.system.webcollector.crawler;

import com.jfinal.kit.PathKit;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.OrderedFieldContainerFactory;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfplugin.mail.MailPlugin;
import com.liberty.common.jfinal._MappingKit;
import com.liberty.system.strategy.calibrator.Calibrator;
import com.liberty.system.strategy.executor.Executor;
import com.liberty.system.strategy.executor.job.ExecutorFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class KlineCrawlerTest {

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
        String a = PathKit.getRootClassPath();
        String b = PathKit.getWebRootPath() + "/target/classes";
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

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void visit() throws Exception {
        KlineCrawler klineCrawler = new KlineCrawler();
        klineCrawler.start();
    }
}