package com.liberty.system.strategy.agent;

import java.util.Date;

public interface AgentSyn {
    void execute();

    /**
     * 策略验证,不包含startDate,包含endDate
     * @param startDate
     * @param endDate
     */
    void calibrate(Date startDate,Date endDate);

    /**
     * 策略验证,不包含startDate,包含endDate
     * @param startDate
     * @param endDate
     */
    void calibrateCustomize(Date startDate,Date endDate,String klineType);
}
