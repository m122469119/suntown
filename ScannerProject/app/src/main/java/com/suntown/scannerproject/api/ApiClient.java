package com.suntown.scannerproject.api;



import com.suntown.scannerproject.base.BaseApplication;
import com.suntown.scannerproject.netUtils.NetworkUtils;

import java.io.File;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;


/**
 * retrofit
 */
public class ApiClient {
    private static final String BASE_URL = ApiConstant.formatBASE_HOST(ApiConstant.BASE_URL);

    private static OkHttpClient.Builder mClientBuilder;
    public ApiService mApiService;

    /**
     * 通过设置拦截器，给retrofit增加缓存，日志，请求头拦截等功能
     */
    static {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        File httpCacheDir = new File(BaseApplication.getAppContext().getCacheDir(),"cache");

        Cache cache = new Cache(httpCacheDir, 100 * 1024 * 1024);

        Interceptor CACHE_CONTROL_INTERCEPTOR = chain -> {
            Request request = chain.request();
            if (!NetworkUtils.isAvailable(BaseApplication.getAppContext())) {
                request = request.newBuilder()
                        .cacheControl(CacheControl.FORCE_CACHE)
                        .build();
            }
            Response response = chain.proceed(request);
            if (NetworkUtils.isAvailable(BaseApplication.getAppContext())) {
                String cacheControl = request.cacheControl().toString();
                response.newBuilder()
                        .header("Cache-Control", cacheControl)
                        .removeHeader("Pragma") // 清除头信息，因为服务器如果不支持，会返回一些干扰信息，不清除下面无法生效
                        .build();
            } else {
                int maxStale = 60 * 60 * 24 * 14; // 无网络时，设置超时为2周
                response.newBuilder()
                        .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                        .removeHeader("Pragma")
                        .build();
            }
            return response;
        };
/**
 * 请求头拦截器
 */
//        Interceptor headerInterceptor = new Interceptor() {
//            @Override
//            public Response intercept(Chain chain) throws IOException {
//                Request originalRequest = chain.request();
//                Request.Builder requestBuilder = originalRequest.newBuilder()
//                        .header("AppType", "TPOS")
//                        .header("Content-Type", "application/json")
//                        .header("Accept", "application/json")
//                        .header("apikey", "851c327e3a79257ce12bf4cb2f29eade")
//                        .method(originalRequest.method(), originalRequest.body());
//                Request request = requestBuilder.build();
//                return chain.proceed(request);
//            }
//        };

        mClientBuilder = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
//                .addInterceptor(headerInterceptor)
                .addInterceptor(CACHE_CONTROL_INTERCEPTOR)
                .cache(cache);
    }

    private ApiClient() {

        Retrofit mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
//                .addConverterFactory(SimpleXmlConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
//                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(mClientBuilder.build())
                .build();

        mApiService = mRetrofit.create(ApiService.class);

    }

    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final ApiClient INSTANCE = new ApiClient();
    }

    //获取单例
    public static ApiClient getInstance() {
        return SingletonHolder.INSTANCE;
    }
}
