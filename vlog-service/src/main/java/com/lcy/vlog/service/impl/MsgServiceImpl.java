package com.lcy.vlog.service.impl;

import com.lcy.vlog.base.BaseInfoProperties;
import com.lcy.vlog.enums.MessageEnum;
import com.lcy.vlog.mo.MessageMO;
import com.lcy.vlog.pojo.Users;
import com.lcy.vlog.repository.MessageRepository;
import com.lcy.vlog.service.MsgService;
import com.lcy.vlog.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MsgServiceImpl extends BaseInfoProperties implements MsgService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserService userService;

    @Override
    public void createMsg(String fromUserId,
                          String toUserId,
                          Integer type,
                          Map msgContent) {

        Users fromUser = userService.getUser(fromUserId);

        MessageMO messageMO = new MessageMO();

        messageMO.setFromUserId(fromUserId);
        messageMO.setFromNickname(fromUser.getNickname());
        messageMO.setFromFace(fromUser.getFace());

        messageMO.setToUserId(toUserId);

        messageMO.setMsgType(type);
        if (msgContent != null) {
            messageMO.setMsgContent(msgContent);
        }

        messageMO.setCreateTime(new Date());

        messageRepository.save(messageMO);
    }

    @Override
    public List<MessageMO> queryList(String toUserId,
                                     Integer page,
                                     Integer pageSize) {

        Pageable pageable = PageRequest.of(page,
                                            pageSize,
                                            Sort.Direction.DESC,
                                            "createTime");

        List<MessageMO> list =  messageRepository
                        .findAllByToUserIdEqualsOrderByCreateTimeDesc(toUserId,
                                                                pageable);
        for (MessageMO msg : list) {
            // ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            if (msg.getMsgType() != null && Objects.equals(msg.getMsgType(), MessageEnum.FOLLOW_YOU.type)) {
                Map map = msg.getMsgContent();
                if (map == null) {
                    map = new HashMap();
                }

                String relationship = redis.get(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + msg.getToUserId() + ":" + msg.getFromUserId());
                if (StringUtils.isNotBlank(relationship) && relationship.equalsIgnoreCase("1")) {
                    map.put("isFriend", true);
                } else {
                    map.put("isFriend", false);
                }
                msg.setMsgContent(map);
             }
        }
        return list;
    }
}
