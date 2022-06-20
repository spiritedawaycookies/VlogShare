package com.lcy.vlog.controller;

import com.lcy.vlog.base.BaseInfoProperties;
import com.lcy.vlog.graceful.result.GracefulJSONResult;
import com.lcy.vlog.mo.MessageMO;
import com.lcy.vlog.model.Stu;
import com.lcy.vlog.service.MsgService;
import com.lcy.vlog.utils.SMSUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Api(tags = "MsgController 消息功能模块的接口")
@RequestMapping("msg")
@RestController
public class MsgController extends BaseInfoProperties {

    @Autowired
    private MsgService msgService;

    @GetMapping("list")
    public GracefulJSONResult list(@RequestParam String userId,
                                @RequestParam Integer page,
                                @RequestParam Integer pageSize) {

        // mongodb 从0分页，区别于数据库
        if (page == null) {
            page = COMMON_START_PAGE_ZERO;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        List<MessageMO> list = msgService.queryList(userId, page, pageSize);

        return GracefulJSONResult.ok(list);
    }
}
