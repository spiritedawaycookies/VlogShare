package com.lcy.vlog.controller;

import com.lcy.vlog.base.BaseInfoProperties;
import com.lcy.vlog.bo.VlogBO;
import com.lcy.vlog.enums.YesOrNo;
import com.lcy.vlog.graceful.result.GracefulJSONResult;
import com.lcy.vlog.graceful.result.ResponseStatusEnum;
import com.lcy.vlog.service.VlogService;
import com.lcy.vlog.utils.PagedGridResult;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@Api(tags = "VlogController 短视频相关业务功能的接口")
@RequestMapping("vlog")
@RestController
public class VlogController extends BaseInfoProperties {

    @Autowired
    private VlogService vlogService;

    @PostMapping("publish")
    public GracefulJSONResult publish(@Valid @RequestBody VlogBO vlogBO) {
        vlogService.createVlog(vlogBO);
        return GracefulJSONResult.ok();
    }

    @GetMapping("indexList")
    public GracefulJSONResult indexList(@RequestParam(defaultValue = "") String userId,//default是为了可选 否则必填
                                        @RequestParam(defaultValue = "") String search,
                                        @RequestParam Integer page,
                                        @RequestParam Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult gridResult = vlogService.getIndexVlogList(userId, search, page, pageSize);
        return GracefulJSONResult.ok(gridResult);
    }

    @GetMapping("detail")
    public GracefulJSONResult detail(@RequestParam(defaultValue = "") String userId,
                                     @RequestParam String vlogId) {
        return GracefulJSONResult.ok(vlogService.getVlogDetailById(userId, vlogId));
    }


    @PostMapping("changeToPrivate")
    public GracefulJSONResult changeToPrivate(@RequestParam String userId,
                                              @RequestParam String vlogId) {
        vlogService.changeToPrivateOrPublic(userId,
                vlogId,
                YesOrNo.YES.type);
        return GracefulJSONResult.ok();
    }

    @PostMapping("changeToPublic")
    public GracefulJSONResult changeToPublic(@RequestParam String userId,
                                             @RequestParam String vlogId) {
        vlogService.changeToPrivateOrPublic(userId,
                vlogId,
                YesOrNo.NO.type);
        return GracefulJSONResult.ok();
    }


    @GetMapping("myPublicList")
    public GracefulJSONResult myPublicList(@RequestParam String userId,
                                           @RequestParam Integer page,
                                           @RequestParam Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult gridResult = vlogService.queryMyVlogList(userId,
                page,
                pageSize,
                YesOrNo.NO.type);
        return GracefulJSONResult.ok(gridResult);
    }

    @GetMapping("myPrivateList")
    public GracefulJSONResult myPrivateList(@RequestParam String userId,
                                            @RequestParam Integer page,
                                            @RequestParam Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult gridResult = vlogService.queryMyVlogList(userId,
                page,
                pageSize,
                YesOrNo.YES.type);
        return GracefulJSONResult.ok(gridResult);
    }

    @GetMapping("myLikedList")
    public GracefulJSONResult myLikedList(@RequestParam String userId,
                                          @RequestParam Integer page,
                                          @RequestParam Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult gridResult = vlogService.getMyLikedVlogList(userId,
                page,
                pageSize);
        return GracefulJSONResult.ok(gridResult);
    }


    @PostMapping("like")
    public GracefulJSONResult like(@RequestParam String userId,
                                   @RequestParam String vlogerId,
                                   @RequestParam String vlogId) {
        if (userId == null) return GracefulJSONResult.errorCustom(ResponseStatusEnum.USER_STATUS_ERROR);
        if (vlogerId == null) return GracefulJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        if (vlogId == null) return GracefulJSONResult.errorCustom(ResponseStatusEnum.FILE_NOT_EXIST_ERROR);

        // 我点赞的视频，关联关系保存到数据库
        vlogService.userLikeVlog(userId, vlogId);

        // 点赞后，视频和视频发布者的获赞都会 +1
        redis.increment(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + vlogerId, 1);
        redis.increment(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId, 1);

        // 我点赞的视频，需要在redis中保存关联关系
        redis.set(REDIS_USER_LIKE_VLOG + ":" + userId + ":" + vlogId, "1");

        // 点赞完毕，获得当前在redis中的总数
        // 比如获得总计数为 1k/1w/10w，假定阈值（配置）为2000
        // 此时1k满足2000，则触发入库 (弱一致性)

        String countsStr = redis.get(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId);
        log.info("======" + REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId + "======");
        Integer counts = 0;
//        if (StringUtils.isNotBlank(countsStr)) {
//            counts = Integer.valueOf(countsStr);
//            if (counts >= nacosCounts) {
//                vlogService.flushCounts(vlogId, counts);
//            }
//        }
//

        return GracefulJSONResult.ok();
    }


    @PostMapping("unlike")
    public GracefulJSONResult unlike(@RequestParam String userId,
                                     @RequestParam String vlogerId,
                                     @RequestParam String vlogId) {

        // 我取消点赞的视频，关联关系删除
        vlogService.userUnLikeVlog(userId, vlogId);

        redis.decrement(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + vlogerId, 1);
        redis.decrement(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId, 1);
        redis.del(REDIS_USER_LIKE_VLOG + ":" + userId + ":" + vlogId);

        return GracefulJSONResult.ok();
    }

    @PostMapping("totalLikedCounts")
    public GracefulJSONResult totalLikedCounts(@RequestParam String vlogId) {
        return GracefulJSONResult.ok(vlogService.getVlogBeLikedCounts(vlogId));
    }

    @GetMapping("followList")
    public GracefulJSONResult followList(@RequestParam String myId,
                                         @RequestParam Integer page,
                                         @RequestParam Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult gridResult = vlogService.getMyFollowVlogList(myId,
                page,
                pageSize);
        return GracefulJSONResult.ok(gridResult);
    }

    @GetMapping("friendList")
    public GracefulJSONResult friendList(@RequestParam String myId,
                                         @RequestParam Integer page,
                                         @RequestParam Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult gridResult = vlogService.getMyFriendVlogList(myId,
                page,
                pageSize);
        return GracefulJSONResult.ok(gridResult);
    }
}
