import cn.xnatural.app.AppContext;
import cn.xnatural.app.ServerTpl;
import cn.xnatural.enet.event.EC;
import cn.xnatural.enet.event.EL;
import cn.xnatural.http.HttpServer;
import cn.xnatural.jpa.Repo;
import cn.xnatural.remoter.Remoter;
import cn.xnatural.sched.Sched;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public class AppTest {

    static final Logger log = LoggerFactory.getLogger(AppTest.class);

    @Test
    void appTest() throws Exception {
        final AppContext app = new AppContext();
        app.addSource(new ServerTpl("server1") {
            @EL(name = "sys.starting")
            void start() {
                log.info("{} start", name);
            }
        });
        app.addSource(new ServerTpl() {
            @Named
            ServerTpl server1;

            @EL(name = "sys.starting")
            void start() {
                log.info("{} ========= {}", name, server1.getName());
            }
        });
        addWeb(app);
        addSched(app);
        addJpa(app);
        app.addSource(new ServerTpl("remoter") {
            Remoter remoter;
            @EL(name = "sched.started")
            void start() {
                remoter = new Remoter(app.name(), app.id(), attrs(), exec(), ep, bean(Sched.class));
                exposeBean(remoter);
                exposeBean(remoter.getAioClient());
                ep.fire(name + ".started");
            }

            @EL(name = "sys.heartbeat", async = true)
            void heartbeat() {
                remoter.sync();
                remoter.getAioServer().clean();
            }
            @EL(name = "sys.stopping", async = true)
            void stop() { remoter.stop(); }
        });
        app.addSource(this);
        app.start();
        Thread.sleep(1000 * 60 * 10);
    }

    /**
     * 创建数据库操作
     */
    static void addJpa(AppContext app) {
        app.addSource(new ServerTpl("jpa_local") { //数据库 jpa_local
            Repo repo;
            @EL(name = "sys.starting", async = true)
            void start() {
                repo = new Repo(attrs()).init();
                exposeBean(repo);
                ep.fire(name + ".started");
            }

            @EL(name = "sys.stopping", async = true, order = 2f)
            void stop() { if (repo != null) repo.close(); }
        });
    }


    /**
     * 创建 web服务
     */
    static void addWeb(AppContext app) {
        app.addSource(
                new ServerTpl("web") { //添加http服务
                    HttpServer server;

                    @EL(name = "sys.starting", async = true)
                    void start() {
                        server = new HttpServer(app.attrs(name), exec());
                        server.buildChain(chain -> {
                            chain.get("test", hCtx -> hCtx.render("xxxxxx"));
                        });
                        server.start();
                        server.enabled = false;
                    }

                    @EL(name = "sys.started", async = true)
                    void started() {
                        for (Object ctrl : server.getCtrls()) exposeBean(ctrl);
                        server.enabled = true;
                    }

                    @EL(name = "sys.stop")
                    void stop() { if (server != null) server.stop(); }
                }
        );
    }


    /**
     * 添加时间调度服务
     */
    static void addSched(AppContext app) {
        app.addSource(new ServerTpl("sched") { // 定时任务
            Sched sched;
            @EL(name = "sys.starting", async = true)
            void start() {
                sched = new Sched(attrs(), exec()).init();
                exposeBean(sched);
                ep.fire(name + ".started");
            }

            @EL(name = "sched.after")
            void after(Duration duration, Runnable fn) {sched.after(duration, fn);}

            @EL(name = "sys.stopping", async = true)
            void stop() { if (sched != null) sched.stop(); }
        });
    }


    /**
     * 测试服务本地线程池
     */
    @EL(name = "sys.started", async = true)
    void testServerExec(EC ec) {
        AppContext app = (AppContext) ec.source();
        AtomicInteger cnt = new AtomicInteger();
        app.bean(Sched.class, null).fixedDelay(Duration.ofSeconds(5), () -> {
            app.exec().execute(() -> {
                try {
                    Thread.sleep(1000 * 20);
                    log.info("===== " + cnt.incrementAndGet() + ", " + app.exec().toString());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        });
    }


    @Test
    void sysLoadTest() throws Exception {
        final AppContext app = new AppContext();
        app.start();
        for (int i = 0; i < 100000; i++) {
            int finalI = i;
            app.exec().execute(() -> {
                System.out.println("Task " + finalI);
            });
        }
        Thread.sleep(10 * 60 * 1000);
    }
}
