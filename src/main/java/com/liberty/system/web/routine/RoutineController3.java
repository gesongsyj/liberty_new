package com.liberty.system.web.routine;


import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.liberty.common.web.BaseController;
import com.liberty.system.strategy.cuttor.LossCuttor;


/**
 * 定时任务
 */
public class RoutineController3 extends BaseController implements Job {

	/**
	 * 止损器
	 */
	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		LossCuttor cuttor = new LossCuttor();
		cuttor.cut();
	}
	
}
