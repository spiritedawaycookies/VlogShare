package com.lcy.vlog.mapper;

import com.lcy.vlog.my.mapper.MyMapper;
import com.lcy.vlog.pojo.Fans;
import com.lcy.vlog.vo.FansVO;
import com.lcy.vlog.vo.VlogerVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface FansMapperCustom extends MyMapper<Fans> {

    public List<VlogerVO> queryMyFollows(@Param("paramMap") Map<String, Object> map);

    public List<FansVO> queryMyFans(@Param("paramMap") Map<String, Object> map);

}