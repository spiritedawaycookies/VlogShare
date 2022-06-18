package com.lcy.vlog.controller;

import com.lcy.vlog.config.MinIOConfig;
import com.lcy.vlog.graceful.result.GracefulJSONResult;
import com.lcy.vlog.model.Stu;
import com.lcy.vlog.utils.MinIOUtils;
import com.lcy.vlog.utils.SMSUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Api(tags = "FileController 文件上传测试的接口")
@RestController
public class FileController {

    @Autowired
    private MinIOConfig minIOConfig;

//    @PostMapping("upload")
//    public GracefulJSONResult upload(MultipartFile file) throws Exception {
//
//        String fileName = file.getOriginalFilename();
//
//        MinIOUtils.uploadFile(minIOConfig.getBucketName(),
//                              fileName,
//                              file.getInputStream());
//
//        String imgUrl = minIOConfig.getFileHost()
//                        + "/"
//                        + minIOConfig.getBucketName()
//                        + "/"
//                        + fileName;
//
//        return GracefulJSONResult.ok(imgUrl);
//    }
}
