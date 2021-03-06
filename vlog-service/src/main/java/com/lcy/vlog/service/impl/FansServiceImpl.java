package com.lcy.vlog.service.impl;

import com.github.pagehelper.PageHelper;
import com.lcy.vlog.base.BaseInfoProperties;
import com.lcy.vlog.base.RabbitMQConfig;
import com.lcy.vlog.bo.VlogBO;
import com.lcy.vlog.enums.MessageEnum;
import com.lcy.vlog.enums.YesOrNo;
import com.lcy.vlog.mapper.FansMapper;
import com.lcy.vlog.mapper.FansMapperCustom;
import com.lcy.vlog.mapper.VlogMapper;
import com.lcy.vlog.mapper.VlogMapperCustom;
import com.lcy.vlog.mo.MessageMO;
import com.lcy.vlog.pojo.Fans;
import com.lcy.vlog.pojo.Vlog;
import com.lcy.vlog.service.FansService;
import com.lcy.vlog.service.MsgService;
import com.lcy.vlog.service.VlogService;
import com.lcy.vlog.utils.JsonUtils;
import com.lcy.vlog.utils.PagedGridResult;
import com.lcy.vlog.vo.FansVO;
import com.lcy.vlog.vo.IndexVlogVO;
import com.lcy.vlog.vo.VlogerVO;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

@Service
public class FansServiceImpl extends BaseInfoProperties implements FansService {

    @Autowired
    private FansMapper fansMapper;
    @Autowired
    private FansMapperCustom fansMapperCustom;

    @Autowired
    private MsgService msgService;

    @Autowired
    private Sid sid;
    @Autowired
    public RabbitTemplate rabbitTemplate;

    @Transactional
    @Override
    public void doFollow(String myId, String vlogerId) {

        String fid = sid.nextShort();

        Fans fans = new Fans();
        fans.setId(fid);
        fans.setFanId(myId);
        fans.setVlogerId(vlogerId);

        // 判断对方是否关注我，如果关注我，那么双方都要互为朋友关系
        Fans followedMe = queryFansRelationship(vlogerId, myId);
        if (followedMe != null) {
            fans.setIsFanFriendOfMine(YesOrNo.YES.type);

            followedMe.setIsFanFriendOfMine(YesOrNo.YES.type);
            fansMapper.updateByPrimaryKeySelective(followedMe);
        } else {
            fans.setIsFanFriendOfMine(YesOrNo.NO.type);
        }

        fansMapper.insert(fans);


        // 系统消息：关注
       //  msgService.createMsg(myId, vlogerId, MessageEnum.FOLLOW_YOU.type, null);
        // 优化：使用mq异步解耦
        MessageMO messageMO = new MessageMO();
        messageMO.setFromUserId(myId);
        messageMO.setToUserId(vlogerId);
        // 优化：使用mq异步解耦
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_MSG,
                "sys.msg." + MessageEnum.FOLLOW_YOU.enValue,
                JsonUtils.objectToJson(messageMO));
    }

    public Fans queryFansRelationship(String fanId, String vlogerId) {
        Example example = new Example(Fans.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("vlogerId", vlogerId);
        criteria.andEqualTo("fanId", fanId);

        List list = fansMapper.selectByExample(example);

        Fans fan = null;
        if (list != null && list.size() > 0 && !list.isEmpty()) {
            fan = (Fans) list.get(0);
        }

        return fan;
    }

    @Transactional
    @Override
    public void doCancel(String myId, String vlogerId) {

        // 判断我们是否朋友关系，如果是，则需要取消双方的关系
        Fans fan = queryFansRelationship(myId, vlogerId);
        if (fan != null && Objects.equals(fan.getIsFanFriendOfMine(), YesOrNo.YES.type)) {
            // 抹除双方的朋友关系，自己的关系删除即可
            Fans pendingFan = queryFansRelationship(vlogerId, myId);
            pendingFan.setIsFanFriendOfMine(YesOrNo.NO.type);
            fansMapper.updateByPrimaryKeySelective(pendingFan);
        }

        // 删除自己的关注关联表记录
        fansMapper.delete(fan);
    }

    @Override
    public boolean queryDoIFollowVloger(String myId, String vlogerId) {
//        Fans vloger = queryFansRelationship(myId, vlogerId);
//        return vloger != null;
        String followRelationshipStr = redis.get(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + myId + ":" + vlogerId);
        return StringUtils.isNotBlank(followRelationshipStr) && followRelationshipStr.equalsIgnoreCase("1");
    }

    @Override
    public PagedGridResult queryMyFollows(String myId,
                                          Integer page,
                                          Integer pageSize) {
        Map<String, Object> map = new HashMap<>();
        map.put("myId", myId);

        PageHelper.startPage(page, pageSize);

        List<VlogerVO> list = fansMapperCustom.queryMyFollows(map);

        return setterPagedGrid(list, page);
    }

    @Override
    public PagedGridResult queryMyFans(String myId,
                                       Integer page,
                                       Integer pageSize) {

        /*
         * <判断粉丝是否是我的朋友（互粉互关）>
         * 普通做法：
         * 多表关联+嵌套关联查询，这样会违反多表关联的规范，不可取，高并发下回出现性能问题
         *
         * 常规做法：
         * 1. 避免过多的表关联查询，先查询我的粉丝列表，获得fansList
         * 2. 判断粉丝关注我，并且我也关注粉丝 -> 循环fansList，获得每一个粉丝，再去数据库查询我是否关注他
         * 3. 如果我也关注他（粉丝），说明，我俩互为朋友关系（互关互粉），则标记flag为true，否则false
         *
         * 高端做法：
         * 1. 关注/取关的时候，关联关系保存在redis中，不要依赖数据库
         * 2. 数据库查询后，直接循环查询redis，避免第二次循环查询数据库的尴尬局面
         */


        Map<String, Object> map = new HashMap<>();
        map.put("myId", myId);

        PageHelper.startPage(page, pageSize);

        List<FansVO> list = fansMapperCustom.queryMyFans(map);

        for (FansVO f : list) {
            String relationship = redis.get(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + myId + ":" + f.getFanId());
            if (StringUtils.isNotBlank(relationship) && relationship.equalsIgnoreCase("1")) {
                f.setFriend(true);
            }
        }

        return setterPagedGrid(list, page);
    }
}
