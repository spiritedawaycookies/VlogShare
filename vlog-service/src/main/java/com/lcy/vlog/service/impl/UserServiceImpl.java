package com.lcy.vlog.service.impl;

import com.lcy.vlog.bo.UpdatedUserBO;
import com.lcy.vlog.enums.Sex;
import com.lcy.vlog.enums.UserInfoModifyType;
import com.lcy.vlog.enums.YesOrNo;
import com.lcy.vlog.exceptions.GracefulException;
import com.lcy.vlog.graceful.result.ResponseStatusEnum;
import com.lcy.vlog.mapper.UsersMapper;
import com.lcy.vlog.pojo.Users;
import com.lcy.vlog.service.UserService;
import com.lcy.vlog.utils.DateUtil;
import com.lcy.vlog.utils.DesensitizationUtil;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.Objects;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private Sid sid; //分库分表不能使用自增id,需要第三方工具生成全局唯一id
    private static final String USER_FACE1 = "http://192.168.3.84:8099/static/avatar/defaultAvatar.png";

    @Override
    public Users queryMobileIsExist(String mobile) {
        Example userExample = new Example(Users.class);
        Example.Criteria criteria = userExample.createCriteria();
        criteria.andEqualTo("mobile", mobile);
        Users user = usersMapper.selectOneByExample(userExample);
        return user;
    }

    @Transactional
    @Override
    public Users createUser(String mobile) {

        // 获得全局唯一主键
        String userId = sid.nextShort();

        Users user = new Users();
        user.setId(userId);

        user.setMobile(mobile);
        user.setNickname(DesensitizationUtil.commonDisplay(mobile));
        user.setuserNum(DesensitizationUtil.commonDisplay(mobile));
        user.setFace(USER_FACE1);

        user.setBirthday(DateUtil.stringToDate("1900-01-01"));
        user.setSex(Sex.secret.type);

        user.setCountry("中国");
        user.setProvince("");
        user.setCity("");
        user.setDistrict("");
        user.setDescription("This guy is lazy and left nothing~");
        user.setCanUserNumBeUpdated(YesOrNo.YES.type);

        user.setCreatedTime(new Date());
        user.setUpdatedTime(new Date());

        usersMapper.insert(user);

        return user;
    }

    @Override
    public Users getUser(String userId) {
        return usersMapper.selectByPrimaryKey(userId);
    }

    @Transactional
    @Override
    public Users updateUserInfo(UpdatedUserBO updatedUserBO) {

        Users pendingUser = new Users();
        BeanUtils.copyProperties(updatedUserBO, pendingUser);

        int result = usersMapper.updateByPrimaryKeySelective(pendingUser);
        if (result != 1) {
            GracefulException.display(ResponseStatusEnum.USER_UPDATE_ERROR);
        }

        return getUser(updatedUserBO.getId());
    }

    @Transactional
    @Override
    public Users updateUserInfo(UpdatedUserBO updatedUserBO, Integer type) {
        Example example = new Example(Users.class);
        Example.Criteria criteria = example.createCriteria();
        if (Objects.equals(type, UserInfoModifyType.NICKNAME.type)) {
            criteria.andEqualTo("nickname", updatedUserBO.getNickname());
            Users user = usersMapper.selectOneByExample(example);
            if (user != null) {
                GracefulException.display(ResponseStatusEnum.USER_INFO_UPDATED_NICKNAME_EXIST_ERROR);
            }
        }

        if (Objects.equals(type, UserInfoModifyType.USERNUM.type)) {
            criteria.andEqualTo("userNum", updatedUserBO.getUserNum());
            Users user = usersMapper.selectOneByExample(example);
            if (user != null) {
                GracefulException.display(ResponseStatusEnum.USER_INFO_UPDATED_USERNUM_EXIST_ERROR);
            }

            Users tempUser = getUser(updatedUserBO.getId());
            if (Objects.equals(tempUser.getCanUserNumBeUpdated(), YesOrNo.NO.type)) {
                GracefulException.display(ResponseStatusEnum.USER_INFO_CANT_UPDATED_USERNUM_ERROR);
            }

            updatedUserBO.setCanUserNumBeUpdated(YesOrNo.NO.type);
        }

        return updateUserInfo(updatedUserBO);
    }
}
