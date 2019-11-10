package com.liberty.system.downloader.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.liberty.common.utils.DateUtil;
import com.liberty.common.utils.HTTPUtils;
import com.liberty.system.downloader.DownLoader;
import com.liberty.system.model.Currency;
import com.liberty.system.model.Kline;

public class HxDownLoader implements DownLoader {
	private static final Map<String, String> klineTypeNumberMap;
	private static final Map<String, String> paramTypeMap;
	private static final Map<String, Integer> klineTypeBetweenMap;
	static {
		paramTypeMap = new HashMap<String, String>();
		paramTypeMap.put("2", "1");// 5分钟线
		paramTypeMap.put("3", "2");// 15分钟线
		paramTypeMap.put("4", "3");// 30分钟线
		paramTypeMap.put("5", "4");// 60分钟线
		paramTypeMap.put("7", "5");// 日线
		paramTypeMap.put("8", "6");// 周线
		paramTypeMap.put("9", "9");// 月线

		klineTypeNumberMap = new HashMap<String, String>();
		klineTypeNumberMap.put("2", "-200000");// 5分钟线
		// klineTypeNumberMap.put("1", "-1440");// 5分钟线
		klineTypeNumberMap.put("3", "-960");// 15分钟线
		klineTypeNumberMap.put("4", "-960");// 30分钟线
		klineTypeNumberMap.put("5", "-720");// 60分钟线
		klineTypeNumberMap.put("7", "-1000");// 日线
		klineTypeNumberMap.put("8", "-520");// 周线
		klineTypeNumberMap.put("9", "-120");// 月线

		klineTypeBetweenMap = new HashMap<String, Integer>();
		klineTypeBetweenMap.put("2", 5 * 60 * 1000);
		klineTypeBetweenMap.put("3", 15 * 60 * 1000);
		klineTypeBetweenMap.put("4", 30 * 60 * 1000);
		klineTypeBetweenMap.put("5", 60 * 60 * 1000);
		klineTypeBetweenMap.put("7", 24 * 60 * 60 * 1000);
		klineTypeBetweenMap.put("8", 7 * 24 * 60 * 60 * 1000);
		klineTypeBetweenMap.put("9", 31 * 24 * 60 * 60 * 1000);
	}

	@Override
	public List<Kline> downLoad(Currency currency, String type, String method, Date lastDate) {
		Map<String, String> params = new HashMap<String, String>();
		List<Kline> klineList = new ArrayList<Kline>();
		String response = "";
		String dataUrl = "http://webforex.hermes.hexun.com/forex/kline";

		Date now = new Date();
		Date tomorrow = DateUtil.getNextDay(now);
		String tomorrowFormat = new SimpleDateFormat("yyyyMMdd").format(tomorrow);

		params.put("start", tomorrowFormat + "080000");
		params.put("type", paramTypeMap.get(type));// K线级别
		params.put("code", "FOREX" + currency.getCode());// 设置code参数
		if (lastDate == null) {
			if (paramTypeMap.get(type) == null) {
				return null;// 没有该级别K线的数据
			} else {
				params.put("number", klineTypeNumberMap.get(type) == null ? "-1000" : klineTypeNumberMap.get(type));
			}
		} else {
			long between = DateUtil.getNumberBetween(DateUtil.getNextDay(now), lastDate, klineTypeBetweenMap.get(type));
			String number = String.valueOf(between);
			params.put("number", "-" + number);
			// params.put("number", "-" + "10");//测试
		}
		try {
			response = HTTPUtils.http(dataUrl, params, "get");
			response = response.substring(response.indexOf("{"));
			response = response.substring(0, response.lastIndexOf("}") + 1);
			Map<String, Object> responseMap = JSON.parseObject(response, Map.class);
			Object object = responseMap.get("Data");
			JSONArray parseArray = JSON.parseArray(object.toString());
			Object startDate = parseArray.get(1);// 开始时间
			Object endDate = parseArray.get(2);// 结束时间
			Object priceMul = parseArray.get(4);// 价格倍数

			JSONArray dataArray = JSON.parseArray(parseArray.get(0).toString());// 数据数组
			for (Object object2 : dataArray) {
				Kline kline = new Kline();
				JSONArray parseArray2 = JSON.parseArray(object2.toString());
				kline.setDate(DateUtil.strDate(parseArray2.get(0).toString(), "yyyyMMddHHmmss"));
				kline.setMax(Double.valueOf(parseArray2.get(4).toString()) / Double.valueOf(priceMul.toString()));
				kline.setMin(Double.valueOf(parseArray2.get(5).toString()) / Double.valueOf(priceMul.toString()));
				// kline.setCurrencyId(currency.getId());
				// kline.setType(record.getStr("key"));

				klineList.add(kline);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return klineList;
	}

	@Override
	public List<Kline> downLoad(Currency currency, String type, String method, int size) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void downLoadRealTimeData(Currency currency, String type, String method) {
		// TODO Auto-generated method stub
		
	}

}
