package com.lcy.vlog.mapper;

import com.lcy.vlog.my.mapper.MyMapper;
import com.lcy.vlog.pojo.Comment;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentMapper extends MyMapper<Comment> {
}