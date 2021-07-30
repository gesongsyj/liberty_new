package com.liberty.system.web;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfplugin.mail.MailKit;
import com.liberty.common.constant.ConstantDefine;
import com.liberty.common.plugins.threadPoolPlugin.ThreadPoolKit;
import com.liberty.common.utils.DateUtil;
import com.liberty.common.utils.ResultMsg;
import com.liberty.common.utils.ResultStatusCode;
import com.liberty.common.web.BaseController;
import com.liberty.system.downloader.DownLoader;
import com.liberty.system.downloader.impl.DfcfDownLoader;
import com.liberty.system.model.Currency;
import com.liberty.system.model.Kline;
import com.liberty.system.model.Line;
import com.liberty.system.model.Stroke;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class KlineController extends BaseController {

    private static final Map<String, String> klineTypeNumberMap;
    private static final Map<String, Integer> klineTypeBetweenMap;

    // 涨停幅度限制
    private static final double LIMIT_UP = 0.09;
    // 停止跟踪阈值
    private static final int STOP_FOLLOW_LIMIT = 20;

    // 默认下载器
    private DownLoader downLoader = new DfcfDownLoader();

    public void setDownLoader(DownLoader downLoader) {
        this.downLoader = downLoader;
    }

    static {
        klineTypeNumberMap = new HashMap<String, String>();
        klineTypeNumberMap.put("1", "-200000");// 5分钟线
        // klineTypeNumberMap.put("1", "-1440");// 5分钟线
        klineTypeNumberMap.put("2", "-960");// 15分钟线
        klineTypeNumberMap.put("3", "-960");// 30分钟线
        klineTypeNumberMap.put("4", "-720");// 60分钟线
        klineTypeNumberMap.put("5", "-1000");// 日线
        klineTypeNumberMap.put("6", "-520");// 周线
        klineTypeNumberMap.put("9", "-120");// 月线

        klineTypeBetweenMap = new HashMap<String, Integer>();
        klineTypeBetweenMap.put("1", 5 * 60 * 1000);
        klineTypeBetweenMap.put("2", 15 * 60 * 1000);
        klineTypeBetweenMap.put("3", 30 * 60 * 1000);
        klineTypeBetweenMap.put("4", 60 * 60 * 1000);
        klineTypeBetweenMap.put("5", 24 * 60 * 60 * 1000);
        klineTypeBetweenMap.put("6", 7 * 24 * 60 * 60 * 1000);
        klineTypeBetweenMap.put("9", 31 * 24 * 60 * 60 * 1000);
    }

    @SuppressWarnings("deprecation")
    public static void main(String[] args) {
        List<List<Integer>> a = new ArrayList<>();
        List<Integer> b = new ArrayList<>();
        b.add(1);
        b.add(2);
        b.add(3);
        a.add(b);
        a.add(b);
        System.err.println(a);
    }

    /**
     * 更新该股的k线,笔,线段数据
     */
//	@Before(Tx.class)
    public void updateData() {
        String code = paras.get("code");
        KlineController klineController = new KlineController();
        klineController.downloadData(code);
        klineController.createStroke(code);
        klineController.createLine(code);
        Currency currency = Currency.dao.findByCode(code);
        redirect("/kline/charts?currencyId=" + currency.getId());
    }

    /**
     * 查看k线,笔,线段图表
     */
    public void charts() {
        String currencyId = paras.get("currencyId");
        if (currencyId == null) {
            renderJson(new ResultMsg(ResultStatusCode.INVALID_INPUT));
            return;
        }
        Currency currency = Currency.dao.findById(currencyId);
        setAttr("code", currency.getCode());
        render("kline.html");
    }

    /**
     * 获取图表数据
     */
    public void fetchData() {
        String code = paras.get("code");
        String kType = paras.containsKey("kType") ? paras.get("kType") : ConstantDefine.KLINE_TYPE_K;
        if (code == null) {
            renderJson(new ResultMsg(ResultStatusCode.INVALID_INPUT));
            return;
        }
        Currency currency = Currency.dao.findByCode(code);
        List<Kline> allKlines = Kline.dao.listAllByCurrencyId(currency.getId(), kType);
        List<Stroke> allStrokes = Stroke.dao.listAllByCurrencyId(currency.getId(), kType);
        List<Line> allLines = Line.dao.listAllByCode(code, kType);

        List<Map<String, Object>> bospList = new ArrayList<>();

        List<List<Object>> klines = new ArrayList<List<Object>>();
        for (int i = 0; i < allKlines.size(); i++) {
            Kline kline = allKlines.get(i);
            List<Object> klineData = new ArrayList<Object>();
            klineData.add(kline.getDate());
            klineData.add(kline.getOpen());
            klineData.add(kline.getClose());
            klineData.add(kline.getMin());
            klineData.add(kline.getMax());
            klineData.add(kline.getDiff());// index:5
            klineData.add(kline.getDea());
            klineData.add(kline.getBar());
            klines.add(klineData);

            List<Object> point = new ArrayList<>();
            Map<String,Object> bospMap = new HashMap<>();
            Map<String,Object> style = new HashMap<>();
            Map<String,Object> normal = new HashMap<>();
            if(Kline.BUY_POINT.equals(kline.getBosp())){
                point.add(kline.getDate());
                point.add(kline.getMin());
                bospMap.put("coord",point);
                bospList.add(bospMap);
            }
            if(Kline.SALE_POINT.equals(kline.getBosp())){
                point.add(kline.getDate());
                point.add(kline.getMax());
                normal.put("color","rgb(41,60,85)");
                style.put("normal",normal);
                bospMap.put("coord",point);
                bospMap.put("itemStyle",style);
                bospList.add(bospMap);
            }

        }

        List<List<Object>> strokes = new ArrayList<List<Object>>();
        for (int i = 0; i < allStrokes.size(); i++) {
            List<Object> strokeNums = new ArrayList<Object>();
            strokeNums.add(allStrokes.get(i).getStartDate());
            if ("0".equals(allStrokes.get(i).getDirection())) {
                strokeNums.add(allStrokes.get(i).getMin());
            } else {
                strokeNums.add(allStrokes.get(i).getMax());
            }
            strokes.add(strokeNums);
            if (i == allStrokes.size() - 1) {
                List<Object> strokeNums2 = new ArrayList<Object>();
                strokeNums2.add(allStrokes.get(i).getEndDate());
                if ("0".equals(allStrokes.get(i).getDirection())) {
                    strokeNums2.add(allStrokes.get(i).getMax());
                } else {
                    strokeNums2.add(allStrokes.get(i).getMin());
                }
                strokes.add(strokeNums2);
            }
        }

        List<List<Map<String, Object>>> lines = new ArrayList<List<Map<String, Object>>>();
        List<List<Object>> lineStrokes = new ArrayList<List<Object>>();
        for (int i = 0; i < allLines.size(); i++) {
            List<Map<String, Object>> perLine = new ArrayList<Map<String, Object>>();
            Map<String, Object> start = new HashMap<String, Object>();
            List<Object> startNum = new ArrayList<Object>();
            Map<String, Object> end = new HashMap<String, Object>();
            List<Object> endNum = new ArrayList<Object>();
            startNum.add(DateUtil.dateStr(allLines.get(i).getStartDate(), "yyyy-MM-dd HH:mm:ss"));
            if ("0".equals(allLines.get(i).getDirection())) {
                startNum.add(allLines.get(i).getMin());
            } else {
                startNum.add(allLines.get(i).getMax());
            }
            start.put("coord", startNum);

            endNum.add(DateUtil.dateStr(allLines.get(i).getEndDate(), "yyyy-MM-dd HH:mm:ss"));
            if ("0".equals(allLines.get(i).getDirection())) {
                endNum.add(allLines.get(i).getMax());
            } else {
                endNum.add(allLines.get(i).getMin());
            }
            end.put("coord", endNum);
            perLine.add(start);
            perLine.add(end);
            lines.add(perLine);

            List<Object> strokeLineNums = new ArrayList<Object>();
            strokeLineNums.add(allLines.get(i).getStartDate());
            if ("0".equals(allLines.get(i).getDirection())) {
                strokeLineNums.add(allLines.get(i).getMin());
            } else {
                strokeLineNums.add(allLines.get(i).getMax());
            }
            if (i == allLines.size() - 1) {
                strokeLineNums.add(allLines.get(i).getEndDate());
                if ("0".equals(allLines.get(i).getDirection())) {
                    strokeLineNums.add(allLines.get(i).getMax());
                } else {
                    strokeLineNums.add(allLines.get(i).getMin());
                }
            }
            lineStrokes.add(strokeLineNums);
        }

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("currency", currency);
        resultMap.put("klines", klines);
        resultMap.put("strokes", strokes);
        resultMap.put("lines", lines);
        resultMap.put("lineStrokes", lineStrokes);
        resultMap.put("bospList",bospList);
        renderJson(new ResultMsg(ResultStatusCode.OK, resultMap));
    }

    /**
     * K线数据下载 财经网站爬取数据
     */
//	@Before(Tx.class)
    public void downloadData(String includeCurrencyCode) {
        String sql = "select * from dictionary where type='klineType_gp'";
        List<Record> klineType = Db.find(sql);
        Map<String, List<Kline>> klineMap = new HashMap<String, List<Kline>>();
        Map<String, Kline> lastKlineMap = new HashMap<String, Kline>();
        for (Record record : klineType) {
            Currency currency = Currency.dao.findByCode(includeCurrencyCode);
            // 取出最后两条数据,最新的一条数据可能随时变化,新增数据时此条记录先删除
            List<Kline> lastTwo = Kline.dao.getLast2ByCurrencyId(currency.getId(), record.getStr("key"));
            Kline lastKline = null;
            if (lastTwo == null || lastTwo.size() <= 1) {
                if (lastTwo.size() == 1) {
                    lastTwo.get(0).delete();
                }
            } else {
                lastTwo.get(0).delete();
                lastKline = lastTwo.get(1);
                lastKlineMap.put(includeCurrencyCode + "_" + record.getStr("key"), lastKline);
            }
            List<Kline> klineList = null;

            try {
                klineList = this.downLoader.downLoad(currency, record.getStr("key"), "get", lastKline);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("下载数据错误!");
            }
            if (klineList == null || klineList.size() == 0) {
                continue;
            }
//            if (null != lastKline && klineList.get(0).getDate().getTime() < lastKline.getDate().getTime()) {
//                lastKlineMap.put(includeCurrencyCode + "_" + record.getStr("key"), null);
//            }

            klineMap.put(currency.getCode() + "_" + record.getStr("key"), klineList);
            Kline.dao.saveMany(klineMap, lastKlineMap);
            klineMap.clear();
            lastKlineMap.clear();
        }
//        renderText("ok");
    }

    /**
     * 根据k线数据生成笔
     *
     * @param includeCurrencyCode
     */
//	@Before(Tx.class)
    public void createStroke(String includeCurrencyCode) {
        Currency currency = Currency.dao.findByCode(includeCurrencyCode);
        String sql = "select * from dictionary where type='klineType_gp'";
        List<Record> klineType = Db.find(sql);
        for (Record record : klineType) {
            System.out.println(record);
            List<Kline> klines;
            // 查询最后一笔
            Stroke lastStroke = Stroke.dao.getLastByCurrencyId(currency.getId(), record.getStr("key"));
            if (lastStroke == null) {
                klines = Kline.dao.listAllByCurrencyId(currency.getId(), record.getStr("key"));
            } else {
                // 查询最后一笔之后的K线
                Date date = lastStroke.getEndDate();
                klines = Kline.dao.getListAfterDate(currency.getId(), record.getStr("key"), date);
            }
            if (klines.isEmpty()) {
                continue;
            }
            // 处理K线的包含关系
            handleInclude(klines, lastStroke);
            // 生成笔
//            List<Stroke> strokes = processStrokes(klines, lastStroke);
            List<Stroke> strokes = processStrokes_new(klines, lastStroke);
        }

        renderText("ok");
    }

//    @Before(Tx.class)
    public void createStroke_bak(String includeCurrencyCode) {
        Currency currency = Currency.dao.findByCode(includeCurrencyCode);
        String sql = "select * from dictionary where type='klineType_gp'";
        List<Record> klineType = Db.find(sql);
        for (Record record : klineType) {
            System.out.println(record);
            List<Kline> klines;
            // 查询最后一笔
            Stroke lastStroke = Stroke.dao.getLastByCurrencyId(currency.getId(), record.getStr("key"));
            if (lastStroke == null) {
                klines = Kline.dao.listAllByCurrencyId(currency.getId(), record.getStr("key"));
            } else {
                // 查询最后一笔之后的K线
                Date date = lastStroke.getEndDate();
                klines = Kline.dao.getListAfterDate(currency.getId(), record.getStr("key"), date);
            }
            if (klines.isEmpty()) {
                continue;
            }
            // 处理K线的包含关系
            handleInclude(klines, lastStroke);
            // 生成笔
            List<Stroke> strokes = processStrokes(klines, lastStroke);
        }

        renderText("ok");
    }

    /**
     * 根据笔数据生成线段
     *
     * @param includeCurrencyCode
     */
//	@Before(Tx.class)
    public void createLine(String includeCurrencyCode) {
        List<Stroke> strokes = null;

        Currency currency = Currency.dao.findByCode(includeCurrencyCode);

        String sql = "select * from dictionary where type='klineType_gp'";
        List<Record> klineType = Db.find(sql);
        for (Record record : klineType) {
            // 生成的线段
            List<Line> storeLines = new ArrayList<>();
            Line lastLine = Line.dao.getLastByCode(includeCurrencyCode, record.getStr("key"));
            if (lastLine == null) {
                strokes = Stroke.dao.listAllByCurrencyId(currency.getId(), record.getStr("key"));
                if (strokes == null || strokes.size() == 0) {
                    continue;
                }
            } else {
                storeLines.add(lastLine);
                // 查询最后一条线段后的笔
                Date date = lastLine.getEndDate();
                // 用多线程机制
                strokes = Stroke.dao.listAfterByEndDate(currency.getId(), record.getStr("key"), date);
                if (strokes == null || strokes.size() == 0) {
                    continue;
                }
                // 最后一笔的结束点有变动,最后一条线段的结束点未变
                if (0 != strokes.get(0).getStartDate().compareTo(lastLine.getEndDate())) {
                    lastLine.setEndDate(strokes.get(0).getEndDate()).update();
                    if (Line.LINE_TYPE_UP.equals(lastLine.getDirection())) {
                        lastLine.setMax(strokes.get(0).getMax());
                    } else {
                        lastLine.setMin(strokes.get(0).getMin());
                    }
                    lastLine.saveOrUpdate(currency.getId(), record.getStr("key"));
                    strokes.remove(0);
                }
            }
            if (strokes.size() >= 3) {
                loopProcessLines3(strokes, storeLines);
            }
        }

    }

    /**
     * 多线程下载 处理数据
     */
    public void multiProData(List<Currency> cs) {
        multiProData(cs,true);
    }

    /**
     * 多线程下载 处理数据
     */
    public void multiProData(List<Currency> cs,boolean sendMail) {
        long start = System.currentTimeMillis();
        ThreadPoolExecutor executor = ThreadPoolKit.getExecutor();
        int queueSize = executor.getQueue().remainingCapacity();
		List<Future> futureList = new ArrayList<>();
		for (int i = 0; i < cs.size(); i++) {
            for (int j = 0; j < queueSize && i < cs.size(); j++, i++) {
                int index = i;
                Future<?> future = executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        Currency currency = cs.get(index);
                        downloadData(currency.getCode());
                        createStroke(currency.getCode());
                        createLine(currency.getCode());
                    }
                });
                futureList.add(future);
				System.out.println("当前线程池信息: \n" + "存活线程数===" + executor.getActiveCount() + ";\n完成任务数===" + executor.getCompletedTaskCount() + ";\n总任务数===" + executor.getTaskCount());
            }
            i--;
		}
		for (Future future : futureList) {
			try {
				future.get();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		long end = System.currentTimeMillis();
        double time = (end - start) * 1.0 / 1000 / 60;
        if(sendMail){
            MailKit.send("1971119509@qq.com", null, "更新数据库股票数据耗时提醒!", "此次更新数据耗时:" + time + "分钟!");
        }
    }

    /**
     * 多线程下载 处理数据
     */
    public void multiProData_bak(List<Currency> cs) {
        long start = System.currentTimeMillis();
        ThreadPoolExecutor executor = ThreadPoolKit.getExecutor();
        List<Future> futureList = new ArrayList<>();
        for (int i = 0; i < cs.size(); i++) {
            int index = i;
            Currency currency = cs.get(i);
            downloadData(currency.getCode());
            Future<?> future = executor.submit(new Runnable() {
                @Override
                public void run() {
                    Currency currency = cs.get(index);
                    createStroke(currency.getCode());
                    createLine(currency.getCode());
                }
            });
            futureList.add(future);
			System.out.println("当前线程池信息: \n" + "存活线程数===" + executor.getActiveCount() + ";\n完成任务数===" + executor.getCompletedTaskCount() + ";\n总任务数===" + executor.getTaskCount());
        }
        for (Future future : futureList) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        long end = System.currentTimeMillis();
        double time = (end - start) * 1.0 / 1000 / 60;
        MailKit.send("1971119509@qq.com", null, "更新数据库股票数据耗时提醒!", "此次更新数据耗时:" + time + "分钟!");
    }

    /**
     * 处理跟踪标记,包括移除和跟踪
     */
    public void handleFollowed(){
        removeFollowed();
        followLimitUp();
    }

    /**
     * 给涨停股添加标记
     */
    public void followLimitUp(){
        List<Currency> cs = Currency.dao.listAll();
        if(null != cs){
            for (Currency c : cs) {
                Kline last1 = Kline.dao.getLastOneByCurrencyId(c.getId(), Kline.KLINE_TYPE_K);
                if(null != last1 && null != last1.getAoi()){
                    if(last1.getAoi()>LIMIT_UP){
                        c.setFollowed(true);
                        c.setFollowedDate(last1.getDate());
                        c.update();
                    }
                }
            }
        }
    }

    /**
     * 移除followed标志
     */
    public void removeFollowed(){
        List<Currency> cs = Currency.dao.listFollowed();
        if(null != cs){
            for (Currency c : cs) {
                Kline last1 = Kline.dao.getLastOneByCurrencyId(c.getId(), Kline.KLINE_TYPE_K);
                if(null != last1){
                    List<Kline> byDateRange = Kline.dao.getByDateRange(c.getId(), Kline.KLINE_TYPE_K, c.getFollowedDate(), last1.getDate());
                    if(byDateRange.size()>=STOP_FOLLOW_LIMIT){
                        c.setFollowed(false);
                        c.setFollowedDate(null);
                        c.update();
                    }
                }
            }
        }
    }

}
