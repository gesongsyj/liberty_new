package com.liberty.system.strategy.executor.job;

import com.alibaba.fastjson.JSONObject;
import com.liberty.common.constant.ConstantDefine;
import com.liberty.system.blackHouse.RemoveStrategyBh;
import com.liberty.system.model.Currency;
import com.liberty.system.model.Kline;
import com.liberty.system.model.Strategy;
import com.liberty.system.model.Stroke;
import com.liberty.system.strategy.executor.Executor;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * 蓄风雷之势
 */
public class Strategy11Executor extends StrategyExecutor implements Executor {

    public Strategy11Executor() {
        this.strategy = Strategy.dao.findById(11);
    }

    @Override
    public Vector<Currency> execute(String code) {
        long start = System.currentTimeMillis();
        Vector<Currency> stayCurrency = new Vector<>();
        if (code == null) {
            List<Currency> allCurrency = Currency.dao.listAll();
            for (Currency currency : allCurrency) {
                if (RemoveStrategyBh.inBlackHouse(currency)) {// 在小黑屋里面,跳过
                    allCurrency.remove(currency);
                }
            }
            multiProExe(allCurrency, stayCurrency);
        } else {
            Currency currency = Currency.dao.findByCode(code);
            if (!RemoveStrategyBh.inBlackHouse(code)) {// 不在小黑屋里且满足策略
                if (executeSingle(currency)) {
                    if (notExistsRecord(currency)) {
                        stayCurrency.add(currency);
                    }
                }
            }
        }
        sendMailToBuy(stayCurrency, this);
        System.out.println("策略[" + this.getStrategy().getDescribe() + "]执行完毕!");
        long end = System.currentTimeMillis();
        double time = (end - start) * 1.0 / 1000 / 60;
        sendMailTimecost(time);
        return stayCurrency;
    }

    @Override
    public boolean executeSingle(Currency currency) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("currency_id", currency.getId());
        jsonObject.put("start_date", "2021-04-16");
        jsonObject.put("end_date", "2021-07-02");
        Socket socket = null;
        try {
            InetAddress addr = InetAddress.getLocalHost();
            String host = addr.getHostName();
            //String ip=addr.getHostAddress().toString(); //获取本机ip
            //log.info("调用远程接口:host=>"+ip+",port=>"+12345);

            // 初始化套接字，设置访问服务的主机和进程端口号，HOST是访问python进程的主机名称，可以是IP地址或者域名，PORT是python进程绑定的端口号
            socket = new Socket(host, 12345);

            // 获取输出流对象
            OutputStream os = socket.getOutputStream();
            PrintStream out = new PrintStream(os);
            // 发送内容
            out.print(jsonObject.toJSONString());
            // 告诉服务进程，内容发送完毕，可以开始处理
            out.print("over");

            // 获取服务进程的输入流
            InputStream is = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));
            String tmp = null;
            StringBuilder sb = new StringBuilder();
            // 读取内容
            while ((tmp = br.readLine()) != null) {
                sb.append(tmp).append('\n');
            }
            System.out.print(sb);
            if (Boolean.parseBoolean(sb.toString().trim())) {
                System.out.println("满足条件!");
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
            }
            System.out.print("远程接口调用结束.");
        }
        return false;
    }

}
