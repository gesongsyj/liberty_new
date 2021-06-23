package com.liberty.system.web;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Page;
import com.liberty.common.plugins.threadPoolPlugin.ThreadPoolKit;
import com.liberty.common.utils.DateUtil;
import com.liberty.common.utils.ResultMsg;
import com.liberty.common.utils.ResultStatusCode;
import com.liberty.common.web.BaseController;
import com.liberty.system.model.Currency;
import com.liberty.system.model.Stroke;
import com.liberty.system.query.StrokeQueryObject;
import com.liberty.system.strategy.calibrator.Calibrator;
import com.liberty.system.strategy.executor.Executor;
import com.liberty.system.strategy.executor.job.Strategy9Executor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class StrokeController extends BaseController {

    public void testSave() {
        for (int i = 0; i < 20; i++) {
            new Stroke().set("id", i + 100).save();
        }
        renderText("添加");
    }

    public void testPage() {
        StrokeQueryObject qo = new StrokeQueryObject();
        // qo.setKeyword("11");
        // qo.setParentId(110L);
        Page<Stroke> paginate = Stroke.dao.paginate(qo);
        System.out.println(paginate.getList());
        renderText("查询");
    }

    /**
     * http://localhost:8080/stroke/calibrate
     * http://localhost:8080/stroke/calibrate?code=600668
     * http://localhost:8080/stroke/calibrate?code=600668&startDate=2019-08-08
     */
    public void calibrate() {
        // 验证器
        Executor executor = new Strategy9Executor();
        Calibrator calibrator = new Calibrator(executor);
        String code = paras.get("code");
        List<Currency> currencies = new ArrayList<>();
        if (code == null) {
            currencies = Currency.dao.listAll();
        } else {
            Currency currency = Currency.dao.findByCode(code);
            currencies.add(currency);
        }
        String startDateStr = paras.get("startDate");
        final List<Currency> cs = currencies;
        ThreadPoolExecutor pool = ThreadPoolKit.getExecutor();
        pool.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10000);
                    while (pool.getQueue().size()>0) {
                        System.out.println("=========>当前线程池信息: \n" + "存活线程数===" + pool.getActiveCount() + ";\n完成任务数===" + pool.getCompletedTaskCount() + ";\n总任务数===" + pool.getTaskCount());
                        Thread.sleep(10000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        List<Future> futureList = new ArrayList<>();
        for (int i = 0; i < cs.size(); i++) {
            int index = i;
            Future<?> future = pool.submit(new Runnable() {
                @Override
                public void run() {
                    Currency currency = cs.get(index);
                    if (startDateStr != null) {
                        Date startDate = DateUtil.strDate(startDateStr, "yyyy-MM-dd");
                        calibrator.calibrate(currency, startDate);
                    } else {
                        calibrator.calibrate(currency, null);
                    }
                }
            });
            futureList.add(future);
            System.out.println("当前线程池信息: \n" + "存活线程数===" + pool.getActiveCount() + ";\n完成任务数===" + pool.getCompletedTaskCount() + ";\n总任务数===" + pool.getTaskCount());
        }
        for (Future future : futureList) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        renderText("ok");
    }
}
