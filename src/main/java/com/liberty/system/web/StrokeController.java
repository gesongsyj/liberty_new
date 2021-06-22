package com.liberty.system.web;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Page;
import com.liberty.common.utils.DateUtil;
import com.liberty.common.utils.ResultMsg;
import com.liberty.common.utils.ResultStatusCode;
import com.liberty.common.web.BaseController;
import com.liberty.system.model.Currency;
import com.liberty.system.model.Stroke;
import com.liberty.system.query.StrokeQueryObject;
import com.liberty.system.strategy.calibrator.Calibrator;
import com.liberty.system.strategy.executor.Executor;
import com.liberty.system.strategy.executor.job.Strategy9Executor;

import java.util.Date;

public class StrokeController extends BaseController {

    public void testSave() {
        for (int i = 0; i < 20; i++) {
            new Stroke().set("id", i + 100).save();
        }
        renderText("添加");
    }

    public void testPage() {
        StrokeQueryObject qo = new StrokeQueryObject();
        // qo.setKeyword("11");
        // qo.setParentId(110L);
        Page<Stroke> paginate = Stroke.dao.paginate(qo);
        System.out.println(paginate.getList());
        renderText("查询");
    }

    /**
     * http://localhost:8080/stroke/calibrate?code=600668
     * http://localhost:8080/stroke/calibrate?code=600668&startDate=2019-08-08
     */
    public void calibrate() {
        // 验证器
        Executor executor = new Strategy9Executor();
        Calibrator calibrator = new Calibrator(executor);
        String code = paras.get("code");
        if(code==null){
            renderText("缺少code参数");
            return;
        }
        String startDateStr = paras.get("startDate");
        Currency currency = Currency.dao.findByCode(code);
        if (startDateStr != null) {
            Date startDate = DateUtil.strDate(startDateStr, "yyyy-MM-dd");
            calibrator.calibrate(currency, startDate);
        } else {
            calibrator.calibrate(currency, null);
        }
        renderText("ok");
    }
}
