package com.automation.zzx.intelligent_basket_demo.utils.okhttp;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by pengchenghu on 2019/2/28.
 * Author Email: 15651851181@163.com
 * Describe: 监听类 封装OKHttp
 */

public class BaseOkHttpClient {
    private Builder mBuilder;

    private BaseOkHttpClient(Builder builder) {
        this.mBuilder = builder;
    }

    public Request buildRequest() {
        Request.Builder builder = new Request.Builder();

        builder = buildHeaderParam(builder); // 添加头信息

        if (mBuilder.method == "GET") {
            builder.url(buildGetRequestParam());
            builder.get();
        } else if (mBuilder.method == "POST") {
            builder.url(mBuilder.url);
            try {
                builder.post(buildPostRequestParam());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return builder.build();
    }

    /*
     * Header 参数拼接
     */
    private Request.Builder buildHeaderParam(Request.Builder builder){
        if(mBuilder.headers.size() <= 0){
            return builder;
        }
        for (RequestParameter p : mBuilder.headers) {
            builder.addHeader(p.getKey(), p.getObj() == null ? "" : p.getObj().toString());
        }
        return builder;
    }

    /**
     * GET拼接参数
     *
     * @return
     */
    private String buildGetRequestParam() {
        if (mBuilder.params.size() <= 0) {
            return this.mBuilder.url;
        }
        Uri.Builder builder = Uri.parse(mBuilder.url).buildUpon();
        for (RequestParameter p : mBuilder.params) {
            builder.appendQueryParameter(p.getKey(), p.getObj() == null ? "" : p.getObj().toString());
        }
        String url = builder.build().toString();
        return url;
    }

    /**
     * POST拼接参数
     *
     * @return
     */
    private RequestBody buildPostRequestParam() throws JSONException {
        if (mBuilder.isJsonParam) {
            JSONObject jsonObj = new JSONObject();
            for (RequestParameter p : mBuilder.params) {
                jsonObj.put(p.getKey(), p.getObj());
            }
            String json = jsonObj.toString();
            return RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        }
        FormBody.Builder builder = new FormBody.Builder();
        for (RequestParameter p : mBuilder.params) {
            builder.add(p.getKey(), p.getObj() == null ? "" : p.getObj().toString());
        }
        return builder.build();
    }

    /**
     * 回调调用
     *
     * @param callBack
     */
    public void enqueue(BaseCallBack callBack) {
        OkHttpManage.getInstance().request(this, callBack);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private String url;
        private String method;
        private List<RequestParameter> params;
        private List<RequestParameter> headers;
        private boolean isJsonParam;

        public BaseOkHttpClient build() {
            return new BaseOkHttpClient(this);
        }

        private Builder() {
            method = "GET";
            params = new ArrayList<>();
            headers = new ArrayList<>();
            isJsonParam = false;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        /**
         * GET请求
         *
         * @return
         */
        public Builder get() {
            method = "GET";
            return this;
        }

        /**
         * POST请求
         *
         * @return
         */
        public Builder post() {
            method = "POST";
            return this;
        }

        /**
         * JSON参数
         *
         * @return
         */
        public Builder json() {
            isJsonParam = true;
            return post();
        }

        /**
         * Form请求
         *
         * @return
         */
        public Builder form() {
            return this;
        }

        /**
         * 添加头
         *
         * @param key
         * @param value
         * @return
         */
        public Builder addHeader(String key, Object value) {
            if (headers == null) {
                headers = new ArrayList<>();
            }
            headers.add(new RequestParameter(key, value));
            return this;
        }

        /**
         * 添加参数
         *
         * @param key
         * @param value
         * @return
         */
        public Builder addParam(String key, Object value) {
            if (params == null) {
                params = new ArrayList<>();
            }
            params.add(new RequestParameter(key, value));
            return this;
        }
    }
}
