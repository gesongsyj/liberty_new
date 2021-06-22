package com.liberty.common.jfinal;

import cn.dreampie.quartz.QuartzPlugin;
import com.jfinal.config.*;
import com.jfinal.ext.interceptor.SessionInViewInterceptor;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.OrderedFieldContainerFactory;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.render.ViewType;
import com.jfinal.template.Engine;
import com.jfplugin.mail.MailPlugin;
import com.liberty.common.interceptor.CoreInterceptor;
import com.liberty.common.interceptor.ShiroInterceptor;
import com.liberty.common.plugins.threadPoolPlugin.ThreadPoolPlugin;
import com.liberty.common.utils.CacheUtil;
import com.liberty.common.utils.DateUtil;
import com.liberty.system.model.Currency;
import com.liberty.system.model.Kline;
import com.liberty.system.model.Strategy;
import com.liberty.system.strategy.Judger.JudgerExe;
import com.liberty.system.strategy.agent.AgentSyn;
import com.liberty.system.strategy.agent.StrategyAgent;
import com.liberty.system.strategy.agent.impl.BoxBreakStrategyAgent;
import com.liberty.system.strategy.agent.impl.CombinationStrategyAgent;
import com.liberty.system.strategy.agent.impl.ReverseCheckStrategyAgent;
import com.liberty.system.strategy.agent.impl.UpwardTrendStrategyAgent;
import com.liberty.system.strategy.calibrator.Calibrator;
import com.liberty.system.strategy.executor.Executor;
import com.liberty.system.strategy.executor.job.*;
import net.dreamlu.event.EventPlugin;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CoreConfig extends JFinalConfig {
	@Override
	public void configConstant(Constants me) {
		loadPropertyFile("jfinal.properties");
		me.setEncoding("UTF-8");
		me.setDevMode(true);
		me.setViewType(ViewType.FREE_MARKER);
	}

	@Override
	public void configRoute(Routes me) {
		me.add(new CoreRoutes());
	}

	@Override
	public void configPlugin(Plugins me) {
		// 读取jdbc配置
		final String url = getProperty("jdbcUrl");
		final String username = getProperty("username");
		final String password = getProperty("password");
		final Integer initialSize = Integer.parseInt(getProperty("initialSize"));
		final Integer minIdle = Integer.parseInt(getProperty("minIdle"));
		final Integer maxActive = Integer.parseInt(getProperty("maxActive"));
		final String driverClass = getProperty("driverClass");

		DruidPlugin druidPlugin = new DruidPlugin(url, username, password, driverClass);
		druidPlugin.set(initialSize, minIdle, maxActive);
		druidPlugin.setFilters("stat,wall");// 监控统计："stat" ;防SQL注入："wall"
		me.add(druidPlugin);
		// 实体映射
		ActiveRecordPlugin arp = new ActiveRecordPlugin(druidPlugin);
		arp.setShowSql(true);
		arp.setContainerFactory(new OrderedFieldContainerFactory());// 字段有序，保持和查询的顺序一致
		// 设置sql存放的根路径
		String a= PathKit.getRootClassPath();
		String b= PathKit.getWebRootPath();
		arp.setBaseSqlTemplatePath(PathKit.getRootClassPath() + "/sql");
		arp.addSqlTemplate("all.sql");
		me.add(arp);
		// DB映射
		_MappingKit.mapping(arp);

		// 定时任务
		QuartzPlugin quartz = new QuartzPlugin();
		quartz.setJobs("job.properties");
		me.add(quartz);

		// 线程池插件
		ThreadPoolPlugin threadPool = new ThreadPoolPlugin();
		me.add(threadPool);

		// 邮件插件
		MailPlugin mailPlugin = new MailPlugin(PropKit.use("mail.properties").getProperties());
		me.add(mailPlugin);

		// 初始化事件插件
		EventPlugin plugin = new EventPlugin();
		plugin.async(); // 开启全局异步
		plugin.scanJar(); // 设置扫描jar包，默认不扫描
		// plugin.scanPackage("com.hotel.service.event"); // 设置监听器默认包，默认全扫描
		me.add(plugin);

	}

	@Override
	public void configInterceptor(Interceptors me) {
		me.add(new ShiroInterceptor());
//		me.add(new Tx());
		me.add(new CoreInterceptor());
		me.add(new SessionInViewInterceptor());
	}

	@Override
	public void configHandler(Handlers me) {

	}

	@Override
	public void configEngine(Engine me) {

	}

	@Override
	public void afterJFinalStart() {

//		CurrencyController currencyController = new CurrencyController();
//		currencyController.updateCurrency();

//		stratege1Executor executor = new stratege1Executor();
//		Vector<Currency> cs = executor.execute(null);

//		Vector<Currency> vector = new Vector<>();
//		vector.add(new Currency().setCode("11111").setName("11111"));
//		Strategy strategy = new Strategy().setDescribe("二三买重合");
//		MailUtil.sendMailToBuy(vector, strategy);

//		stratege1Executor executor = new stratege1Executor();
//		executor.execute(null);
		
//		KlineController klineController = new KlineController();
//		List<Currency> listAll = Currency.dao.listAll();
//		klineController.multiProData(listAll);
		
//		KlineController klineController = new KlineController();
//		List<Currency> listAll = Currency.dao.listAll();
//		for (Currency currency : listAll) {
//			klineController.downloadData(currency.getCode());
//		}
		
		//执行策略三
//		Executor executor1 = new Strategy3Executor();
//		executor1.execute("603288");
//		Executor executor2 = new Strategy4Executor();
//		executor2.execute(null);
//		Executor executor3 = new Strategy5Executor();
//		executor3.execute(null);
//		Executor executor4 = new Strategy4Executor();
//		executor4.execute(null);
//		AgentSyn agentSyn = new UpwardTrendStrategyAgent();
//		agentSyn.calibrate(DateUtil.strDate("2000-08-05","yyyy-MM-dd"),DateUtil.strDate("2020-02-08","yyyy-MM-dd"));

//		AgentSyn agentSyn = new CombinationStrategyAgent();
//		agentSyn.calibrate(DateUtil.strDate("2000-08-05","yyyy-MM-dd"),DateUtil.strDate("2020-02-08","yyyy-MM-dd"));

//		AgentSyn agentSyn = new ReverseCheckStrategyAgent();
//		agentSyn.calibrateCustomize(DateUtil.strDate("2020-01-07 10:00:00","yyyy-MM-dd HH:mm:ss"),DateUtil.strDate("2020-02-05 10:30:00","yyyy-MM-dd HH:mm:ss"), Calendar.MINUTE,30, Kline.KLINE_TYPE_M30K);

//		AgentSyn agentSyn = new BoxBreakStrategyAgent();
//		agentSyn.execute();
//		agentSyn.calibrate(DateUtil.strDate("2019-12-31","yyyy-MM-dd"),DateUtil.strDate("2020-02-16","yyyy-MM-dd"));
		//策略组合
//		JudgerExe judgerExe = new JudgerExe();
//		judgerExe.execute();
		// 验证器
//		Executor executor = new Strategy9Executor();
//		Calibrator calibrator = new Calibrator(executor);
//		Currency currency = Currency.dao.findByCode("600668");
//		calibrator.calibrate(currency);
//		executor.execute("601318");
//		List<Currency> allCurrency = Currency.dao.listAll();
//		for (int i = 0; i < allCurrency.size(); i++) {
//			System.out.println("这是第"+i+"只股票");
//			executor.execute(allCurrency.get(i).getCode());
//		}
	}

}
