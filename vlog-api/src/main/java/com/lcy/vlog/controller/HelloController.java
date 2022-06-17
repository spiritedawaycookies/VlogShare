package com.lcy.vlog.controller;

import com.lcy.vlog.graceful.result.GracefulJSONResult;
import com.lcy.vlog.model.Stu;
import com.lcy.vlog.utils.SMSUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api(tags = "Hello 测试的接口")
@RestController
public class HelloController {

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
}
