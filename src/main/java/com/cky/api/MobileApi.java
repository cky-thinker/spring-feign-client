package com.cky.api;

import com.cky.DO.MobileInfo;
import com.cky.DO.Result;
import com.cky.config.FeignApi;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;

import java.util.Map;

@FeignApi(serviceUrl = "http://apis.juhe.cn/")
public interface MobileApi {

    @RequestLine("GET /mobile/get?phone={phone}&dtype={dtype}&key={key}")
    Result<MobileInfo> getByParam(@Param("phone") String phone,
                                  @Param("dtype") String dtype,
                                  @Param("key") String key);

    @RequestLine("GET /mobile/get")
    Result<MobileInfo> getByQueryMap(@QueryMap Map<String, Object> queryMap);

}
