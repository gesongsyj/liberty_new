package com.liberty.system.web.routine;

import com.liberty.common.web.BaseController;
import com.liberty.system.model.Currency;
import com.liberty.system.strategy.agent.AgentSyn;
import com.liberty.system.strategy.agent.impl.ReverseCheckStrategyAgent;
import com.liberty.system.web.KlineController;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;

/**
 * 背驰判断定时任务
 */
public class ReverseRoutine extends BaseController implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 更新数据
        KlineController klineController = new KlineController();
        List<Currency> listFollowed = Currency.dao.listFollowed();
        klineController.multiProData(listFollowed);
        // 判断背驰
        AgentSyn agentSyn = new ReverseCheckStrategyAgent();
        agentSyn.execute();
    }
}
