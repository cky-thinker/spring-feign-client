# spring-feign-client
feign组件集成spring代码示例

# 案例使用指南:

- git clone git@github.com:cky-thinker/spring-feign-client.git

- 使用Idea或者Eclipse打开该项目,安装maven依赖

- 运行com.cky.Applicaiton,可以看到类似如下的运行结果:

```
22:58:41.365 [main] INFO  com.cky.Application - --- 使用Param提交示例 ---
22:58:41.548 [main] INFO  com.cky.Application - Result{resultcode='200', reason='Return Successd!', result=MobileInfo{province='陕西', city='宝鸡', areacode='0917', zip='721000', company='联通', card=''}, error_code=0}
22:58:41.548 [main] INFO  com.cky.Application - --- 使用map提交示例 ---
22:58:41.625 [main] INFO  com.cky.Application - Result{resultcode='200', reason='Return Successd!', result=MobileInfo{province='陕西', city='宝鸡', areacode='0917', zip='721000', company='联通', card=''}, error_code=0}
```

# 说明
此案例模拟调用的是[聚合开放api](www.juhe.cn)
只需要声明一个接口,便可以在spring中通过该接口的代理对象来进行远程调用,自动将json进行泛型序列化.

使用指南:
- 将下面两个文件copy到项目中,确保spring的能够扫描到
```
com.cky.config.FeignApi
com.cky.config.FeignClientRegister
```
- 在resources/application.properties 中添加配置项: feign.client.scan.path = xxx 设置自定义api接口扫描路径
- 在配置的api接口包中创建接口
- 给接口添加@FeignApi注解并指定url,具体可以参考该示例
- 在接口中添加Feign风格的方法 [Feign注解使用指南](https://github.com/OpenFeign/feign)   [中文版指南](https://www.cnblogs.com/chenkeyu/p/9017996.html)
- 在项目中使用@Autowired 注入Api使用即可

