package com.liberty.common.utils;

import java.util.List;

import com.liberty.system.model.Kline;

public class MACD {
	private int sp, lp, dp;

	public MACD() {
		this(12, 26, 9);
	}

	public MACD(int sp, int lp, int dp) {
		this.sp = sp;
		this.lp = lp;
		this.dp = dp;
	}

	public List<Kline> calMacd(List<Kline> klines, Kline lastKline) {
		if (lastKline == null) {
			for (int i = 0; i < klines.size(); i++) {
				Kline kline_current = klines.get(i);
				if (i == 0) {
					kline_current.setEmaS(kline_current.getClose());
					kline_current.setEmaL(kline_current.getClose());
					kline_current.setDiff(0.0);
					kline_current.setDea(0.0);
					kline_current.setBar(0.0);
					kline_current.setAoi(kline_current.getOpen()==0.0?0.0:(kline_current.getClose()-kline_current.getOpen())/kline_current.getOpen());
				} else {
					Kline kline_pre = klines.get(i - 1);
					kline_current.setEmaS(kline_pre.getEmaS() * (sp - 1) / (sp + 1)
							+ kline_current.getClose() * 2 / (sp + 1));
					kline_current.setEmaL(kline_pre.getEmaL() * (lp - 1) / (lp + 1)
							+ kline_current.getClose() * 2 / (lp + 1));
					kline_current.setDiff(kline_current.getEmaS() - kline_current.getEmaL());
					kline_current.setDea(
							kline_pre.getDea() * (dp - 1) / (dp + 1) + kline_current.getDiff() * 2 / (dp + 1));
					kline_current.setBar(2 * (kline_current.getDiff() - kline_current.getDea()));
					kline_current.setAoi(kline_pre.getClose()==0.0?0.0:(kline_current.getClose()-kline_pre.getClose())/kline_pre.getClose());

				}
			}
		} else {
			for (int i = 0; i < klines.size(); i++) {
				Kline kline_current = klines.get(i);
				if (i == 0) {
					kline_current.setEmaS(
							lastKline.getEmaS() * (sp - 1) / (sp + 1) + kline_current.getClose() * 2 / (sp + 1));
					kline_current.setEmaL(
							lastKline.getEmaL() * (lp - 1) / (lp + 1) + kline_current.getClose() * 2 / (lp + 1));
					kline_current.setDiff(kline_current.getEmaS() - kline_current.getEmaL());
					kline_current
							.setDea(lastKline.getDea() * (dp - 1) / (dp + 1) + kline_current.getDiff() * 2 / (dp + 1));
					kline_current.setBar(2 * (kline_current.getDiff() - kline_current.getDea()));
					kline_current.setAoi(lastKline.getClose()==0.0?0.0:(kline_current.getClose()-lastKline.getClose())/lastKline.getClose());
				} else {
					Kline kline_pre = klines.get(i - 1);
					kline_current.setEmaS(kline_pre.getEmaS() * (sp - 1) / (sp + 1)
							+ kline_current.getClose() * 2 / (sp + 1));
					kline_current.setEmaL(kline_pre.getEmaL() * (lp - 1) / (lp + 1)
							+ kline_current.getClose() * 2 / (lp + 1));
					kline_current.setDiff(kline_current.getEmaS() - kline_current.getEmaL());
					kline_current.setDea(
							kline_pre.getDea() * (dp - 1) / (dp + 1) + kline_current.getDiff() * 2 / (dp + 1));
					kline_current.setBar(2 * (kline_current.getDiff() - kline_current.getDea()));
					kline_current.setAoi(kline_pre.getClose()==0.0?0.0:(kline_current.getClose()-kline_pre.getClose())/kline_pre.getClose());
				}
			}
		}
		return klines;
	}
}
