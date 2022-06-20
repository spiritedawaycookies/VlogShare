package com.lcy.vlog.service.impl;

import com.github.pagehelper.PageHelper;
import com.lcy.vlog.base.BaseInfoProperties;
import com.lcy.vlog.base.RabbitMQConfig;
import com.lcy.vlog.bo.VlogBO;
import com.lcy.vlog.enums.MessageEnum;
import com.lcy.vlog.enums.YesOrNo;
import com.lcy.vlog.mapper.MyLikedVlogMapper;
import com.lcy.vlog.mapper.VlogMapper;
import com.lcy.vlog.mapper.VlogMapperCustom;
import com.lcy.vlog.mo.MessageMO;
import com.lcy.vlog.pojo.MyLikedVlog;
import com.lcy.vlog.pojo.Vlog;
import com.lcy.vlog.service.FansService;
import com.lcy.vlog.service.MsgService;
import com.lcy.vlog.service.VlogService;
import com.lcy.vlog.utils.JsonUtils;
import com.lcy.vlog.utils.PagedGridResult;
import com.lcy.vlog.vo.IndexVlogVO;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VlogServiceImpl extends BaseInfoProperties implements VlogService {

    @Autowired
    private VlogMapper vlogMapper;

    @Autowired
    private VlogMapperCustom vlogMapperCustom;

    @Autowired
    private MyLikedVlogMapper myLikedVlogMapper;

    @Autowired
    private FansService fansService;
    @Autowired
    private MsgService msgService;

    @Autowired
    private Sid sid;
    @Autowired
    public RabbitTemplate rabbitTemplate;

    @Transactional
    @Override
    public void createVlog(VlogBO vlogBO) {

        String vid = sid.nextShort();

        Vlog vlog = new Vlog();
        BeanUtils.copyProperties(vlogBO, vlog);

        vlog.setId(vid);

        vlog.setLikeCounts(0);
        vlog.setCommentsCounts(0);
        vlog.setIsPrivate(YesOrNo.YES.type);

        vlog.setCreatedTime(new Date());
        vlog.setUpdatedTime(new Date());

        vlogMapper.insert(vlog);
    }

    @Override
    public PagedGridResult getIndexVlogList(String userId,
                                            String search,
                                            Integer page,
                                            Integer pageSize) {

        PageHelper.startPage(page, pageSize); //面向切面,以下的list已经是分好页了

        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isNotBlank(search)) {
            map.put("search", search);
        }
        List<IndexVlogVO> list = vlogMapperCustom.getIndexVlogList(map);

        for (IndexVlogVO v : list) {
            String vlogerId = v.getVlogerId();
            String vlogId = v.getVlogId();

            if (StringUtils.isNotBlank(userId)) {
                // 用户是否关注该博主
                boolean doIFollowVloger = fansService.queryDoIFollowVloger(userId, vlogerId);
                v.setDoIFollowVloger(doIFollowVloger);

                // 判断当前用户是否点赞过视频
                v.setDoILikeThisVlog(doILikeVlog(userId, vlogId));
            }

            // 获得当前视频被点赞过的总数
            v.setLikeCounts(getVlogBeLikedCounts(vlogId));
        }

//        return list;
        return setterPagedGrid(list, page);
    }

    private IndexVlogVO setterVO(IndexVlogVO v, String userId) {
        String vlogerId = v.getVlogerId();
        String vlogId = v.getVlogId();

        if (StringUtils.isNotBlank(userId)) {
            // 用户是否关注该博主
            boolean doIFollowVloger = fansService.queryDoIFollowVloger(userId, vlogerId);
            v.setDoIFollowVloger(doIFollowVloger);

            // 判断当前用户是否点赞过视频
            v.setDoILikeThisVlog(doILikeVlog(userId, vlogId));
        }

        // 获得当前视频被点赞过的总数
        v.setLikeCounts(getVlogBeLikedCounts(vlogId));

        return v;
    }

    @Override
    public Integer getVlogBeLikedCounts(String vlogId) {
        String countsStr = redis.get(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId);
        if (StringUtils.isBlank(countsStr)) {
            countsStr = "0";
        }
        return Integer.valueOf(countsStr);
    }

    private boolean doILikeVlog(String myId, String vlogId) {

        String doILike = redis.get(REDIS_USER_LIKE_VLOG + ":" + myId + ":" + vlogId);
        boolean isLike = false;
        if (StringUtils.isNotBlank(doILike) && doILike.equalsIgnoreCase("1")) {
            isLike = true;
        }
        return isLike;
    }

    @Override
    public IndexVlogVO getVlogDetailById(String userId, String vlogId) {

        Map<String, Object> map = new HashMap<>();
        map.put("vlogId", vlogId);

        List<IndexVlogVO> list = vlogMapperCustom.getVlogDetailById(map);

        if (list != null && list.size() > 0 && !list.isEmpty()) {
            IndexVlogVO vlogVO = list.get(0);
            return setterVO(vlogVO, userId);
        }

        return null;
    }

    @Transactional
    @Override
    public void changeToPrivateOrPublic(String userId,
                                        String vlogId,
                                        Integer yesOrNo) {
        Example example = new Example(Vlog.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id", vlogId);
        criteria.andEqualTo("vlogerId", userId);

        Vlog pendingVlog = new Vlog();
        pendingVlog.setIsPrivate(yesOrNo);

        vlogMapper.updateByExampleSelective(pendingVlog, example);
    }

    @Override
    public PagedGridResult queryMyVlogList(String userId,
                                           Integer page,
                                           Integer pageSize,
                                           Integer yesOrNo) {

        Example example = new Example(Vlog.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("vlogerId", userId);
        criteria.andEqualTo("isPrivate", yesOrNo);

        PageHelper.startPage(page, pageSize);
        List<Vlog> list = vlogMapper.selectByExample(example);

        return setterPagedGrid(list, page);
    }

    @Transactional
    @Override
    public void userLikeVlog(String userId, String vlogId) {

        String rid = sid.nextShort();

        MyLikedVlog likedVlog = new MyLikedVlog();
        likedVlog.setId(rid);
        likedVlog.setVlogId(vlogId);
        likedVlog.setUserId(userId);


        myLikedVlogMapper.insert(likedVlog);


//        // 系统消息：点赞短视频
        Vlog vlog = this.getVlog(vlogId);
        Map msgContent = new HashMap();
        msgContent.put("vlogId", vlogId);
        msgContent.put("vlogCover", vlog.getCover());
//        msgService.createMsg(userId,
//                            vlog.getVlogerId(),
//                            MessageEnum.LIKE_VLOG.type,
//                            msgContent);
        // MQ异步解耦
        MessageMO messageMO = new MessageMO();
        messageMO.setFromUserId(userId);
        messageMO.setToUserId(vlog.getVlogerId());
        messageMO.setMsgContent(msgContent);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_MSG,
                "sys.msg." + MessageEnum.LIKE_VLOG.enValue,
                JsonUtils.objectToJson(messageMO));
    }

    @Override
    public Vlog getVlog(String id) {
        return vlogMapper.selectByPrimaryKey(id);
    }

    @Transactional
    @Override
    public void userUnLikeVlog(String userId, String vlogId) {

        MyLikedVlog likedVlog = new MyLikedVlog();
        likedVlog.setVlogId(vlogId);
        likedVlog.setUserId(userId);

        myLikedVlogMapper.delete(likedVlog);
    }

    @Override
    public PagedGridResult getMyLikedVlogList(String userId,
                                              Integer page,
                                              Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        List<IndexVlogVO> list = vlogMapperCustom.getMyLikedVlogList(map);

        return setterPagedGrid(list, page);
    }

    @Override
    public PagedGridResult getMyFollowVlogList(String myId,
                                               Integer page,
                                               Integer pageSize) {
        PageHelper.startPage(page, pageSize);

        Map<String, Object> map = new HashMap<>();
        map.put("myId", myId);

        List<IndexVlogVO> list = vlogMapperCustom.getMyFollowVlogList(map);

        for (IndexVlogVO v : list) {
            String vlogerId = v.getVlogerId();
            String vlogId = v.getVlogId();

            if (StringUtils.isNotBlank(myId)) {
                // 用户必定关注该博主
                v.setDoIFollowVloger(true);

                // 判断当前用户是否点赞过视频
                v.setDoILikeThisVlog(doILikeVlog(myId, vlogId));
            }

            // 获得当前视频被点赞过的总数
            v.setLikeCounts(getVlogBeLikedCounts(vlogId));
        }

        return setterPagedGrid(list, page);
    }

    @Override
    public PagedGridResult getMyFriendVlogList(String myId,
                                               Integer page,
                                               Integer pageSize) {

        PageHelper.startPage(page, pageSize);

        Map<String, Object> map = new HashMap<>();
        map.put("myId", myId);

        List<IndexVlogVO> list = vlogMapperCustom.getMyFriendVlogList(map);

        for (IndexVlogVO v : list) {
            String vlogerId = v.getVlogerId();
            String vlogId = v.getVlogId();

            if (StringUtils.isNotBlank(myId)) {
                // 用户必定关注该博主
                v.setDoIFollowVloger(true);

                // 判断当前用户是否点赞过视频
                v.setDoILikeThisVlog(doILikeVlog(myId, vlogId));
            }

            // 获得当前视频被点赞过的总数
            v.setLikeCounts(getVlogBeLikedCounts(vlogId));
        }

        return setterPagedGrid(list, page);
    }
    @Transactional
    @Override
    public void flushCounts(String vlogId, Integer counts) {

        Vlog vlog = new Vlog();
        vlog.setId(vlogId);
        vlog.setLikeCounts(counts);

        vlogMapper.updateByPrimaryKeySelective(vlog);
        redis.set(REDIS_VLOG_OLD_LIKED_COUNTS+":"+vlogId, String.valueOf(counts));

    }
}
