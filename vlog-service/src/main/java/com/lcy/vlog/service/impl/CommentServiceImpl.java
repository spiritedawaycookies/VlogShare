package com.lcy.vlog.service.impl;

import com.github.pagehelper.PageHelper;
import com.lcy.vlog.base.BaseInfoProperties;
import com.lcy.vlog.base.RabbitMQConfig;
import com.lcy.vlog.bo.CommentBO;
import com.lcy.vlog.enums.MessageEnum;
import com.lcy.vlog.enums.YesOrNo;
import com.lcy.vlog.exceptions.GracefulException;
import com.lcy.vlog.exceptions.MyCustomException;
import com.lcy.vlog.graceful.result.ResponseStatusEnum;
import com.lcy.vlog.mapper.CommentMapper;
import com.lcy.vlog.mapper.CommentMapperCustom;
import com.lcy.vlog.mo.MessageMO;
import com.lcy.vlog.pojo.Comment;
import com.lcy.vlog.pojo.Vlog;
import com.lcy.vlog.service.CommentService;
import com.lcy.vlog.service.MsgService;
import com.lcy.vlog.service.VlogService;
import com.lcy.vlog.utils.JsonUtils;
import com.lcy.vlog.utils.PagedGridResult;
import com.lcy.vlog.vo.CommentVO;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentServiceImpl extends BaseInfoProperties implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private CommentMapperCustom commentMapperCustom;

    @Autowired
    private VlogService vlogService;
    @Autowired
    private MsgService msgService;

    @Autowired
    private Sid sid;
    @Autowired
    public RabbitTemplate rabbitTemplate;

    @Override
    public CommentVO createComment(CommentBO commentBO) {

        String commentId = sid.nextShort();
        if (commentBO.getCommentUserId() == null||commentBO.getVlogerId()==null ) {
            throw new MyCustomException(ResponseStatusEnum.USER_STATUS_ERROR);
        }
        Comment comment = new Comment();
        comment.setId(commentId);

        comment.setVlogId(commentBO.getVlogId());
        comment.setVlogerId(commentBO.getVlogerId());

        comment.setCommentUserId(commentBO.getCommentUserId());
        comment.setFatherCommentId(commentBO.getFatherCommentId());
        comment.setContent(commentBO.getContent());

        comment.setLikeCounts(0);
        comment.setCreateTime(new Date());

        commentMapper.insert(comment);

        // redis????????????service???????????????????????????
        redis.increment(REDIS_VLOG_COMMENT_COUNTS + ":" + commentBO.getVlogId(), 1);

        // ?????????????????????????????????????????????????????????
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(comment, commentVO);


        // ?????????????????????/??????
        Vlog vlog = vlogService.getVlog(commentBO.getVlogId());
        Map msgContent = new HashMap();
        msgContent.put("vlogId", vlog.getId());
        msgContent.put("vlogCover", vlog.getCover());
        msgContent.put("commentId", commentId);
        msgContent.put("commentContent", commentBO.getContent());
        Integer type = MessageEnum.COMMENT_VLOG.type;
        String routeType = MessageEnum.COMMENT_VLOG.enValue;
        if (StringUtils.isNotBlank(commentBO.getFatherCommentId()) &&
                !commentBO.getFatherCommentId().equalsIgnoreCase("0") ) {
            type = MessageEnum.REPLY_YOU.type;
            routeType = MessageEnum.REPLY_YOU.enValue;
        }

//        msgService.createMsg(commentBO.getCommentUserId(),
//                commentBO.getVlogerId(),
//                type,
//                msgContent);

        // MQ????????????
        MessageMO messageMO = new MessageMO();
        messageMO.setFromUserId(commentBO.getCommentUserId());
        messageMO.setToUserId(commentBO.getVlogerId());
        messageMO.setMsgContent(msgContent);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_MSG,
                "sys.msg." + routeType,
                JsonUtils.objectToJson(messageMO));
        return commentVO;
    }

    @Override
    public PagedGridResult queryVlogComments(String vlogId,
                                             String userId,
                                             Integer page,
                                             Integer pageSize) {

        Map<String, Object> map = new HashMap<>();
        map.put("vlogId", vlogId);

        PageHelper.startPage(page, pageSize);

        List<CommentVO> list = commentMapperCustom.getCommentList(map);

        for (CommentVO cv : list) {
            String commentId = cv.getCommentId();

            // ?????????????????????????????????????????????
            String countsStr = redis.get(REDIS_VLOG_COMMENT_LIKED_COUNTS+":"+commentId);
            Integer counts = 0;
            if (StringUtils.isNotBlank(countsStr)) {
                counts = Integer.valueOf(countsStr);
            }
            cv.setLikeCounts(counts);

            // ??????????????????????????????????????????
            String doILike = redis.get(REDIS_USER_LIKE_COMMENT+":"+userId + ":" + commentId);
            if (StringUtils.isNotBlank(doILike) && doILike.equalsIgnoreCase("1")) {
                cv.setIsLike(YesOrNo.YES.type);
            }
        }

        return setterPagedGrid(list, page);
    }

    @Override
    public void deleteComment(String commentUserId,
                              String commentId,
                              String vlogId) {

        Comment pendingDelete = new Comment();
        pendingDelete.setId(commentId);
        pendingDelete.setCommentUserId(commentUserId);

        commentMapper.delete(pendingDelete);

        // ?????????????????????
        redis.decrement(REDIS_VLOG_COMMENT_COUNTS + ":" + vlogId, 1);
    }

    @Override
    public Comment getComment(String id) {
        return commentMapper.selectByPrimaryKey(id);
    }
}
