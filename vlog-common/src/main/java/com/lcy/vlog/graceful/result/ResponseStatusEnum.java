package com.lcy.vlog.graceful.result;

/**
 * 响应结果枚举，用于提供给GraceJSONResult返回给前端的
 * 本枚举类中包含了很多的不同的状态码供使用，可以自定义
 * 便于更优雅的对状态码进行管理，一目了然
 */
public enum ResponseStatusEnum {

    SUCCESS(200, true, "Success"),
    FAILED(500, false, "Failed"),

    // 50x
    UN_LOGIN(501, false, "Please login"),
    TICKET_INVALID(502, false, "Session expired, please login again"),
    NO_AUTH(503, false, "No privilege"),
    MOBILE_ERROR(504, false, "SMS not sent, please try again"),
    SMS_NEED_WAIT_ERROR(505, false, "SMS sent too frequent, please try again"),
    SMS_CODE_ERROR(506, false, "Wrong SMS code"),
    USER_FROZEN(507, false, "Account frozen, please contact admin"),
    USER_UPDATE_ERROR(508, false, "User info update failed, please contact admin"),
    USER_INACTIVE_ERROR(509, false, "Plase go to [账号设置], modify your info and activate"),
    USER_INFO_UPDATED_ERROR(5091, false, "Info update failed"),
    USER_INFO_UPDATED_NICKNAME_EXIST_ERROR(5092, false, "Nickname exists"),
    USER_INFO_UPDATED_USERNUM_EXIST_ERROR(5092, false, "Account name exists"),
    USER_INFO_CANT_UPDATED_USERNUM_ERROR(5092, false, "Cannot update account name"),
    FILE_UPLOAD_NULL_ERROR(510, false, "File cannot be null"),
    FILE_UPLOAD_FAILD(511, false, "File upload failed"),
    FILE_FORMATTER_FAILD(512, false, "File format not supported"),
    FILE_MAX_SIZE_500KB_ERROR(5131, false, "Only allow file up to 500kb"),
    FILE_MAX_SIZE_2MB_ERROR(5132, false, "Only allow file up to 2MB"),
    FILE_NOT_EXIST_ERROR(514, false, "Requested file not exists"),
    USER_STATUS_ERROR(515, false, "User status error"),
    USER_NOT_EXIST_ERROR(516, false, "User doesn't exist"),

    // 自定义系统级别异常 54x
    SYSTEM_INDEX_OUT_OF_BOUNDS(541, false, "System index out of bounds"),
    SYSTEM_ARITHMETIC_BY_ZERO(542, false, "System error. Cannot divide by zero"),
    SYSTEM_NULL_POINTER(543, false, "Null pointer exception"),
    SYSTEM_NUMBER_FORMAT(544, false, "Number format exception"),
    SYSTEM_PARSE(545, false, "Parsing exception"),
    SYSTEM_IO(546, false, "IO exception"),
    SYSTEM_FILE_NOT_FOUND(547, false, "File not found exception"),
    SYSTEM_CLASS_CAST(548, false, "Class cast exception"),
    SYSTEM_PARSER_ERROR(549, false, "Parser exception"),
    SYSTEM_DATE_PARSER_ERROR(550, false, "Datetime parsing exception"),

    // admin 管理系统 56x
    ADMIN_USERNAME_NULL_ERROR(561, false, "Admin username cannot be null"),
    ADMIN_USERNAME_EXIST_ERROR(562, false, "Admin username exists"),
    ADMIN_NAME_NULL_ERROR(563, false, "Admin name cannot be null"),
    ADMIN_PASSWORD_ERROR(564, false, "Password not correspond"),
    ADMIN_CREATE_ERROR(565, false, "Create admin failed"),
    ADMIN_PASSWORD_NULL_ERROR(566, false, "Password cannot be null"),
    ADMIN_NOT_EXIT_ERROR(567, false, "Admin not exists or wrong password"),
    ADMIN_FACE_NULL_ERROR(568, false, "Face info cannot be null"),
    ADMIN_FACE_LOGIN_ERROR(569, false, "Face login failed"),
    CATEGORY_EXIST_ERROR(570, false, "Category exists"),

    // 媒体中心 相关错误 58x
    ARTICLE_COVER_NOT_EXIST_ERROR(580, false, "Cover not exists"),
    ARTICLE_CATEGORY_NOT_EXIST_ERROR(581, false, "Category not exists"),
    ARTICLE_CREATE_ERROR(582, false, "Create item failed, please contact admin"),
    ARTICLE_QUERY_PARAMS_ERROR(583, false, "Item query param exception"),
    ARTICLE_DELETE_ERROR(584, false, "Delete item failed"),
    ARTICLE_WITHDRAW_ERROR(585, false, "Withdraw item failed"),
    ARTICLE_REVIEW_ERROR(585, false, "Review item failed"),
    ARTICLE_ALREADY_READ_ERROR(586, false, "Already read"),

    // 人脸识别错误代码
    FACE_VERIFY_TYPE_ERROR(600, false, "Face verification wrong type"),
    FACE_VERIFY_LOGIN_ERROR(601, false, "Cannot face login"),

    // 系统错误，未预期的错误 555
    SYSTEM_ERROR(555, false, "System busy"),
    SYSTEM_OPERATION_ERROR(556, false, "Operation failed, retry or contact admin"),
    SYSTEM_RESPONSE_NO_INFO(557, false, ""),
    SYSTEM_ERROR_GLOBAL(558, false, "Global context:System busy, please try again"),
    SYSTEM_ERROR_FEIGN(559, false, "Client Feign:System busy, please try again"),
    SYSTEM_ERROR_ZUUL(560, false, "Request:System busy, please try again");


    // 响应业务状态
    private Integer status;
    // 调用是否成功
    private Boolean success;
    // 响应消息，可以为成功或者失败的消息
    private String msg;

    ResponseStatusEnum(Integer status, Boolean success, String msg) {
        this.status = status;
        this.success = success;
        this.msg = msg;
    }

    public Integer status() {
        return status;
    }

    public Boolean success() {
        return success;
    }

    public String msg() {
        return msg;
    }
}
