package com.liberty.system.dingtalk;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

/**
 * 每个机器人每分钟最多发送20条。
 */
public class DingUtil {
    /**
     * @param content 钉钉机器人消息内容
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     * @throws InvalidKeyException
     */
    public static void sendMsg(String content) throws NoSuchAlgorithmException,
            UnsupportedEncodingException, InvalidKeyException {

        //群机器人复制到的秘钥secret
        String secret = "SEC8c527d64ab15f3bf35679fd67dc46a57787992360b6e1e728f232f7752986263";
        //获取系统时间戳
        Long timestamp = System.currentTimeMillis();
        //拼接
        String stringToSign = timestamp + "\n" + secret;
        //使用HmacSHA256算法计算签名
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
        //进行Base64 encode 得到最后的sign，可以拼接进url里
        String sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)), "UTF-8");
        //钉钉机器人地址（配置机器人的webhook）
        String dingUrl = "https://oapi.dingtalk" +
                ".com/robot/send?access_token" +
                "=e86c248a95bf58f84bff653c15d9208aa3b869ef7baac60a58424d48c3ed9b45&timestamp=" + timestamp + "&sign=" + sign;

        try {

            //是否通知所有人
            boolean isAtAll = true;
            //通知具体人的手机号码列表
            List<String> mobileList = Lists.newArrayList();
            //组装请求内容
            String reqStr = buildReqStr(content, isAtAll, mobileList);
            //推送消息（http请求）
            String result = HttpUtil.post(dingUrl, reqStr);
            // String result = HttpClientUtil.sendPostDataByJson(sign, reqStr,"utf8");
            System.out.println("result == " + result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 组装请求报文
     *
     * @param content
     * @return
     */
    private static String buildReqStr(String content, boolean isAtAll, List<String> mobileList) {
        //消息内容
        Map<String, String> contentMap = Maps.newHashMap();
        contentMap.put("content", content);
        //通知人
        Map<String, Object> atMap = Maps.newHashMap();
        //1.是否通知所有人
        atMap.put("isAtAll", isAtAll);
        //2.通知具体人的手机号码列表
        atMap.put("atMobiles", mobileList);

        Map<String, Object> reqMap = Maps.newHashMap();
        reqMap.put("msgtype", "text");
        reqMap.put("text", contentMap);
        reqMap.put("at", atMap);

        return JSON.toJSONString(reqMap);
    }

    public static void main(String[] args) throws UnsupportedEncodingException,
            NoSuchAlgorithmException, InvalidKeyException {
        sendMsg("syj帅炸了!");
    }
}
