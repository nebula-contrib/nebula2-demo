package myproject.dropping.nebulausage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Spring boot 入口类
 *
 * @author dropping
 * @date 2021/4/1
 */
@Controller
@ComponentScan(basePackages = {"myproject.dropping.nebulausage"})
@Configuration
@EnableAutoConfiguration
@Slf4j
public class AppMain {

    /**
     * 入口函数
     *
     * @param args 启动运行参数
     */
    public static void main(String[] args) {
        long t1 = System.currentTimeMillis();
        new SpringApplicationBuilder(AppMain.class).run(args);
        log.info("Nebula usage project started with {} ms", (System.currentTimeMillis() - t1));
    }

    /**
     * 用于检验服务是否正常运行
     *
     * @return 测试串
     */
    @RequestMapping(value = "/ok.htm", method = RequestMethod.GET)
    @ResponseBody
    String ok() {
        return "ok";
    }
}


