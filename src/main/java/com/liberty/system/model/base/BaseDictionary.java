package com.liberty.system.model.base;

import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseDictionary<T extends BaseDictionary> extends SuperModel<T> implements IBean {

	public T setId(java.lang.Integer id) {
		set("id", id);
		return (T)this;
	}
	
	public java.lang.Integer getId() {
		return getInt("id");
	}

	public T setKey(java.lang.String key) {
		set("key", key);
		return (T)this;
	}
	
	public java.lang.String getKey() {
		return getStr("key");
	}

	public T setValue(java.lang.String value) {
		set("value", value);
		return (T)this;
	}
	
	public java.lang.String getValue() {
		return getStr("value");
	}

	public T setType(java.lang.String type) {
		set("type", type);
		return (T)this;
	}
	
	public java.lang.String getType() {
		return getStr("type");
	}

	public T setDescription(java.lang.String description) {
		set("description", description);
		return (T)this;
	}
	
	public java.lang.String getDescription() {
		return getStr("description");
	}

}
