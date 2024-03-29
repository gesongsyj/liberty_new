package com.liberty.system.strategy.executor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import com.liberty.system.model.Currency;
import com.liberty.system.model.Strategy;

public interface Executor {
	Strategy getStrategy();

	Vector<Currency> execute(String code);

	String getExecuteDate();

	void setExecuteDate(String executeDate);

	boolean isCalibrate();

	void setCalibrate(boolean value);

	boolean isOnlyK();

	void setOnlyK(boolean onlyK);
}
