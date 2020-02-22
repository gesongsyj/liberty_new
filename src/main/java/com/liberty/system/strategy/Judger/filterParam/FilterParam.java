package com.liberty.system.strategy.Judger.filterParam;

import com.liberty.system.model.Currency;
import com.liberty.system.strategy.Judger.Judger;

import java.util.Date;

public interface FilterParam {
    Judger initJudger();

    boolean judge(Currency currency,Date date);
}
