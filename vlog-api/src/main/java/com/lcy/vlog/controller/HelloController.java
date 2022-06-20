package com.lcy.vlog.controller;

import com.lcy.vlog.base.RabbitMQConfig;
import com.lcy.vlog.graceful.result.GracefulJSONResult;
import com.lcy.vlog.model.Stu;
import com.lcy.vlog.utils.SMSUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api(tags = "Hello 测试的接口")
@RestController
@RefreshScope //可以刷新诶配置
public class HelloController {
    @Value("${nacos.counts}") //测试自定义的配置
    private Integer nacosCounts;

    @GetMapping("nacosCounts")
    public Object nacosCounts() {
        return GracefulJSONResult.ok("nacosCounts的数值为：" + nacosCounts);
    }

    @ApiOperation(value = "hello - test router")
    @GetMapping("hello")
    public Object hello() {

        Stu stu = new Stu("lcy", 18);
        log.debug(stu.toString());
        log.info(stu.toString());
        log.warn(stu.toString());
        log.error(stu.toString());

        return GracefulJSONResult.ok(stu);
//        return GracefulJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR_GLOBAL);
//        return GracefulJSONResult.ok("Hello SpringBoot~");
    }

    @Autowired
    private SMSUtils smsUtils;

    //测试发短信
    @GetMapping("sms")
    public Object sms() throws Exception {

        String code = "123456";
        smsUtils.sendSMS("", code);

        return GracefulJSONResult.ok();
    }

    @Autowired
    public RabbitTemplate rabbitTemplate;

    @GetMapping("produce")
    public Object produce() throws Exception {


        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_MSG,
                "sys.msg.send",
                "我发了一个消息~~");


        /**
         * 路由规则
         * route-key
         * display.*.*
         *      display.a.b
         *      display.public.msg
         *      display.a.b.c不可
         *      * 代表一个占位符
         *
         *  display.#
         *      display.a.b
         *      display.a.b.c.d
         *      display.public.msg
         *      display.delete.msg.do
         *      # 代表多个占位符
         *
         */

        return GracefulJSONResult.ok();
    }

    @GetMapping("produce2")
    public Object produce2() throws Exception {

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_MSG,
                "sys.msg.delete",
                "我删除了一个消息~~");

        return GracefulJSONResult.ok();
    }

}
