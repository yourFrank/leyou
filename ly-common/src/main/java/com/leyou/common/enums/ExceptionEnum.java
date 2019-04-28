package com.leyou.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 自定义枚举类型，规范异常code和msg,防止乱写
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum  ExceptionEnum {
    CATEGORY_DELETE_ERROR(500,"商品删除失败"),
    CATEGORY_NOT_FOUND(404,"商品分类没有查到"),
    BRANDS_NOT_FOUND(404,"品牌不存在"),
    BRAND_SAVE_ERROR(500,"品牌新增失败"),
    INVALID_FILE_TYPE(500,"无效的文件类型"),
    SPECGROUP_NOT_FOUND(404,"规格组找不到"),
    UPLOAD_FILE_ERROR(500,"无效的文件类型"),
    SPECPARAM_NOT_FOUND(404,"规格参数没找到"),
    GOODS_NOT_FOUND(404,"商品没有找到"),
    GOODS_SAVE_ERROR(500,"商品保存失败"),
    GOODS_DELETE_ERROR(500,"商品删除失败"),
    TEMPLATE_CREATED_ERROR(500,"页面创建失败"),
    INVALID_USER_TYPE_ERROR(400,"无效的用户类型"),
    INVALID_PHONE_CODE_ERROR(500,"手机验证码无效"),
    USERNAME_PASSWORD_NOT_MATCH_ERROR(500,"用户名密码不匹配"),
    USER_TOKEN_CREATED_ERROR(500,"用户token生成失败"),
    UNAUTHORIZED(403,"用户token失效")
    ;
    private int code;
    private String msg;
}
