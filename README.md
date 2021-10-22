# 介绍
小巧的java应用微内核框架. 基于 [enet](https://gitee.com/xnat/enet) 事件环型框架结构

> 系统只一个公用线程池: 所有的执行都被抽象成Runnable加入到公用线程池中执行
> > 系统中的任务只有在线程池到达最大后,才需要排对执行(和默认线程池的行为不同)

> 上层业务不需要创建线程池和线程增加复杂度, 而使用Devourer控制执行并发

> 所以系统性能只由线程池大小属性 sys.exec.corePoolSize=8, sys.exec.maximumPoolSize=30 和 jvm内存参数 -Xmx512m 控制


<!--
@startuml
skinparam ConditionEndStyle hline
split
   -[hidden]->
   :服务 1;
split again
   -[hidden]->
   :服务 2;
split again
   -[hidden]->
   :服务 n;
end split

if (任务(Runnable)) then (对列执行器/吞噬器)
  fork
    -> 并发 ;
    :Devourer 1;
    :one-by-one;
    -> 提交 ;
  fork again
    -> 并发 ;
    :Devourer 2;
    :two-by-two;
    -> 提交 ;
  fork again
    -> 并发 ;
    :Devourer n;
    :n-by-n;
    -> 提交 ;
  end fork
else
  fork
    :任务 1;
  fork again
    :任务 2;
  fork again
    :任务 n;
  end fork
endif

:系统线程池;
@enduml
-->
![Image text](http://www.plantuml.com/plantuml/png/fPBFIWCn4CRlUOevjeT5skC6Ia5z0JsAXvHCRMWwARkfqZVmZz9waeAe1S4U1151QFVWopIxFePPTmiAfGYUPkVxllrD9gGPMn7PGK-pkAkeBoBHWsr3KbbbQ9ValXrgX8vMX2pkQsKP00G77IKbqc7GoDimlRrovrEeyI82uaWesX2i_pL8d259A0OprORACacyKTaR48cMnceLR0S9AkvbxavlqhEdp-cbcyostFZEJPxzLzNpsYRgvbW86k3cxsvq3Vox3tVms0mYZA1M2eOmQ0q3N_ZgZtGqNYuUWPUWyj2RSVLIi2Scn_doBnoL0kKmMeT2aiMQg6FykN9Ot1ZKrWnSUVKD7lscarHjniBiBScI-spnaSqkqjS7pfhUyQ_e2m00)


# 安装教程
```xml
<dependency>
    <groupId>cn.xnatural</groupId>
    <artifactId>tiny</artifactId>
    <version>1.0.10</version>
</dependency>
```

# 可搭配其它服务
[http](https://gitee.com/xnat/http), [jpa](https://gitee.com/xnat/jpa),
[sched](https://gitee.com/xnat/sched), [remoter](https://gitee.com/xnat/remoter)

# 初始化
```java
final AppContext app = new AppContext(); // 创建一个应用
app.addSource(new ServerTpl("server1") { // 添加服务 server1
    @EL(name = "sys.starting")
    void start() {
        log.info("{} start", name);
    }
});
app.addSource(new TestService()); // 添加自定义服务
app.start(); // 应用启动(会依次触发系统事件)
```

> 基于事件环型框架结构图
> 
> > 以AppContext#EP为事件中心挂载服务结构
<!--
@startuml
class AppContext {
  #EP ep
  #Map<String, Object> sourceMap
  #Map<String, Devourer> queues

  +addSource(Object source)
  +start()
}

note left of AppContext::ep
  事件中心
end note

note right of AppContext::sourceMap
  所有被添加的Server
end note

note left of AppContext::queues
  所有被Server#queue 方添加的
  对列执行器
end note

note right of AppContext::addSource
  添加服务
end note

note left of AppContext::start
1. 触发事件 sys.inited 系统配置加载完成, 线程池, 事件中心初始化已完成
2. 触发事件 sys.starting 调用所有服务所包含@EL(name = "sys.starting") 的监听器方法
3. sys.starting 所有监听执行完成后, 继续触发 sys.started 事件
end note

class EP {
  #{field} Map<String, Listener> lsMap;
  +{method} fire(String 事件名, Object...参数);
}

note left of EP::lsMap
  所有监听器映射: 关联@EL方法
end note

note right of EP::fire
  事件触发
end note

AppContext <|-- EP
EP <|-- Server1 : 收集监听器
EP <|-- Server2 : 收集监听器
EP <|-- Server3 : 收集监听器


class Server1 {
  ...启动监听...
  @EL(name = "sys.starting")
  void start()
  
  ...停止监听...
  @EL(name = "sys.stopping")
  void stop()

  ...其它监听...
  @EL(name = "xx")
  {method} void xx()

  ...基本功能...

  {method} bean(Class bean类型, String bean名)

  {method} queue(String 对列名, Runnabel 任务)

  {method} async(Runnable 异步任务)

  {method} get[Integer, Long, Boolean, Str](String 属性名)
}

class Server2  {
  ...启动监听...
  @EL(name = "sys.starting")
  void start()

  ...其它监听...
  @EL(name = "oo")
  {method} String oo()

  ...基本功能...

  {method} bean(Class bean类型, String bean名)

  {method} queue(String 对列名, Runnabel 任务)

  {method} async(Runnable 异步任务)

  {method} get[Integer, Long, Boolean, Str](String 属性名)
}

class Server3  {  
  ...停止监听...
  @EL(name = "sys.stopping")
  void stop()

  ...其它监听...
  @EL(name = "aa")
  {method} aa()

  ...基本功能...

  {method} bean(Class bean类型, String bean名)

  {method} queue(String 对列名, Runnabel 任务)

  {method} async(Runnable 异步任务)

  {method} get[Integer, Long, Boolean, Str](String 属性名)
}
@enduml
-->

![Image text](http://www.plantuml.com/plantuml/png/vLJDJjjE7BpxANw2Iw9_Y0JS0dz4ItEeH5LKZbKF9ju4LuutjHqKH960a0gIK884z8EK3wGsaI1y50aARiJBPDVXBRhs6kDGe4YLUk6stfsPdPsTTRzkY9gHJYf2J15r7HwbKWDODL36W0a1e3qw12Xb3vw9gTvXGvFLH0YUZxn6CQCFT9pMOeYjN0SyGMDi2Mbzy2QDqaWN6E0_KPA67KA0yrrwq5vpN0I2mgGWgDX0eA2u0JZkinE9E3uQPuM6UTpuKIFdMG6f4jXmbwJ9YT7VM7wFT7wAbkURsplqn2JvJUlpx33Inf3c2TsnEp-8NuHpsvq5eAkddYW3aVrJClU1pbUQMqNogNelfru-ZC-rQ7c1vBVkuyx9J-WCGxFoZImkyPH07zV3iYeRI0BhoBJCZOlSWbNVOyhDUfti5UbSAGJMsRbLBT33pL1Bk6Jk2waKI76Ld7pdKA7h1dbdOtRdq3p8MijL7WxtpSQac2EbdVxeO40LamZ-XpO_foq8B2rhROcKTbb8TeH7Aq9tk5MOIt8K3vJR8QNtpBnPiSmQTtL5Gv9x55zqlDxH8LxhYRYC56aI_AKTb7K3gNPf5PtDzzYzd4WYOnGpO5pMK80ZNMrIMhXy2U5mc2pEq9M3OC_r1hCT8n55z_Vlwi0VDyd1R0H8xgWvlSnIuWdSKXOkPVlmdW4_jm_lUxszRpiw64E83l4XRsidH80k7r-ilVDSN4Dq_H7HVGF2pTVRnGxPJgMqJ_9L3cEVRFBsBh35CInBSFah070rfikqjdst1awbMZNO39Dm1NB7P2zxcq0cuz2yYtRucSmLU-ECbdT9VgEPhTjiFtO4YMfWm3wuCxGEJR9U206lYJF5IXBqK_Z_q2sI-vTmYlGYhQhY25AWOPhixRIIH7rSZGKuH450VixGsjURW0bal7og6YY1DDPdRBVwCSOAC-AuUkLjVBXEfogEkSdMg-k2lx-x-mMFSMlmhZMC7shqtKpj7vLU55kp5yK757e_KgLqKla5)

## 系统事件: app.start() 后会依次触发 sys.inited, sys.starting, sys.started
+ sys.inited: 应用始化完成(环境配置, 系统线程池, 事件中心)
+ sys.starting: 通知所有服务启动. 一般为ServerTpl
+ sys.started: 应用启动完成
+ sys.stopping: 应用停止事件(kill pid)

## 环境配置
>+ 系统属性(-Dconfigname): configname 指定配置文件名. 默认:app
>+ 系统属性(-Dprofile): profile 指定启用特定的配置
>+ 系统属性(-Dconfigdir): configdir 指定额外配置文件目录

* 只读取properties文件. 按顺序读取app.properties, app-[profile].properties 两个配置文件
* 配置文件支持简单的 ${} 属性替换

  > 加载顺序(优先级从低到高):
  * classpath:app.properties, classpath:app-[profile].properties
  * file:./app.properties, file:./app-[profile].properties
  * configdir:app.properties, configdir:app-[profile].properties
  * 自定义环境配置(重写方法): AppContext#customEnv(Map)
  * System.getProperties()

## 添加 [http](https://gitee.com/xnat/http) 服务
```properties
### app.propertiees
web.hp=:8080
```
```java
app.addSource(new ServerTpl("web") { //添加web服务
    HttpServer server;
    @EL(name = "sys.starting", async = true)
    void start() {
        server = new HttpServer(app().attrs(name), exec());
        server.buildChain(chain -> {
            chain.get("get", hCtx -> {
                hCtx.render("xxxxxxxxxxxx");
            });
        }).start();
    }
    @EL(name = "sys.stopping")
    void stop() {
        if (server != null) server.stop();
    }
});
```

## 添加 [jpa](https://gitee.com/xnat/jpa)
```properties
### app.properties
jpa_local.url=jdbc:mysql://localhost:3306/test?useSSL=false&user=root&password=root
```
```java
app.addSource(new ServerTpl("jpa_local") { //数据库 jpa_local
    Repo repo;
    @EL(name = "sys.starting", async = true)
    void start() {
        repo = new Repo(attrs()).init();
        exposeBean(repo); // 把repo暴露给全局
        ep.fire(name + ".started");
    }

    @EL(name = "sys.stopping", async = true)
    void stop() { if (repo != null) repo.close(); }
});
```

## 动态添加服务
```java
@EL(name = "sys.inited")
void sysInited() {
    if (!app.attrs("redis").isEmpty()) { //根据配置是否有redis,创建redis客户端工具
        app.addSource(new RedisClient())
    }
}
```

## 系统心跳
> 需要用 [sched](https://gitee.com/xnat/sched) 添加 _sched.after_ 事件监听
```java
@EL(name = "sched.after")
void after(Duration duration, Runnable fn) {sched.after(duration, fn);}
```
> 每隔一段时间触发一次心跳, 1~4分钟(两个配置相加)随机心跳
> + 配置(sys.heartbeat.minInterval) 控制心跳最小时间间隔
> + 配置(sys.heartbeat.randomInterval) 控制心跳最大时间间隔

## 服务基础类: ServerTpl
> 推荐所有被加入到AppContext中的服务都是ServerTpl的子类
```java
app.addSource(new ServerTpl("服务名") {
    
    @EL(name = "sys.starting", async = true)
    void start() {
        // 初始化服务
    }
})
```

### bean注入:按类型匹配 @Inject
```java
app.addSource(new ServerTpl() {
    @Inject Repo repo;  //自动注入, 按类型

    @EL(name = "sys.started", async = true)
    void init() {
        List<Map> rows = repo.rows("select * from test")
        log.info("========= {}", rows);
    }
});
```

### bean注入:按类型和名字全匹配 @Named
```java
app.addSource(new ServerTpl("testNamed") {
    @Named ServerTpl server1; //自动注入, 按类型和名字

    @EL(name = "sys.started", async = true)
    void init() {
        log.info("{} ========= {}", name, server1.getName());
    }
});
```

### 动态bean获取: 方法 bean(Class bean类型, String bean名字)
```java
app.addSource(new ServerTpl() {
    @EL(name = "sys.started", async = true)
    void start() {
        String str = bean(Repo).firstRow("select count(1) as total from test").get("total").toString()；
        log.info("=========" + str);
    }
});
```

### bean依赖注入原理
> 两种bean容器: AppContext是全局bean容器, 每个服务(ServerTpl)都是一个bean容器
> > 获取bean对象: 先从全局查找, 再从每个服务中获取

* 暴露全局bean
  ```java
  app.addSource(new TestService());
  ```
* 服务(ServerTpl)里面暴露自己的bean
  ```java
  Repo repo = new Repo("jdbc:mysql://localhost:3306/test?user=root&password=root").init();
  exposeBean(repo); // 加入到bean容器,暴露给外部使用
  ```

### 属性直通车
> 服务(ServerTpl)提供便捷方法获取配置.包含: getLong, getInteger, getDouble, getBoolean等
```properties
## app.properties
testSrv.prop1=1
testSrv.prop2=2.2
```
```java
app.addSource(new ServerTpl("testSrv") {
    @EL(name = "sys.starting")
    void init() {
        log.info("print prop1: {}, prop2: {}", getInteger("prop1"), getDouble("prop2"));    
    }
})
```

### 提交异步任务
```java
async(() -> {
    // 异步执行任务
})
```
### 创建任务对列
```java
queue("toEs", () -> {
    // 提交数据到es
})
```

## 对列执行器/并发控制器 Devourer
> 当需要控制任务最多 一个一个, 两个两个... 的执行时
>
> > 服务基础类(ServerTpl)提供方法: queue

### 创建对列执行器
```java
queue("save")
    .failMaxKeep(10000) // 最多保留失败的任务个数, 默认不保留
    .parallel(2) // 最多同时执行任务数, 默认1(one-by-one)
    .errorHandle {ex, me ->
        // 当任务执行抛错时执行
    };
```
### 添加任务到队列
```java
// 方法1
queue("save", () -> {
    // 执行任务
});
// 方法2
queue("save").offer(() -> {
    // 执行任务
});
```
### 队列控制: 暂停/恢复
```java
// 暂停执行, 一般用于发生错误时
// 注: 必须有新的任务入对, 重新触发继续执行. 或者resume方法手动恢复执行
queue("save")
    .errorHandle {ex, me ->
        // 发生错误时, 让对列暂停执行(不影响新任务入对)
        // 1. 暂停一段时间
        me.suspend(Duration.ofSeconds(180));
        // 2. 条件暂停
        // me.suspend(queue -> true);
    };
```
### 队列最后任务有效
```java
// 是否只使用队列最后一个, 清除队列前面的任务
// 适合: 入队的频率比出队高, 前面的任务可有可无
// 例: increment数据库的一个字段的值
Devourer q = queue("increment").useLast(true);
for (int i = 0; i < 20; i++) {
    // 入队快, 任务执行慢， 中间的可以不用执行
    q.offer(() -> repo.execute("update test set count=?", i));
}
```

### 并发流量控制锁 LatchLock
> 当被执行代码块需要控制同时线程执行的个数时
```java
final LatchLock lock = new LatchLock();
lock.limit(3); // 设置并发限制. 默认为1
if (lock.tryLock()) { // 尝试获取一个锁
    try {
        // 被执行的代码块    
    } finally {
        lock.release(); // 释放一个锁
    }
}
```

## 数据库操作工具
#### 创建一个数据源
```java
DB repo = new DB("jdbc:mysql://localhost:3306/test?useSSL=false&user=root&password=root&allowPublicKeyRetrieval=true");
```
#### 查询单条记录
```java
repo.row("select * from test order by id desc");
```
#### 查询多条记录
```java
repo.rows("select * from test limit 10");
repo.rows("select * from test where id in (?, ?)", 2, 7);
```
#### 查询单个值
```java
// 只支持 Integer.class, Long.class, String.class, Double.class, BigDecimal.class, Boolean.class, Date.class
repo.single("select count(1) from test", Integer.class);
```
#### 插入一条记录
```java
repo.execute("insert into test(name, age, create_time) values(?, ?, ?)", "方羽", 5000, new Date());
```
#### 更新一条记录
```java
repo.execute("update test set age = ? where id = ?", 10, 1)
```
#### 事务
```java
// 执行多条sql语句
repo.trans(() -> {
    // 插入并返回id
    Object id = repo.insertWithGeneratedKey("insert into test(name, age, create_time) values(?, ?, ?)", "方羽", 5000, new Date());
    repo.execute("update test set age = ? where id = ?", 18, id);
    return null;
});
```

## http客户端
```java
// get
Utils.http().get("http://xnatural.cn:9090/test/cus?p2=2")
    .header("test", "test") // 自定义header
    .cookie("sessionId", "xx") // 自定义 cookie
    .connectTimeout(5000) // 设置连接超时 5秒
    .readTimeout(15000) // 设置读结果超时 15秒
    .param("p1", 1) // 添加参数
    .debug().execute();
```
```java
// post
Utils.http().post("http://xnatural.cn:9090/test/cus")
    .debug().execute();
```
```java
// post 表单
Utils.http().post("http://xnatural.cn:9090/test/form")
    .param("p1", "p1")
    .debug().execute();
```
```java
// post 上传文件
Utils.http().post("http://xnatural.cn:9090/test/upload")
    .param("file", new File("d:/tmp/1.txt"))
    .debug().execute();

// post 上传文件流. 一般上传大文件 可配合 汇聚流 使用
Utils.http().post("http://xnatural.cn:9090/test/upload")
    .fileStream("file", "test.md", new FileInputStream("d:/tmp/test.md"))
    .debug().execute();
```
```java
// post json
Utils.http().post("http://xnatural.cn:9090/test/json")
    .jsonBody(new JSONObject().fluentPut("p1", 1).toString())
    .debug().execute();
```
```java
// post 普通文本
Utils.http().post("http://xnatural.cn:9090/test/string")
    .textBody("xxxxxxxxxxxxxxxx")
    .debug().execute();
```

## 对象拷贝器
#### javabean 拷贝到 javabean
```java
Utils.copier(
      new Object() {
          public String name = "徐言";
      }, 
      new Object() {
          private String name;
          public void setName(String name) { this.name = name; }
          public String getName() { return name; }
      }
).build();
```
#### 对象 转换成 map
```java
Utils.copier(
      new Object() {
          public String name = "方羽";
          public String getAge() { return 5000; }
      }, 
      new HashMap()
).build();
```
#### 添加额外属性源
```java
Utils.copier(
      new Object() {
          public String name = "云仙君";
      }, 
      new Object() {
          private String name;
          public Integer age;
          public void setName(String name) { this.name = name; }
          public String getName() { return name; }
          
      }
).add("age", () -> 1).build();
```
#### 忽略属性
```java
Utils.copier(
      new Object() {
          public String name = "徐言";
          public Integer age = 22;
      }, 
      new Object() {
          private String name;
          public Integer age = 33;
          public void setName(String name) { this.name = name; }
          public String getName() { return name; }
          
      }
).ignore("age").build(); // 最后 age 为33
```
#### 属性值转换
```java
Utils.copier(
      new Object() {
          public long time = System.currentTimeMillis();
      }, 
      new Object() {
          private String time;
          public void setTime(String time) { this.time = time; }
          public String getTime() { return time; }
          
      }
).addConverter("time", o -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date((long) o)))
        .build();
```
#### 属性值转换
```java
Utils.copier(
      new Object() {
          public String name;
      }, 
      new Object() {
          private String name = "方羽";
          public void setName(String name) { this.name = name; }
          public String getName() { return name; }
          
      }
).ignoreNull(true).build(); // 最后 name 为 方羽
```
#### 属性名映射
```java
Utils.copier(
      new Object() {
          public String p1 = "徐言";
      }, 
      new Object() {
          private String pp1 = "方羽";
          public void setPp1(String pp1) { this.pp1 = pp1; }
          public String getPp1() { return pp1; }
          
      }
).mapProp( "p1", "pp1").build(); // 最后 name 为 徐言
```

## 文件内容监控器(类linux tail)
```java
Utils.tailer().tail("d:/tmp/tmp.json", 5);
```

## 无限递归优化实现 Recursion
> 解决java无尾递归替换方案. 例:
  ```java
  System.out.println(factorialTailRecursion(1, 10_000_000).invoke());
  ```
  ```java
  /**
   * 阶乘计算
   * @param factorial 当前递归栈的结果值
   * @param number 下一个递归需要计算的值
   * @return 尾递归接口,调用invoke启动及早求值获得结果
   */
  Recursion<Long> factorialTailRecursion(final long factorial, final long number) {
      if (number == 1) {
          // new Exception().printStackTrace();
          return Recursion.done(factorial);
      }
      else {
          return Recursion.call(() -> factorialTailRecursion(factorial + number, number - 1));
      }
  }
  ```
> 备忘录模式:提升递归效率. 例:
  ```java
  System.out.println(fibonacciMemo(47));
  ```
  ```java
  /**
   * 使用同一封装的备忘录模式 执行斐波那契策略
   * @param n 第n个斐波那契数
   * @return 第n个斐波那契数
   */
  long fibonacciMemo(long n) {
      return Recursion.memo((fib, number) -> {
          if (number == 0 || number == 1) return 1L;
          return fib.apply(number-1) + fib.apply(number-2);
      }, n);
  }
  ```

<!--
参照: 
  - https://www.cnblogs.com/invoker-/p/7723420.html
  - https://www.cnblogs.com/invoker-/p/7728452.html
-->

## 简单缓存 CacheSrv
```java
// 添加缓存服务
app.addSource(new CacheSrv());
```
```properties

## app.properties 缓存最多保存100条数据
cacheSrv.itemLimit=100
```
```java
// 1. 设置缓存
bean(CacheSrv).set("缓存key", "缓存值", Duration.ofMinutes(30));
// 2. 获取缓存
bean(CacheSrv).get("缓存key");
// 3. 过期设置
bean(CacheSrv).expire("缓存key", Duration.ofMinutes(30));
// 4. 手动删除
bean(CacheSrv).remove("缓存key");
```

## 延迟对象 Lazier
> 封装是一个延迟计算值(只计算一次)
```java
final Lazier<String> _id = new Lazier<>(() -> {
    String id = getHeader("X-Request-ID");
    if (id != null && !id.isEmpty()) return id;
    return UUID.randomUUID().toString().replace("-", "");
});
```
* 延迟获取属性值
  ```java
  final Lazier<String> _name = new Lazier<>(() -> getAttr("sys.name", String.class, "app"));
  ```
* 重新计算
  ```java
  final Lazier<Integer> _num = new Lazier(() -> new Random().nextInt(10));
  _num.get();
  _num.clear(); // 清除重新计算
  _num.get();
  ```


## 应用例子
最佳实践: [Demo(java)](https://gitee.com/xnat/appdemo)
, [Demo(scala)](https://gitee.com/xnat/tinyscalademo)
, [GRule(groovy)](https://gitee.com/xnat/grule)

# 1.0.11 ing
- [ ] CacheSrv accessTime

# 参与贡献

xnatural@msn.cn
