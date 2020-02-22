package com.liberty.system.strategy.Judger;

import com.liberty.system.model.Currency;

import java.util.Date;

public interface Judger {
    boolean judgeItem(Currency currency,Date date);
}
