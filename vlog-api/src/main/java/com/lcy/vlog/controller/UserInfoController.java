package com.lcy.vlog.controller;

import com.lcy.vlog.config.MinIOConfig;
import com.lcy.vlog.base.BaseInfoProperties;
import com.lcy.vlog.bo.UpdatedUserBO;
import com.lcy.vlog.enums.FileTypeEnum;
import com.lcy.vlog.enums.UserInfoModifyType;
import com.lcy.vlog.graceful.result.GracefulJSONResult;
import com.lcy.vlog.graceful.result.ResponseStatusEnum;
import com.lcy.vlog.model.Stu;
import com.lcy.vlog.pojo.Users;
import com.lcy.vlog.service.UserService;
import com.lcy.vlog.utils.MinIOUtils;
import com.lcy.vlog.utils.SMSUtils;
import com.lcy.vlog.vo.UsersVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Slf4j
@Api(tags = "UserInfoController 用户信息接口模块")
@RequestMapping("userInfo")
@RestController
public class UserInfoController extends BaseInfoProperties {

    @Autowired
    private UserService userService;

    /**
     * 用数据库计算粉丝数量很慢,会崩,用redis可以,而且是单线程, 线程安全
     *
     * @param userId
     * @return
     * @throws Exception
     */
    @GetMapping("query")
    public GracefulJSONResult query(@RequestParam String userId) throws Exception {

        Users user = userService.getUser(userId);

        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(user, usersVO);
        usersVO.setCanUserNumBeUpdated(user.getCanUserNumBeUpdated());

        // 我的关注博主总数量
        String myFollowsCountsStr = redis.get(REDIS_MY_FOLLOWS_COUNTS + ":" + userId);
        // 我的粉丝总数
        String myFansCountsStr = redis.get(REDIS_MY_FANS_COUNTS + ":" + userId);
        // 用户获赞总数，视频博主（点赞/喜欢）总和
        String likedVloggerCountsStr = redis.get(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + userId);

        Integer myFollowsCounts = 0;
        Integer myFansCounts = 0;
        Integer likedVloggerCounts = 0;
        Integer totalLikeMeCounts = 0;

        if (StringUtils.isNotBlank(myFollowsCountsStr)) {
            myFollowsCounts = Integer.valueOf(myFollowsCountsStr);
        }
        if (StringUtils.isNotBlank(myFansCountsStr)) {
            myFansCounts = Integer.valueOf(myFansCountsStr);
        }

        if (StringUtils.isNotBlank(likedVloggerCountsStr)) {
            likedVloggerCounts = Integer.valueOf(likedVloggerCountsStr);
        }
        totalLikeMeCounts =  likedVloggerCounts;

        usersVO.setMyFollowsCounts(myFollowsCounts);
        usersVO.setMyFansCounts(myFansCounts);
        usersVO.setTotalLikeMeCounts(totalLikeMeCounts);

        return GracefulJSONResult.ok(usersVO);
    }

    @PostMapping("modifyUserInfo")
    public GracefulJSONResult modifyUserInfo(@RequestBody UpdatedUserBO updatedUserBO,
                                             @RequestParam Integer type)
            throws Exception {
//查看修改的是什么信息
        UserInfoModifyType.checkUserInfoTypeIsRight(type);

        Users newUserInfo = userService.updateUserInfo(updatedUserBO, type);

        return GracefulJSONResult.ok(newUserInfo);
    }

    @Autowired
    private MinIOConfig minIOConfig;

    @PostMapping("modifyImage")
    public GracefulJSONResult modifyImage(@RequestParam String userId,
                                          @RequestParam Integer type,
                                          MultipartFile file) throws Exception {

        if (!Objects.equals(type, FileTypeEnum.BGIMG.type) && !Objects.equals(type, FileTypeEnum.FACE.type)) {
            return GracefulJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }

        String fileName = file.getOriginalFilename();

        MinIOUtils.uploadFile(minIOConfig.getBucketName(),
                fileName,
                file.getInputStream());

        String imgUrl = minIOConfig.getFileHost()
                + "/"
                + minIOConfig.getBucketName()
                + "/"
                + fileName;


        // 修改图片地址到数据库
        UpdatedUserBO updatedUserBO = new UpdatedUserBO();
        updatedUserBO.setId(userId);

        if (Objects.equals(type, FileTypeEnum.BGIMG.type)) {
            updatedUserBO.setBgImg(imgUrl);
        } else {
            updatedUserBO.setFace(imgUrl);
        }
        Users users = userService.updateUserInfo(updatedUserBO);

        return GracefulJSONResult.ok(users);
    }
}
