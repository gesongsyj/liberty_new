package com.liberty.system.web.routine;

import java.util.ArrayList;
import java.util.List;

import com.liberty.system.strategy.executor.Executor;
import com.liberty.system.strategy.executor.job.Strategy12Executor;
import com.liberty.system.web.KlineController;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.jfplugin.mail.MailKit;
import com.liberty.common.utils.MailUtil;
import com.liberty.common.web.BaseController;
import com.liberty.system.model.Currency;

/**
 * 定时任务
 */
public class RoutineController1 extends BaseController implements Job {

    /**
     * 更新数据和策略执行
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        KlineController klineController = new KlineController();
        List<Currency> listAll = Currency.dao.listAll();
        klineController.multiProData(listAll,true);
        // 处理标记
        klineController.handleFollowed();
        List<Executor> exes = new ArrayList<Executor>();
        exes.add(new Strategy12Executor());
        for (Executor executor : exes) {
            executor.execute(null);
        }
    }

}
