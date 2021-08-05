package com.liberty.system.strategy.executor.job;

import com.liberty.common.utils.DateUtil;
import com.liberty.system.strategy.executor.Executor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExecutorFactory {
    public static List<Executor> executorList = new ArrayList<>(20);

    static {
        executorList.add(new Strategy1Executor());
        executorList.add(new Strategy2Executor());
        executorList.add(new Strategy3Executor());
        executorList.add(new Strategy4Executor());
        executorList.add(new Strategy5Executor());
        executorList.add(new Strategy6Executor());
        executorList.add(new Strategy7Executor());
        executorList.add(new Strategy8Executor());
        executorList.add(new Strategy9Executor());
        executorList.add(new Strategy10Executor());
        executorList.add(new Strategy11Executor());
        executorList.add(new Strategy12Executor());
    }

    public static Executor buildExecutor(int executorIndex) {
        return buildExecutor(executorIndex, true, false, DateUtil.getDay());
    }

    public static Executor buildExecutor(int executorIndex, boolean isCalibrate, boolean isOnlyK) {
        return buildExecutor(executorIndex, isCalibrate, isOnlyK, DateUtil.getDay());
    }

    public static Executor buildExecutor(int executorIndex, boolean isCalibrate, boolean isOnlyK, String executeDate) {
        Executor executor = executorList.get(executorIndex - 1);
        executor.setCalibrate(isCalibrate);
        executor.setOnlyK(isOnlyK);
        executor.setExecuteDate(executeDate);
        return executor;
    }
}
