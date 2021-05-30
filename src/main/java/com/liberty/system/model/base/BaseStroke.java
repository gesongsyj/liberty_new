package com.liberty.system.model.base;

import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseStroke<T extends BaseStroke> extends SuperModel<T> implements IBean {

	public T setId(java.lang.Integer id) {
		set("id", id);
		return (T)this;
	}
	
	public java.lang.Integer getId() {
		return getInt("id");
	}

	public T setMax(java.lang.Double max) {
		set("max", max);
		return (T)this;
	}
	
	public java.lang.Double getMax() {
		return getDouble("max");
	}

	public T setMin(java.lang.Double min) {
		set("min", min);
		return (T)this;
	}
	
	public java.lang.Double getMin() {
		return getDouble("min");
	}

	public T setStartDate(java.util.Date startDate) {
		set("startDate", startDate);
		return (T)this;
	}
	
	public java.util.Date getStartDate() {
		return get("startDate");
	}

	public T setEndDate(java.util.Date endDate) {
		set("endDate", endDate);
		return (T)this;
	}
	
	public java.util.Date getEndDate() {
		return get("endDate");
	}

	public T setCurrencyId(java.lang.Integer currencyId) {
		set("currencyId", currencyId);
		return (T)this;
	}
	
	public java.lang.Integer getCurrencyId() {
		return getInt("currencyId");
	}

	public T setLineId(java.lang.Integer lineId) {
		set("lineId", lineId);
		return (T)this;
	}
	
	public java.lang.Integer getLineId() {
		return getInt("lineId");
	}

	public T setPrevId(java.lang.Integer prevId) {
		set("prevId", prevId);
		return (T)this;
	}
	
	public java.lang.Integer getPrevId() {
		return getInt("prevId");
	}

	public T setNextId(java.lang.Integer nextId) {
		set("nextId", nextId);
		return (T)this;
	}
	
	public java.lang.Integer getNextId() {
		return getInt("nextId");
	}

	/**
	 * 就是方向?
	 * @param type
	 * @return
	 */
	public T setType(java.lang.String type) {
		set("type", type);
		return (T)this;
	}
	
	public java.lang.String getType() {
		return getStr("type");
	}

	public T setDirection(java.lang.String direction) {
		set("direction", direction);
		return (T)this;
	}
	
	public java.lang.String getDirection() {
		return getStr("direction");
	}

}
