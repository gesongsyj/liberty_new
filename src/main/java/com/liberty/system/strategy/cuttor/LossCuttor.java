package com.liberty.system.strategy.cuttor;

import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.liberty.common.utils.MailUtil;
import com.liberty.system.downloader.DownLoader;
import com.liberty.system.downloader.impl.DfcfDownLoader;
import com.liberty.system.model.Currency;
import com.liberty.system.model.Kline;
import com.liberty.system.model.Strategy;

/**
 * 止损提示器
 * @author Administrator
 *
 */
public class LossCuttor{
	public void cut() {
		List<Record> rs = Db.find("select *,cs.id as csId from currency c RIGHT JOIN currency_strategy cs on cs.currencyId=c.id where cs.cutLine IS NOT NULL");
		DownLoader downLoader = new DfcfDownLoader();
		for (Record record : rs) {
			Currency currency = Currency.dao.findById(record.getInt("currencyId"));
			Strategy strategy = Strategy.dao.findById(record.getInt("strategyId"));
			List<Kline> klinesDate = Kline.dao.getListAfterDate(currency.getId(), Kline.KLINE_TYPE_K, record.getDate("startDate"));
			List<Kline> klines = downLoader.downLoad(currency, "k", "get", klinesDate.get(klinesDate.size()-1));
			for (Kline kline : klines) {
				if(kline.getMin()<record.getDouble("cutLine")) {
					MailUtil.sendMailToSale(currency, strategy, record.getDouble("cutLine"),record.getInt("csId"));
				}
			}
		}
		
	}
}
