package com.alpha.apiautobot.platform.huobipro;

import android.util.Log;

import com.alpha.apiautobot.base.rest.huobipro.HuobiApiService;
import com.alpha.apiautobot.base.rest.huobipro.HuobiProInterceptor;
import com.alpha.apiautobot.domain.dao.kucoin.Market;
import com.alpha.apiautobot.domain.request.huobipro.PlaceOrders;
import com.alpha.apiautobot.domain.response.huobipro.AccountBalance;
import com.alpha.apiautobot.domain.response.huobipro.HRAccounts;
import com.alpha.apiautobot.domain.response.huobipro.HRCoins;
import com.alpha.apiautobot.domain.response.huobipro.HRSymbols;
import com.alpha.apiautobot.domain.response.huobipro.MarketDetail;
import com.alpha.apiautobot.domain.response.huobipro.PlaceOrdersResponse;
import com.alpha.apiautobot.platform.AbstractPlatform;
import com.alpha.apiautobot.utils.ApiSignature;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * 火币REST API
 * https://api.huobi.pro/market  行情API
 * https://api.huobi.pro/v1      交易API
 */
public class HuobiPro extends AbstractPlatform {
    public static final String API_HOST           = "api.huobi.pro";

    public static final String ACCESS_KEY = "f3a7ca68-7f1a9603-905b354c-c1ff2";
    public static final String SECRET_KEY = "xxxxx";

    public HuobiApiService apiService;
    public OkHttpClient httpClient;
    private ExecutorService mExecutor = Executors.newFixedThreadPool(200);

    //交易对列表
    private Map<String, List<HRSymbols.Data>> symbolsMap = new HashMap<>();
    private List<List<MarketDetail>> coinDetails = new ArrayList<>();

    public OkHttpClient genericClient(Interceptor interceptor) {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(interceptor)
                .build();
        this.httpClient = httpClient;
        return httpClient;
    }

    public HuobiPro() {
        initRestful();
    }

    public static String getBaseUrl() {
        return "https://" + API_HOST;
    }

    @Override
    public void initRestful() {
        apiService = new Retrofit.Builder()
                .baseUrl(getBaseUrl())
                .client(genericClient(new HuobiProInterceptor()))
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(HuobiApiService.class);
    }

    @Override
    public void connection() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Call<HRSymbols> call = apiService.getCommonSymbols();
                try {
                    Response<HRSymbols> response = call.execute();
                    if(response.isSuccessful()) {
                        HRSymbols hrSymbols = response.body();
                        for (HRSymbols.Data symbol : hrSymbols.data) {
                            String quoteCurrency = symbol.quoteCurrency;
                            if(!symbolsMap.containsKey(quoteCurrency)) {
                                symbolsMap.put(quoteCurrency, new ArrayList<>());
                            }
                            symbolsMap.get(quoteCurrency).add(symbol);
                        }
                        getMarketList();
                    }else {

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void disConnection() {

    }

    @Override
    public void getMarketList() {
        //获取指定交易对行情
        //获取BTC最新交易行情
        List<HRSymbols.Data> list = symbolsMap.get("btc");
        for (HRSymbols.Data data : list) {
//            final String symbol = data.baseCurrency + data.quoteCurrency;
//            if(data.baseCurrency.equals("ht")) {
                final List<MarketDetail> details = new ArrayList<>();
                coinDetails.add(details);
                //请求市场详情
                mExecutor.execute(new CoinIncreaseRunnable(data.baseCurrency + "/" + data.quoteCurrency, apiService, details));
//                break;
//            }
        }
    }

    public List<List<MarketDetail>> getCoinDetails() {
        return this.coinDetails;
    }

    @Override
    public void getTick() {
    }

    @Override
    public void getAccountInfo() {
    }

    @Override
    public void buyCoin() {

    }

    @Override
    public void sellCoin() {

    }

    @Override
    public void deposite() {

    }

    @Override
    public void withdrawal() {

    }

    /**
     * 查询pro站支持的所有交易对及精度
     * @param callback
     */
    public void getSymbols(Callback<HRSymbols> callback) {
        get("getCommonSymbols",callback);
    }

    /**
     * 获取火币所有账户信息
     */
    public void getAccount(Callback<HRAccounts> callback) {
        get("getAccount", callback);
    }

    /**
     * 获取指定账户的余额
     * @param accountId
     */
    public void getBalance(int accountId, Callback<AccountBalance> callback) {
        get("getBalanceByAccountId", new Class[]{int.class},
                new Object[]{accountId},
                callback);
    }

    /**
     * 获取币种
     * @param callback
     */
    public void getCoins(Callback<HRCoins> callback) {
        get("getCurrencys", callback);
    }

    /**
     * 获取交易详情
     */
    public void getTradeDetails(Callback<MarketDetail> callback) {

    }

    /**
     * 下单
     * @param placeOrders
     * @param callback
     */
    public void postOrdersPlace(PlaceOrders placeOrders, Callback<PlaceOrdersResponse> callback) {
        String json = new Gson().toJson(placeOrders);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);
        apiService.postOrdersPlace(requestBody)
                .enqueue(callback);
    }


    /**
     * 使用通用的反射实现接口调用
     * @param method
     * @param parameterTypes
     * @param objects
     * @param callback
     * @param <T>
     */
    public <T> void get(String method, Class[] parameterTypes, Object[] objects, Callback<T> callback) {
        // 使用反射
        Method m = null;
        try {
            m = apiService.getClass().getDeclaredMethod(method, parameterTypes);
            Call<T> call = (Call<T>) m.invoke(apiService, objects);
            call.enqueue(callback);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public <T> void get(String method, Callback<T> callback) {
        get(method, null, null, callback);
    }

    public <T> void get(String method, Class[] parameterTypes,Callback<T> callback) {
        get(method, parameterTypes, null, callback);
    }

    public <T> void get(String method, Object[] objects, Callback<T> callback) {
        get(method, null, objects, callback);
    }
}
