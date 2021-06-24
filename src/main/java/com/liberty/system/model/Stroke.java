package com.liberty.system.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.SqlPara;
import com.liberty.system.model.base.BaseStroke;
import com.liberty.system.query.StrokeQueryObject;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class Stroke extends BaseStroke<Stroke> {
	// 向上笔
	public static final String STROKE_TYPE_UP = "0";
	// 向下笔
	public static final String STROKE_TYPE_DOWN = "1";

	private boolean fromGap;

	public static final Stroke dao = new Stroke().dao();

	private List<Kline> allKlines = new ArrayList<Kline>();

	/**
	 * 按时间升序的三笔,判断三笔是否重叠
	 *
	 * @param s1
	 * @param s2
	 * @param s3
	 * @return 0:重叠;1:不重叠:方向向上;2:不重叠:方向向下
	 */
	public int overlap(Stroke s1, Stroke s2, Stroke s3) {
		if ("0".equals(s1.getDirection()) && "0".equals(s3.getDirection())) {
			if (s1.getMin() > s3.getMax()) {
				return 2;
			}
		}
		if ("1".equals(s1.getDirection()) && "1".equals(s3.getDirection())) {
			if (s1.getMax() < s3.getMin()) {
				return 1;
			}
		}
		return 0;
	}

	/**
	 * 是否是缺口成笔
	 * 
	 * @return
	 */
	public boolean isFromGap() {
		return fromGap;
	}

	public void setFromGap(boolean fromGap) {
		this.fromGap = fromGap;
	}

	public void updateKline() {
		Db.batchUpdate(allKlines, 5000);
		allKlines.clear();
	}

	public boolean saveOrUpdate(String code, String type) {
		if (this.getId() != null) {
			return update(code, type);
		} else {
			return save(code, type);
		}
	}

	public boolean update(String code, String type) {
		try {
			super.update();
			Currency currency = Currency.dao.findByCode(code);
			List<Kline> klines = Kline.dao.getByDateRange(currency.getId(), type, this.getStartDate(), this.getEndDate());
			for (Kline kline : klines) {
				kline.setStrokeId(this.getId());
				allKlines.add(kline);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean save(String code, String type) {
		try {
			super.save();
			Currency currency = Currency.dao.findByCode(code);
			List<Kline> klines = Kline.dao.getByDateRange(currency.getId(), type, this.getStartDate(), this.getEndDate());
			for (Kline kline : klines) {
				kline.setStrokeId(this.getId());
				allKlines.add(kline);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public Page<Stroke> paginate(StrokeQueryObject qo) {
		SqlPara sqlPara = getSqlParaFromTemplate(Kv.by("qo", qo));
		return dao.paginate(qo.getCurrentPage(), qo.getPageSize(), sqlPara);
	}

	public Stroke getLastByCurrencyId(int currencyId, String type) {
		SqlPara sqlPara = getSqlParaFromTemplate(Kv.by("currencyId", currencyId).set("type", type));
		Stroke stroke = dao.findFirst(sqlPara);
		return stroke;
	}

	public List<Stroke> getLastSomeByCurrencyId(int currencyId, String type, int limit) {
		SqlPara sqlPara = getSqlParaFromTemplate(Kv.by("currencyId", currencyId).set("type", type).set("limit",limit));
		List<Stroke> strokes = dao.find(sqlPara);
		return strokes;
	}

	public List<Stroke> listAllByCurrencyId(int currencyId, String type) {
		SqlPara sqlPara = getSqlParaFromTemplate(Kv.by("currencyId", currencyId).set("type", type));
		List<Stroke> list = dao.find(sqlPara);
		return list;
	}

	public List<Stroke> listAfterByEndDate(int currencyId, String type, Date date) {
		SqlPara sqlPara = getSqlParaFromTemplate(Kv.by("currencyId", currencyId).set("type", type).set("date", date));
		List<Stroke> list = dao.find(sqlPara);
		return list;
	}

	public List<Stroke> getByDateRange(int currencyId, String type, Date startDate, Date endDate) {
		SqlPara sqlPara = getSqlParaFromTemplate(
				Kv.by("currencyId", currencyId).set("type", type).set("startDate", startDate).set("endDate", endDate));
		List<Stroke> list = dao.find(sqlPara);
		return list;
	}

	public List<Stroke> listAll() {
		String sql = getSqlFromTemplate();
		List<Stroke> list = dao.find(sql);
		return list;
	}

	public void deleteByCurrencyId(int currencyId) {
		SqlPara sqlPara = getSqlParaFromTemplate(Kv.by("currencyId",currencyId));
		int update = Db.update(sqlPara);
	}

	public Stroke getLastBeforeDate(int currencyId, String type, Date date) {
		SqlPara sqlPara = getSqlParaFromTemplate(Kv.by("currencyId", currencyId).set("type", type).set("date",date));
		Stroke stroke = dao.findFirst(sqlPara);
		return stroke;
	}

	public List<Stroke> listBeforeByEndDate(int currencyId, String type, Date date) {
		SqlPara sqlPara = getSqlParaFromTemplate(Kv.by("currencyId", currencyId).set("type", type).set("date",date));
		List<Stroke> strokes = dao.find(sqlPara);
		return strokes;
	}
}
