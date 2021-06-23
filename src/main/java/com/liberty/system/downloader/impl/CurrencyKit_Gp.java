package com.liberty.system.downloader.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.json.Json;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.liberty.common.utils.HTTPUtils;
import com.liberty.system.downloader.CurrencyKit;
import com.liberty.system.model.Currency;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CurrencyKit_Gp implements CurrencyKit {
    private static Lock lock = new ReentrantLock();

    private int pagesize = 1;// 5页股票排行数据,每页20条,共100条数据
    private final String ex_url_shanghai = "http://nufm.dfcfw.com/EM_Finance2014NumericApplication/JS.aspx?type=CT&token=4f1862fc3b5e77c150a2b985b12db0fd&sty=FCOIATC&cmd=C.2&st=(ChangePercent)&sr=-1&p=1&ps=20&_=1538047395924";
    private final String ex_url_shenzhen = "http://nufm.dfcfw.com/EM_Finance2014NumericApplication/JS.aspx?type=CT&token=4f1862fc3b5e77c150a2b985b12db0fd&sty=FCOIATC&cmd=C._SZAME&st=(ChangePercent)&sr=-1&p=1&ps=20&_=1538047395924";

    public CurrencyKit_Gp() {
        super();
        Prop prop = PropKit.use("jfinal.properties");
        pagesize = prop.getInt("pagesize");
    }

    @SuppressWarnings("deprecation")
    @Override
    public Vector<Currency> update() {
        Vector<Currency> cs = new Vector<Currency>();
        for (int i = 1; i <= pagesize; i++) {
            String url_shanghai = "http://push2.eastmoney.com/api/qt/clist/get?cb=jQuery112408791025184653198_" + System.currentTimeMillis() + "&pn=" + i + "&pz=2000&po=1&np=1&ut=bd1d9ddb04089700cf9c27f6f7426281&fltt=2&invt=2&fid=f3&fs=m:1+t:2,m:1+t:23&fields=f12,f13,f14&_=" + System.currentTimeMillis();
            String url_shenzhen = "http://push2.eastmoney.com/api/qt/clist/get?cb=jQuery112408791025184653198_" + System.currentTimeMillis() + "&pn=" + i + "&pz=2000&po=1&np=1&ut=bd1d9ddb04089700cf9c27f6f7426281&fltt=2&invt=2&fid=f3&fs=m:0+t:6,m:0+t:80&fields=f12,f13,f14&_=" + System.currentTimeMillis();
            //上证100
            queryCurrency(cs, url_shanghai);
            //深证100
            queryCurrency(cs, url_shenzhen);
        }
        return cs;
    }

    private void queryCurrency(Vector<Currency> cs, String url) {
        String res = HTTPUtils.http(url, null, "get");
        res = res.substring(res.indexOf("["), res.lastIndexOf("]") + 1);
        List<JSONObject> jsonObjects = JSON.parseArray(res, JSONObject.class);


        jsonObjects.parallelStream().forEach(e -> {
            try {
                lock.lock();
                String code = e.get("f12").toString();
                String type = e.get("f13").toString();
                String name = e.get("f14").toString();
                Currency c = Currency.dao.findByCode(code);
                double tatalStockCount = queryTotalStockCount(code);
                if (c != null) {
                    // 判断名称是否有变化
                    if (c.getName().equals(name) && null != c.getTotalStockCount() && c.getTotalStockCount() == tatalStockCount) {
//					continue;
                    } else {
                        c.setName(name);
                        c.setTotalStockCount(tatalStockCount);
                        c.update();
                    }
                } else {
                    c = new Currency();
                    c.setCode(code);
                    c.setName(name);
                    if (code.startsWith("6")) {
                        c.setCurrencyType(Currency.CURRENCY_TYPE_SH);
                    } else {
                        c.setCurrencyType(Currency.CURRENCY_TYPE_SZ);
                    }
                    c.setTotalStockCount(tatalStockCount);
                    cs.add(c);
                    c.save();
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            } finally {
                lock.unlock();
            }

        });

//	@SuppressWarnings("deprecation")
//	@Override
//	public Vector<Currency> update() {
//		Vector<Currency> cs = new Vector<Currency>();
//		for (int i = 1; i <= pagesize; i++) {
//			String url_shanghai = "http://nufm.dfcfw.com/EM_Finance2014NumericApplication/JS.aspx?type=CT&token=4f1862fc3b5e77c150a2b985b12db0fd&sty=FCOIATC&cmd=C.2&st=(ChangePercent)&sr=-1&p="
//					+ i + "&ps=2000&_=" + System.currentTimeMillis();
//			String url_shenzhen = "http://nufm.dfcfw.com/EM_Finance2014NumericApplication/JS.aspx?type=CT&token=4f1862fc3b5e77c150a2b985b12db0fd&sty=FCOIATC&cmd=C._SZAME&st=(ChangePercent)&sr=-1&p="
//					+ i + "&ps=2000&_=" + System.currentTimeMillis();
//			//上证100
//			queryCurrency(cs,url_shanghai);
//			//深证100
//			queryCurrency(cs,url_shenzhen);
//		}
//		return cs;
//	}
//
//	private void queryCurrency(Vector<Currency> cs,String url){
//		String res = HTTPUtils.http(url, null, "get");
//		res=res.substring(res.indexOf("["), res.lastIndexOf("]")+1);
//		List<String> Strs = JSON.parseArray(res, String.class);
//
//		Strs.parallelStream().forEach(e->{
//			try{
//				lock.lock();
//				String[] split = e.split(",");
//				Currency c = Currency.dao.findByCode(split[1]);
//				String code = split[1];
//				double tatalStockCount = queryTotalStockCount(code);
//				if(c!=null){
//					// 判断名称是否有变化
//					if(c.getName().equals(split[2]) && null!=c.getTotalStockCount() && c.getTotalStockCount() == tatalStockCount){
////					continue;
//					}else{
//						c.setName(split[2]);
//						c.setTotalStockCount(tatalStockCount);
//						c.update();
//					}
//				}else{
//					c = new Currency();
//					c.setCode(code);
//					c.setName(split[2]);
//					if(code.startsWith("6")){
//						c.setCurrencyType(Currency.CURRENCY_TYPE_SH);
//					}else{
//						c.setCurrencyType(Currency.CURRENCY_TYPE_SZ);
//					}
//					c.setTotalStockCount(tatalStockCount);
//					cs.add(c);
//					c.save();
//				}
//			}catch (Exception e1){
//				e1.printStackTrace();
//			}finally {
//				lock.unlock();
//			}
//
//		});

//		for (String string : Strs) {
//			String[] split = string.split(",");
//			Currency c = Currency.dao.findByCode(split[1]);
//			String code = split[1];
//			double tatalStockCount = queryTotalStockCount(code);
//			if(c!=null){
//
//				// 判断名称是否有变化
//				if(c.getName().equals(split[2]) && null!=c.getTotalStockCount() && c.getTotalStockCount() == tatalStockCount){
//					continue;
//				}else{
//					c.setName(split[2]);
//					c.setTotalStockCount(tatalStockCount);
//					c.update();
//				}
//			}else{
//				c = new Currency();
//				c.setCode(code);
//				c.setName(split[2]);
//				if(code.startsWith("6")){
//					c.setCurrencyType(Currency.CURRENCY_TYPE_SH);
//				}else{
//					c.setCurrencyType(Currency.CURRENCY_TYPE_SZ);
//				}
//				c.setTotalStockCount(tatalStockCount);
//				cs.add(c);
//				c.save();
//			}
//		}
    }

    /**
     * 查询总股本数
     *
     * @param currencyCode
     * @return
     */
    public static double queryTotalStockCount(String currencyCode) {
        String query_url = "http://push2.eastmoney.com/api/qt/stock/get?fields=f85&secid={0}.{1}&cb=jQuery112306531710165462696_{2}&_={3}";
        long timeMillis = System.currentTimeMillis();
        if (currencyCode.startsWith("0")) {
            query_url = MessageFormat.format(query_url, "0", currencyCode, timeMillis, timeMillis);
        } else if (currencyCode.startsWith("6")) {
            query_url = MessageFormat.format(query_url, "1", currencyCode, timeMillis, timeMillis);
        } else {
            query_url = MessageFormat.format(query_url, "0", currencyCode, timeMillis, timeMillis);
        }
        String res = HTTPUtils.http(query_url, null, "get");
        res = res.substring(res.indexOf("(") + 1, res.lastIndexOf(")"));
        JSONObject jsonObject_data = JSON.parseObject(res);
        Object dataObj = jsonObject_data.get("data");
        JSONObject jsonObject_f85 = JSON.parseObject(JSON.toJSONString(dataObj));
        double f85 = jsonObject_f85.getDoubleValue("f85");
        return f85;
    }

}
