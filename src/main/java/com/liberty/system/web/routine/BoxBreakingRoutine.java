package com.liberty.system.web.routine;

import com.liberty.common.utils.DateUtil;
import com.liberty.common.web.BaseController;
import com.liberty.system.strategy.agent.AgentSyn;
import com.liberty.system.strategy.agent.impl.BoxBreakStrategyAgent;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class BoxBreakingRoutine extends BaseController implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        AgentSyn agentSyn = new BoxBreakStrategyAgent();
        agentSyn.execute();
    }
}
