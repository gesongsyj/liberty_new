package com.liberty.system.model;

import com.liberty.system.model.base.BaseShape;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class Shape extends BaseShape<Shape> {
	public static final String SHAPE_TYPE_HIGH = "0";
	public static final String SHAPE_TYPE_LOW = "1";

	public static final Shape dao = new Shape().dao();

	public boolean isHighShape(Kline k1, Kline k2, Kline k3) {
		return k2.getMax() >= k1.getMax() && k2.getMax() >= k3.getMax();
	}

	public boolean isLowShape(Kline k1, Kline k2, Kline k3) {
		return k2.getMin() <= k1.getMin() && k2.getMin() <= k3.getMin();
	}

	public boolean gapToStroke(Stroke stroke, Kline k1, Kline k2, Kline k3) {
		if (isHighShape(k1, k2, k3) && k2.getMin() > k1.getMax() && k2.getMin() > stroke.getMax()) {
			return true;
		}
		if (isLowShape(k1, k2, k3) && k2.getMax() < k1.getMin() && k2.getMax() < stroke.getMin()) {
			return true;
		}
		return false;
	}

	public boolean gapToStroke(Shape shape, Kline k1, Kline k2, Kline k3) {
		if (isHighShape(k1, k2, k3) && k2.getMin() > k1.getMax() && k2.getMin() > shape.getMax()) {
			return true;
		}
		if (isLowShape(k1, k2, k3) && k2.getMax() < k1.getMin() && k2.getMax() < shape.getMin()) {
			return true;
		}
		return false;
	}
	public boolean gapToStroke(Shape shape, List<Kline> klines) {
		for (int i = 0; i < klines.size()-1; i++) {
			if(Shape.SHAPE_TYPE_HIGH.equals(shape.getType())&&klines.get(i+1).getMin()>klines.get(i).getMin()&&klines.get(i+1).getMin()>shape.getMax()){
				return true;
			}
			if(Shape.SHAPE_TYPE_LOW.equals(shape.getType()) &&klines.get(i+1).getMax()<klines.get(i).getMax()&&klines.get(i+1).getMax()<shape.getMin()){
				return true;
			}
		}
		return false;
	}

}
