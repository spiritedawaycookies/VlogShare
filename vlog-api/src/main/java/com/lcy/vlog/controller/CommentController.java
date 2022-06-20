package com.lcy.vlog.controller;

import com.lcy.vlog.base.BaseInfoProperties;
import com.lcy.vlog.base.RabbitMQConfig;
import com.lcy.vlog.bo.CommentBO;
import com.lcy.vlog.enums.MessageEnum;
import com.lcy.vlog.graceful.result.GracefulJSONResult;
import com.lcy.vlog.graceful.result.ResponseStatusEnum;
import com.lcy.vlog.mo.MessageMO;
import com.lcy.vlog.model.Stu;
import com.lcy.vlog.pojo.Comment;
import com.lcy.vlog.pojo.Vlog;
import com.lcy.vlog.service.CommentService;
import com.lcy.vlog.service.MsgService;
import com.lcy.vlog.service.VlogService;
import com.lcy.vlog.utils.JsonUtils;
import com.lcy.vlog.utils.SMSUtils;
import com.lcy.vlog.vo.CommentVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Api(tags = "CommentController 评论模块的接口")
@RequestMapping("comment")
@RestController
public class CommentController extends BaseInfoProperties {

    @Autowired
    private CommentService commentService;
    @Autowired
    private MsgService msgService;
    @Autowired
    private VlogService vlogService;
    @Autowired
    public RabbitTemplate rabbitTemplate;

    @PostMapping("create")
    public GracefulJSONResult create(@RequestBody @Valid CommentBO commentBO)
            throws Exception {
        if (commentBO.getFatherCommentId() != null && !commentBO.getFatherCommentId().equalsIgnoreCase("0")) {
            if (commentService.getComment(commentBO.getFatherCommentId()) == null)
                return GracefulJSONResult.errorCustom(ResponseStatusEnum.ITEM_NOT_EXIST_ERROR);
        }
        CommentVO commentVO = commentService.createComment(commentBO);
        return GracefulJSONResult.ok(commentVO);
    }

    @GetMapping("counts")
    public GracefulJSONResult counts(@RequestParam String vlogId) {

        String countsStr = redis.get(REDIS_VLOG_COMMENT_COUNTS + ":" + vlogId);
        if (StringUtils.isBlank(countsStr)) {
            countsStr = "0";
        }

        return GracefulJSONResult.ok(Integer.valueOf(countsStr));
    }

    @GetMapping("list")
    public GracefulJSONResult list(@RequestParam String vlogId,
                                   @RequestParam(defaultValue = "") String userId,
                                   @RequestParam Integer page,
                                   @RequestParam Integer pageSize) {

        return GracefulJSONResult.ok(
                commentService.queryVlogComments(
                        vlogId,
                        userId,
                        page,
                        pageSize));
    }

    @DeleteMapping("delete")
    public GracefulJSONResult delete(@RequestParam String commentUserId,
                                     @RequestParam String commentId,
                                     @RequestParam String vlogId) {
        commentService.deleteComment(commentUserId,
                commentId,
                vlogId);
        return GracefulJSONResult.ok();
    }

    @PostMapping("like")
    public GracefulJSONResult like(@RequestParam String commentId,
                                   @RequestParam String userId) {

        // 不要用hash 会有bigkey
        redis.increment(REDIS_VLOG_COMMENT_LIKED_COUNTS + ":" + commentId, 1);
        redis.set(REDIS_USER_LIKE_COMMENT + ":" + userId + ":" + commentId, "1");


//        // 系统消息：点赞评论
        Comment comment = commentService.getComment(commentId);
        Vlog vlog = vlogService.getVlog(comment.getVlogId());
        Map msgContent = new HashMap();
        msgContent.put("vlogId", vlog.getId());
        msgContent.put("vlogCover", vlog.getCover());
        msgContent.put("commentId", commentId);
//        msgService.createMsg(userId,
//                            comment.getCommentUserId(),
//                            MessageEnum.LIKE_COMMENT.type,
//                            msgContent);
        // MQ异步解耦
        MessageMO messageMO = new MessageMO();
        messageMO.setFromUserId(userId);
        messageMO.setToUserId(comment.getCommentUserId());
        messageMO.setMsgContent(msgContent);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_MSG,
                "sys.msg." + MessageEnum.LIKE_COMMENT.enValue,
                JsonUtils.objectToJson(messageMO));

        return GracefulJSONResult.ok();
    }

    @PostMapping("unlike")
    public GracefulJSONResult unlike(@RequestParam String commentId,
                                     @RequestParam String userId) {

        redis.decrement(REDIS_VLOG_COMMENT_LIKED_COUNTS + ":" + commentId, 1);
        redis.del(REDIS_USER_LIKE_COMMENT + ":" + userId + ":" + commentId);

        return GracefulJSONResult.ok();
    }
}
