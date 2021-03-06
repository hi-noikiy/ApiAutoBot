package com.alpha.apiautobot.platform.huobipro;

import android.util.Log;

import com.alpha.apiautobot.base.rest.huobipro.HuobiApiService;
import com.alpha.apiautobot.domain.response.huobipro.MarketDetail;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class CoinIncreaseRunnable implements Runnable {
    final List<MarketDetail> marketDetailList;
    final String symbol;
    final HuobiApiService apiService;

    private String requestSymbol = null;

    public CoinIncreaseRunnable(String symbol, HuobiApiService service, List<MarketDetail> details) {
        this.marketDetailList = details;
        this.symbol = symbol;
        this.apiService = service;
        String[] symbols = symbol.split("/");
        requestSymbol = symbols[0] + symbols[1];
    }

    @Override
    public void run() {
        while (true) {
            Call<MarketDetail> call = apiService.getMarketDetail(requestSymbol);
            try {
                Response<MarketDetail> response = call.execute();
                if (response.isSuccessful()) {
                    MarketDetail marketDetail = response.body();
                    marketDetail.symbol = symbol;
                    double lastDealPrice = marketDetail.tick.close;
                    long timestamp = marketDetail.ts;
//                                MarketDetail.LastPrice lastPrice = new MarketDetail.LastPrice(lastDealPrice, timestamp);
                    if (this.marketDetailList.size() == 0) {
//                                    this.marketDetail = marketDetail;
                        //第一个时间间隔，相当于开盘价
                        marketDetail.price = lastDealPrice;
                        marketDetail.timeStamp = timestamp;
                        marketDetail.timeGap = 5;
                        marketDetailList.add(marketDetail);
//                                    timeFlag = timestamp;
//                                    rateMap.put(0, lastDealPrice);
                        //将时间索引移动到第五分钟
//                                    timeGap += 5;
//                                    continue;
                    } else {
                        //当前时间与最近时间比较
                        int pos = marketDetailList.size() - 1;
                        int second = (int) ((timestamp - marketDetailList.get(pos).ts) / 1000);
                        if (second > 60 * 5) {
                            //达到时间间隔，更新索引
//                                        timeFlag = timestamp;
                            //最多统计个数=统计时间/统计间隔+1
//                                        if(rateMap.size() >= (60 / 5 + 1)) {
//                                            //统计已满
//                                            //删除第一个元素
//                                            rateMap.remove(timeGap - 60);
//                                        }
//                                        timeGap += 5;
                            if (marketDetailList.size() == 60 / 5) {
                                //统计已满，删除第一个统计值
                                marketDetailList.remove(0);
                            }
                            //时间间隔累加
                            marketDetail.timeGap = marketDetailList.get(pos).timeGap + 5;
                            marketDetail.price = lastDealPrice;
                            marketDetail.timeStamp = timestamp;
                            marketDetailList.add(marketDetail);
                        } else {
                            //时间间隔内
                            //更新时间戳，价格
                            marketDetailList.get(pos).timeStamp = timestamp;
                            marketDetailList.get(pos).price = lastDealPrice;
                        }
                    }
                    Log.e("CoinIncreaseRunnable", symbol + "已统计" + marketDetailList.size());
                    //从后向前遍历
//                    int i = marketDetailList.size() - 1;
//                    for (int j = 0; j <= i; j++) {
//                        Double price2 = (Double) marketDetailList.get(i).price;
//                        Double price1 = null;
//                        if (j == 0) {
//                            price1 = marketDetailList.get(0).tick.close;
//                        } else {
//                            price1 = marketDetailList.get(j - 1).price;
//                        }
//                        Integer timeGap1 = marketDetailList.get(i).timeGap;
//                        Integer timeGap2 = null;
//                        if (j == 0) {
//                            timeGap2 = marketDetailList.get(0).timeGap;
//                            timeGap2 -= 5;
//                        } else {
//                            timeGap2 = marketDetailList.get(j - 1).timeGap;
//                        }
//                        double diff = Math.abs(price2 - price1) / price2;
//                        String out = "%s在近%s时间内%s";
//                        NumberFormat format = NumberFormat.getInstance();
//                        format.setMaximumFractionDigits(2);
//                        String printLog = String.format(out, symbol, ((timeGap1 - timeGap2 == 0 ? "5" : (timeGap1 - timeGap2 + ""))) + "分钟",
//                                (price2 > price1 ? "涨幅+" : "跌幅-") + format.format(diff * 100) + "%");
//                        if (diff >= 0.01) {
//                            //涨跌幅大于1%
//                            Log.e("HuobiPro", printLog);
//                        }
//                    }
//                                rateMap.put(timeGap, lastDealPrice);
//                                Iterator entries = rateMap.entrySet().iterator();
//                                while (entries.hasNext()) {
//                                    Map.Entry entry = (Map.Entry) entries.next();
//                                    //最大时间值等于已存时间间隔总数-1乘以时间间隔
//                                    Integer timeIndex = (rateMap.size() - 1) * 5;
//                                    Integer key = (Integer)entry.getKey();
//                                    if(timeIndex == key) {
//                                        break;
//                                    }
//                                    Double price1 = (Double)entry.getValue();
//                                    Double price2 = rateMap.get(timeIndex);
//                                    double diff = Math.abs(price2 - price1) / price2;
//                                    String out = "%s在近%s时间内%s";
//                                    NumberFormat format = NumberFormat.getInstance();
//                                    format.setMaximumFractionDigits(2);
//                                    String printLog = String.format(out, symbol, timeIndex - key + "分钟",
//                                            (price2 > price1 ? "涨幅+" : "跌幅-") + format.format(diff * 100) + "%");
//                                    if(diff >= 0.01) {
//                                        //涨跌幅大于5%
//                                        Log.e("HuobiPro", printLog);
//                                    }
//                                }
//                                this.marketDetail.prices.add(lastPrice);
//                                if(this.marketDetail.prices.size() > 60) {
//                                    //统计间隔5分钟
//                                    //丢弃头部数据
//                                    this.marketDetail.prices.remove(0);
//                                }
                    //检查涨跌幅
//                                int size = this.marketDetail.prices.size();
//                                if(size <= 1) {
//                                    //一条数据不做比对
//                                    continue;
//                                }

//                                for (int i=0; i<size; i++) {
//                                    double price = this.marketDetail.prices.get(i).price;
//                                    long old_timestamp = this.marketDetail.prices.get(i).timestap;
//                                    double diff = Math.abs(lastDealPrice - price) / lastDealPrice;
//                                    String out = "%s在近%s时间内%s";
//                                    String printLog = String.format(out, symbol, ((timestamp - old_timestamp)/60000) + "分钟",
//                                            (lastDealPrice > price ? "涨幅+" : "跌幅-") + String.valueOf(diff * 100) + "%");
//                                    if(diff >= 0.01) {
//                                        //涨跌幅大于5%
//                                        Log.e("HuobiPro", printLog);
//                                    }
//                                }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
