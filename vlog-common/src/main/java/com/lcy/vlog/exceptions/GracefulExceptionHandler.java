package com.lcy.vlog.exceptions;

import com.lcy.vlog.graceful.result.GracefulJSONResult;
import com.lcy.vlog.graceful.result.ResponseStatusEnum;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一异常拦截处理
 * 面向切面
 * 可以针对异常的类型进行捕获，然后返回json信息到前端
 */
@ControllerAdvice
public class GracefulExceptionHandler {

    @ExceptionHandler(MyCustomException.class)
    @ResponseBody
    public GracefulJSONResult returnMyException(MyCustomException e) {
        e.printStackTrace();
        return GracefulJSONResult.exception(e.getResponseStatusEnum());
    }

    /**
     * 参数校验错误
     * @param e
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public GracefulJSONResult returnMethodArgumentNotValid(MethodArgumentNotValidException e) {
        BindingResult result = e.getBindingResult();
        Map<String, String> map = getErrors(result);
        return GracefulJSONResult.errorMap(map);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseBody
    public GracefulJSONResult returnMaxUploadSize(MaxUploadSizeExceededException e) {
//        e.printStackTrace();
        return GracefulJSONResult.errorCustom(ResponseStatusEnum.FILE_MAX_SIZE_2MB_ERROR);
    }

    public Map<String, String> getErrors(BindingResult result) {
        Map<String, String> map = new HashMap<>();
        List<FieldError> errorList = result.getFieldErrors();
        for (FieldError fieldError : errorList) {
            // 错误所对应的属性字段名
            String field = fieldError.getField();
            // 错误的信息
            String msg = fieldError.getDefaultMessage();
            map.put(field, msg);
        }
        return map;
    }
}
