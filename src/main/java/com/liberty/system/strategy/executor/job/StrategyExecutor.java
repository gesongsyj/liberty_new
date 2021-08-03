package com.liberty.system.strategy.executor.job;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfplugin.mail.MailKit;
import com.liberty.common.constant.ConstantDefine;
import com.liberty.common.plugins.threadPoolPlugin.ThreadPoolKit;
import com.liberty.common.utils.DateUtil;
import com.liberty.common.utils.MailUtil;
import com.liberty.system.model.*;
import com.liberty.system.strategy.executor.Executor;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class StrategyExecutor {
    // 存储执行日期,多线程跑的时候确保线程安全
    protected ThreadLocal<String> localExecuteDate = new ThreadLocal<>();
    protected Strategy strategy;
    protected boolean isCalibrate = false;
    protected boolean onlyK = false;

    public boolean isCalibrate() {
        return isCalibrate;
    }

    public void setCalibrate(boolean value) {
        this.isCalibrate = value;
    }

    public String getExecuteDate() {
        return localExecuteDate.get() == null ? DateUtil.getDay() : localExecuteDate.get();
    }

    public void setExecuteDate(String executeDate) {
        this.localExecuteDate.set(executeDate);
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    public boolean isOnlyK() {
        return onlyK;
    }

    public void setOnlyK(boolean onlyK) {
        this.onlyK = onlyK;
    }

    public void sendMailToBuy(Vector<Currency> stayCurrency, Executor executor) {
        System.out.println("sendMailToBuy:" + stayCurrency.size());
        if (stayCurrency.size() != 0 && !this.isCalibrate()) {
            MailUtil.sendMailToBuy(stayCurrency, executor);
        }
    }

    public void sendMailTimecost(double time) {
        String tmp = "策略[" + this.getStrategy().getDescribe() + "]此次执行耗时:" + time + "分钟!";
        System.out.println(tmp);
        if (!this.isCalibrate()) {
            MailKit.send("1971119509@qq.com", null, "策略[" + this.getStrategy().getDescribe() +
                            "]执行耗时提醒!",
                    "此次策略执行耗时:" + time + "分钟!");
        }
    }

    public void multiProExe(List<Currency> cs, Vector<Currency> sc) {
        ThreadPoolExecutor executor = ThreadPoolKit.getExecutor();
        int queueSize = executor.getQueue().remainingCapacity();
        for (int i = 0; i < cs.size(); i++) {
            List<Future> futureList = new ArrayList<>();
            for (int j = 0; j < queueSize && i < cs.size(); j++, i++) {
                int index = i;
                Future<?> future = executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        Currency currency = cs.get(index);
                        if (executeSingle(currency)) {
                            System.err.println("满足策略:" + currency.getCode() + ":" + currency.getName());
                            if (notExistsRecord(currency)) {
                                System.err.println("不存在");
                                sc.add(currency);
                            } else {
                                System.err.println("已存在");
                            }
                        }
//					else {
//						Record record = Db.findFirst("select * from currency_strategy where
//						cutLine is null
//						and
//						currencyId=? and strategyId=?",
//								currency.getId(), strategy.getId());
//						if(record!=null) {
//							Db.delete("currency_strategy",record);
//						}
//					}
                    }
                });
                futureList.add(future);
                System.out.println("当前线程池信息: \n" + "存活线程数===" + executor.getActiveCount() + ";" +
                        "\n完成任务数===" + executor.getCompletedTaskCount() + ";\n总任务数===" + executor.getTaskCount());
            }
            for (Future future : futureList) {
                try {
                    future.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            i--;
        }
    }

    public abstract boolean executeSingle(Currency currency);

    /**
     * 删除当天的执行记录
     *
     * @param code
     */
    public void deleteExecuteRecord(String code) {
        String sql;
        if (code == null) {
            sql = "DELETE from currency_strategy WHERE strategyId=" + this.getStrategy().getId() + " and " +
                    "startDate = " +
                    "'" + DateUtil.getDay() + "'";
        } else {
            Currency currency = Currency.dao.findByCode(code);
            sql = "DELETE from currency_strategy WHERE currencyId = " + currency.getId() + " and " +
                    "strategyId" +
                    "=" + this.getStrategy().getId() + " and startDate = '" + DateUtil.getDay() + "'";
        }
        Db.tx(() -> {
            Db.delete(sql);
            return true;
        });
    }

    /**
     * 满足策略,判断记录是否存在,执行不同的操作
     * *
     *
     * @param currency
     * @return
     */
    public boolean notExistsRecord(Currency currency) {
        Record record = Db.findFirst("select * from currency_strategy where currencyId=? and " +
                        "strategyId=? " +
                        "and " +
                        "startDate=?",
                currency.getId(), this.strategy.getId(), this.getExecuteDate());
        if (record == null) {
            record = new Record().set("currencyId", currency.getId()).set("strategyId",
                    this.strategy.getId())
                    .set("startDate", this.getExecuteDate());
            Db.save("currency_strategy", record);
            return true;
        } else {
//            record.set("startDate", DateUtil.getDay());
//            Db.update("currency_strategy", record);
            // 如果已经存在该条记录,只是做更新时间的处理
            return false;
        }
    }

    /**
     * 判断三笔是否重叠
     *
     * @param s1
     * @param s2
     * @param s3
     * @return 0:重叠;1:不重叠:方向向上;2:不重叠:方向向下
     */
    protected int overlap(Stroke s1, Stroke s2, Stroke s3) {
        if ("0".equals(s1.getDirection()) && "0".equals(s3.getDirection())) {
            if (s1.getMin() > s3.getMax()) {
                return 2;
            }
        }
        if ("1".equals(s1.getDirection()) && "1".equals(s3.getDirection())) {
            if (s1.getMax() < s3.getMin()) {
                return 1;
            }
        }
        return 0;
    }

    /**
     * 构建线段中的中枢
     *
     * @param strokes
     * @return
     */
    public Centre buildLineCentre(List<Stroke> strokes) {
        double max, min, centreMax, centreMin;
        Centre centre = null;
        for (int i = 1; i < strokes.size() - 2; i++) {
            if (overlap(strokes.get(i), strokes.get(i + 1), strokes.get(i + 2)) == 0) {
                if ("0".equals(strokes.get(i).getDirection())) {
                    if (strokes.get(i + 1).getMin() < strokes.get(i).getMin()) {
                        min = strokes.get(i + 1).getMin();
                        centreMin = strokes.get(i).getMin();
                    } else {
                        min = strokes.get(i).getMin();
                        centreMin = strokes.get(i + 1).getMin();
                    }
                    if (strokes.get(i + 2).getMax() > strokes.get(i).getMax()) {
                        max = strokes.get(i + 2).getMax();
                        centreMax = strokes.get(i).getMax();
                    } else {
                        max = strokes.get(i).getMax();
                        centreMax = strokes.get(i + 2).getMax();
                    }
                } else {
                    if (strokes.get(i + 1).getMax() > strokes.get(i).getMax()) {
                        max = strokes.get(i + 1).getMax();
                        centreMax = strokes.get(i).getMax();
                    } else {
                        max = strokes.get(i).getMax();
                        centreMax = strokes.get(i + 1).getMax();
                    }
                    if (strokes.get(i + 2).getMin() < strokes.get(i).getMin()) {
                        min = strokes.get(i + 2).getMin();
                        centreMin = strokes.get(i).getMin();
                    } else {
                        min = strokes.get(i).getMin();
                        centreMin = strokes.get(i + 2).getMin();
                    }
                }
                centre = new Centre();
                centre.setMax(max).setMin(min).setCentreMax(centreMax).setCentreMin(centreMin);
                break;
            } else {
                continue;
            }
        }
        return centre;
    }

    /**
     * 构建中枢
     *
     * @param strokes
     * @return
     */
    public void buildCentre(List<Stroke> strokes, Centre centre, Stroke beforeCentre,
                            Stroke afterCentre) {
        double max, min, centreMax, centreMin;
        Stroke lastStroke = strokes.get(strokes.size() - 1);
        // 最后一笔必须向下
        if ("0".equals(lastStroke.getDirection())) {
            return;
        }
        afterCentre.setStartDate(lastStroke.getStartDate()).setMax(lastStroke.getMax())
                .setEndDate(lastStroke.getEndDate()).setMin(lastStroke.getMin());
        outter:
        for (int i = strokes.size() - 1; i >= 4; i--) {
            if (overlap(strokes.get(i - 3), strokes.get(i - 2), strokes.get(i - 1)) != 0) {
                afterCentre.setStartDate(strokes.get(i - 2).getStartDate()).setMax(strokes.get(i - 2).getMax());
                i--;
                continue;
            } else {
//					"0".equals(strokes.get(i).getDirection())
                if (strokes.get(i - 2).getMin() < strokes.get(i - 3).getMin()) {
                    min = strokes.get(i - 2).getMin();
                    centreMin = strokes.get(i - 3).getMin();
                } else {
                    min = strokes.get(i - 3).getMin();
                    centreMin = strokes.get(i - 2).getMin();
                }
                if (strokes.get(i - 1).getMax() > strokes.get(i - 3).getMax()) {
                    max = strokes.get(i - 1).getMax();
                    centreMax = strokes.get(i - 3).getMax();
                } else {
                    max = strokes.get(i - 3).getMax();
                    centreMax = strokes.get(i - 1).getMax();
                }
                centre.setMax(max).setMin(min).setCentreMax(centreMax).setCentreMin(centreMin)
                        .setStartDate(strokes.get(i - 3).getStartDate()).setEndDate(strokes.get(i - 1).getEndDate());

            }
            if (strokes.get(i - 4).getMax() > centre.getMax()) {
                Stroke stroke = strokes.get(i - 4);
                beforeCentre.setStartDate(stroke.getStartDate()).setMax(stroke.getMax()).setEndDate(stroke.getEndDate())
                        .setMin(stroke.getMin());
            }

            if (i >= 5) {
                for (int j = i - 5; j >= 0; j--) {// i-4作为连接线存在,不参与重构中枢,其实没有任何影响,
                    // 因为i+3的最大值和最小值都可以从前后两笔中取得
//					"0".equals(strokes.get(j).getDirection())必然
                    if (strokes.get(j).getMax() <= centre.getCentreMax()) {
                        if (strokes.get(j).getMin() >= centre.getCentreMin()) {
                            // 整个笔都在中枢区间内
                        } else if (strokes.get(j).getMin() >= centre.getMin()) {
                            centre.setCentreMin(strokes.get(j).getMin());
                            centre.setStartDate(strokes.get(j).getStartDate());
                        } else {
                            centre.setCentreMin(centre.getMin()).setMin(strokes.get(j).getMin());
                            centre.setStartDate(strokes.get(j).getStartDate());
                        }
                    } else if (strokes.get(j).getMax() <= centre.getMax()) {
                        if (strokes.get(j).getMin() > centre.getCentreMax()) {
                            centre.setCentreMax(strokes.get(j).getMax());
                            centre.setStartDate(strokes.get(j).getStartDate());
                        } else if (strokes.get(j).getMin() >= centre.getCentreMin()) {
                            centre.setCentreMax(strokes.get(j).getMax());
                            centre.setStartDate(strokes.get(j).getStartDate());
                        } else if (strokes.get(j).getMin() >= centre.getMin()) {
                            centre.setCentreMax(strokes.get(j).getMax());
                            centre.setCentreMin(strokes.get(j).getMin());
                            centre.setStartDate(strokes.get(j).getStartDate());
                        } else {
                            centre.setCentreMax(strokes.get(j).getMax());
                            centre.setCentreMin(centre.getMin()).setMin(strokes.get(j).getMin());
                            centre.setStartDate(strokes.get(j).getStartDate());
                        }
                    } else {
                        if (strokes.get(j).getMin() > centre.getMax()) {
                            i = j + 1;
                            Stroke stroke = strokes.get(j + 1);
                            beforeCentre.setStartDate(stroke.getStartDate()).setMax(stroke.getMax())
                                    .setEndDate(stroke.getEndDate()).setMin(stroke.getMin());
                            break;
                        } else if (strokes.get(j).getMin() > centre.getCentreMax()) {
                            centre.setCentreMax(centre.getMax()).setMax(strokes.get(j).getMax());
                            centre.setStartDate(strokes.get(j).getStartDate());
                        } else if (strokes.get(j).getMin() >= centre.getCentreMin()) {
                            centre.setCentreMax(centre.getMax()).setMax(strokes.get(j).getMax());
                            centre.setStartDate(strokes.get(j).getStartDate());
                        } else if (strokes.get(j).getMin() >= centre.getMin()) {
                            centre.setCentreMax(centre.getMax()).setMax(strokes.get(j).getMax());
                            centre.setCentreMin(strokes.get(j).getMin());
                            centre.setStartDate(strokes.get(j).getStartDate());
                        } else {
                            centre.setCentreMax(centre.getMax()).setMax(strokes.get(j).getMax());
                            centre.setCentreMin(centre.getMin()).setMin(strokes.get(j).getMin());
                            centre.setStartDate(strokes.get(j).getStartDate());
                        }
                    }
                    j--;
                }
            }

            for (int k = i; k >= 3; k--) {
                if (overlap(strokes.get(k - 3), strokes.get(k - 2), strokes.get(k - 1)) != 0) {
                    beforeCentre.setStartDate(strokes.get(k - 2).getStartDate()).setMax(strokes.get(k - 2).getMax());
                    k--;
                    continue;
                } else {
                    break outter;
                }
            }
            beforeCentre.setStartDate(strokes.get(0).getStartDate()).setMax(strokes.get(0).getMax());
        }
    }

    /**
     * 找到条件堪堪成立的那天
     *
     * @param klinesAfterLastStroke
     * @param lastStroke
     * @return
     */
    public boolean checkPoint(List<Kline> klinesAfterLastStroke, Stroke lastStroke) {
        if (klinesAfterLastStroke.size() < 2) {
            return false;
        }
        double max = klinesAfterLastStroke.get(0).getMax();
        double min = klinesAfterLastStroke.get(0).getMin();
        if (lastStroke.getDirection().equals(ConstantDefine.DIRECTION_UP)) {
            return false;
        }
        for (int i = 1; i < klinesAfterLastStroke.size(); i++) {
            // 判断包含
            if (klinesAfterLastStroke.get(i).getMax() <= max && klinesAfterLastStroke.get(i).getMin() >= min) {
                // 向下,包含取下下
                max = klinesAfterLastStroke.get(i).getMax();
                continue;
            }
            if (klinesAfterLastStroke.get(i).getMax() > max) {
                if (i == klinesAfterLastStroke.size() - 1) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * 判断当前构成底分型
     *
     * @param klines 时间正序排列的集合
     * @return 判断结果
     */
    public boolean checkLowShape(List<Kline> klines) {
        int currentIndex = klines.size() - 1;
        if (currentIndex < 2) {
            return false;
        }
        if (klines.get(currentIndex).getMax() <= klines.get(currentIndex - 1).getMax()) {
            return false;
        }
        if (klines.get(currentIndex).getMax() >= klines.get(currentIndex - 1).getMax() &&
                klines.get(currentIndex).getMin() <= klines.get(currentIndex - 1).getMin()) {
            List<Kline> subKlines = new ArrayList<>(klines.subList(0, currentIndex));
            subKlines.get(subKlines.size() - 1).setMax(klines.get(currentIndex).getMax());
            return checkLowShape(subKlines);
        }
        if (klines.get(currentIndex).getMax() > klines.get(currentIndex - 1).getMax() &&
                klines.get(currentIndex).getMin() > klines.get(currentIndex - 1).getMin()) {
            if (klines.get(currentIndex - 1).getMax() > klines.get(currentIndex - 2).getMax() &&
                    klines.get(currentIndex - 1).getMin() > klines.get(currentIndex - 2).getMin()) {
                return false;
            }
            if (klines.get(currentIndex - 1).getMax() > klines.get(currentIndex - 2).getMax() &&
                    klines.get(currentIndex - 1).getMin() < klines.get(currentIndex - 2).getMin()) {
                List<Kline> subKlines = new ArrayList<>(klines);
                // 这里设置min,上面设置max,都是为了更好的得到true
                subKlines.get(subKlines.size() - 3).setMin(subKlines.get(subKlines.size() - 2).getMin());
                subKlines.remove(subKlines.size() - 2);
                return checkLowShape(subKlines);
            }
            return true;
        }
        return false;
    }
}
