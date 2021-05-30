package com.liberty.common.utils.stock;

import com.liberty.common.constant.ConstantDefine;
import com.liberty.system.model.Kline;
import com.liberty.system.model.Shape;
import com.liberty.system.model.Stroke;

import java.util.ArrayList;
import java.util.List;

/**
 * K线处理工具类
 */
public class KlineUtil {

    /**
     * 包含关系处理 递归处理
     * @param klines K线集合
     * @param preStroke 前一笔
     * @param index 处理的K线下标
     */
    public static void includeHandler(List<Kline> klines, Stroke preStroke,int index){
        if(index == klines.size()-1){
            return;
        }
        // 前一笔不存在或者方向为null
        int klineDirection = getKlineDirection(klines.get(index), klines.get(index + 1));
        if(null == preStroke || null == preStroke.getDirection()){
            if (klineDirection <ConstantDefine.KLINE_RELATION_UP){
                klines.remove(index);
                index--;
                // 递归调用
                includeHandler(klines,preStroke,index+1);
            }else {
                preStroke = new Stroke();
                if(ConstantDefine.KLINE_RELATION_UP == klineDirection){
                    preStroke.setDirection(ConstantDefine.DIRECTION_DOWN);
                }else {
                    preStroke.setDirection(ConstantDefine.DIRECTION_UP);
                }
            }
        }
        // 第一根K线包含第二根K线
        if(ConstantDefine.KLINE_RELATION_CUR_IN_PRE== klineDirection){
            if(preStroke.getDirection().equals(ConstantDefine.DIRECTION_UP)){
                klines.get(index).setMax(klines.get(index+1).getMax());
            }else{
                klines.get(index).setMin(klines.get(index+1).getMin());
            }
            klines.remove(index+1);
            index--;

        }
        // 第二根K线包含第一根K线
        if(ConstantDefine.KLINE_RELATION_PRE_IN_CUR== klineDirection){
            if(preStroke.getDirection().equals(ConstantDefine.DIRECTION_UP)){
                klines.get(index+1).setMax(klines.get(index).getMax());
            }else{
                klines.get(index+1).setMin(klines.get(index).getMin());
            }
            klines.remove(index);
            index--;
        }
        // 递归调用
        includeHandler(klines,preStroke,index+1);
    }

    /**
     *
     * @param klinePre
     * @param klineCur
     * @return
     */
    public static int getKlineDirection(Kline klinePre, Kline klineCur){
        // 第一根K线包含第二根K线
        if(klinePre.getMax()>klineCur.getMax()&&klinePre.getMin()<klineCur.getMin()){
            return ConstantDefine.KLINE_RELATION_CUR_IN_PRE;
        }else if (klinePre.getMax()<klineCur.getMax()&&klinePre.getMin()>klineCur.getMin()){
            // 第二根K线包含第一根K线
            return ConstantDefine.KLINE_RELATION_PRE_IN_CUR;
        }else if(klinePre.getMax()<klineCur.getMax()&& klinePre.getMin()<klineCur.getMin()){
            // 方向向上
            return ConstantDefine.KLINE_RELATION_UP;
        }else{
            // 方向向下
            return ConstantDefine.KLINE_RELATION_DOWN;
        }
    }

    /**
     * 笔构建器
     * @param klines 已处理包含关系的K线
     * @param preShape 上一个分型
     * @param index K线下标
     * @return 构建的笔集合
     */
    public static List<Stroke> buildStroke(List<Kline> klines, Shape preShape,int index){
        List<Stroke> strokes = new ArrayList<>();
        if (null ==buildShape(klines,index)){
            return strokes;
        }
        if(null == preShape){
            preShape = buildShape(klines,index);
            strokes.addAll(buildStroke(klines,preShape,preShape.getIndex()));
        }else{
            // todo 各种不成立的情况分析,以及打破原笔的情况分析
            Shape shape = buildShape(klines,index);
            if(shape.getType().equals(preShape.getType())){
                // 与前分型同类型

            }else{
                // 与前分型不同类型
            }
            // 判断是否满足结合律
            if(shape.getHasGap()){
                // 笔有缺口
                // 构成一笔
            }else{
                // 笔没有缺口
                if(shape.getIndex()-preShape.getIndex()<4){
                    // 不构成一笔
                    if(true){

                    }
                }else{
                    // 构成一笔
                }
            }
            Stroke stroke = new Stroke();
            stroke.setDirection(shape.getType());
            stroke.setFromGap(shape.getHasGap());
            stroke.setCurrencyId(klines.get(0).getCurrencyId());
            stroke.setStartDate(preShape.getDate());
            stroke.setEndDate(shape.getDate());
            if(ConstantDefine.SHAPE_TYPE_TOP.equals(shape.getType())){
                stroke.setMax(shape.getMax());
                stroke.setMin(preShape.getMin());
            }else{
                stroke.setMax(preShape.getMax());
                stroke.setMin(shape.getMin());
            }
            strokes.add(stroke);
            strokes.addAll(buildStroke(klines,shape,shape.getIndex()));
        }
        return strokes;
    }

    /**
     * 从给的K线集合中构造出一个分型,有了一个就返回
     * @param klines K集合
     * @param index klines的起始下标
     * @return 分型
     */
    public static Shape buildShape(List<Kline> klines,int index){
        Shape shape = new Shape();
        for (int i = index; i < klines.size()-2; i++) {
            if(!shape.getHasGap()&&(klines.get(i).getMin()>klines.get(i+1).getMax()||klines.get(i).getMax()<klines.get(i+1).getMin())){
                shape.setHasGap(true);
            }
            if(klines.get(i+1).getMin()<klines.get(i).getMin()&&klines.get(i+1).getMin()<klines.get(i+2).getMin()){
                shape.setIndex(i+1);
                shape.setMin(klines.get(i+1).getMin());
                shape.setDate(klines.get(i+1).getDate());
                shape.setType(ConstantDefine.SHAPE_TYPE_bottom);
                return shape;
            }
            if(klines.get(i+1).getMax()>klines.get(i).getMax()&&klines.get(i+1).getMax()>klines.get(i+1).getMax()){
                shape.setIndex(i+1);
                shape.setMax(klines.get(i+1).getMax());
                shape.setDate(klines.get(i+1).getDate());
                shape.setType(ConstantDefine.SHAPE_TYPE_TOP);
                return shape;
            }
        }
        return null;
    }
}
