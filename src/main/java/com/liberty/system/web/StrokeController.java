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

    public void calibrate() {
        // 验证器
        Executor executor = new Strategy9Executor();
        Calibrator calibrator = new Calibrator(executor);
        Currency currency = Currency.dao.findByCode("600668");
        Date startDate = DateUtil.strDate("2019-08-05", "yyyy-MM-dd");
        calibrator.calibrate(currency,startDate);
//        calibrator.calibrate(currency,null);
        renderText("ok");
    }
}
