package com.liberty.system.downloader.impl;

import com.alibaba.fastjson.JSON;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.liberty.common.utils.HTTPUtils;
import com.liberty.system.downloader.CurrencyKit;
import com.liberty.system.model.Currency;

import java.util.ArrayList;
import java.util.List;

public class CurrencyKit_Gp implements CurrencyKit {
	private int pagesize = 1;// 5页股票排行数据,每页20条,共100条数据
	private final String ex_url_shanghai = "http://nufm.dfcfw.com/EM_Finance2014NumericApplication/JS.aspx?type=CT&token=4f1862fc3b5e77c150a2b985b12db0fd&sty=FCOIATC&cmd=C.2&st=(ChangePercent)&sr=-1&p=1&ps=20&_=1538047395924";
	private final String ex_url_shenzhen = "http://nufm.dfcfw.com/EM_Finance2014NumericApplication/JS.aspx?type=CT&token=4f1862fc3b5e77c150a2b985b12db0fd&sty=FCOIATC&cmd=C._SZAME&st=(ChangePercent)&sr=-1&p=1&ps=20&_=1538047395924";

	public CurrencyKit_Gp() {
		super();
		Prop prop = PropKit.use("jfinal.properties");
		pagesize=prop.getInt("pagesize");
	}

	@SuppressWarnings("deprecation")
	@Override
	public List<Currency> update() {
		ArrayList<Currency> cs = new ArrayList<Currency>();
		for (int i = 1; i <= pagesize; i++) {
			String url_shanghai = "http://nufm.dfcfw.com/EM_Finance2014NumericApplication/JS.aspx?type=CT&token=4f1862fc3b5e77c150a2b985b12db0fd&sty=FCOIATC&cmd=C.2&st=(ChangePercent)&sr=-1&p="
					+ i + "&ps=2000&_=" + System.currentTimeMillis();
			String url_shenzhen = "http://nufm.dfcfw.com/EM_Finance2014NumericApplication/JS.aspx?type=CT&token=4f1862fc3b5e77c150a2b985b12db0fd&sty=FCOIATC&cmd=C._SZAME&st=(ChangePercent)&sr=-1&p="
					+ i + "&ps=2000&_=" + System.currentTimeMillis();
			//上证100只股票
			queryCurrency(cs,url_shanghai);
			//深证100只股票
			queryCurrency(cs,url_shenzhen);
		}
		return cs;
	}

	private void queryCurrency(ArrayList<Currency> cs,String url){
		String res = HTTPUtils.http(url, null, "get");
		res=res.substring(res.indexOf("["), res.lastIndexOf("]")+1);
		List<String> Strs = JSON.parseArray(res, String.class);
		for (String string : Strs) {
			String[] split = string.split(",");
			Currency currency = new Currency();
			Currency c = Currency.dao.findByCode(split[1]);
			if(c!=null){
				// 判断名称是否有变化
				if(c.getName().equals(split[2])){
					continue;
				}else{
					c.setName(split[2]);
					c.update();
				}
			}
			String code = split[1];
			currency.setCode(code);
			currency.setName(split[2]);
			if(code.startsWith("0")){
				currency.setCurrencyType(Currency.CURRENCY_TYPE_SZ);
			}else if(code.startsWith("6")){
				currency.setCurrencyType(Currency.CURRENCY_TYPE_SH);
			}else{
				currency.setCurrencyType(Currency.CURRENCY_TYPE_KCB);
			}
			cs.add(currency);
			currency.save();
		}
	}
}
