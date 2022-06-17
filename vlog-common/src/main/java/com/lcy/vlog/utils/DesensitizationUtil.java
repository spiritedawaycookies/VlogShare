package com.lcy.vlog.utils;

/**
 * 通用脱敏工具类
 * 可用于：
 *      用户名
 *      手机号
 *      邮箱
 *      地址等
 */
public class DesensitizationUtil {

    private static final int SIZE = 6;
    private static final String SYMBOL = "*";

    public static void main(String[] args) {
        String name = commonDisplay("匿名");
        String mobile = commonDisplay("13900000000");
        String mail = commonDisplay("user@lcy.com");
        String address = commonDisplay("123, 456 Road, Vancouver");

        System.out.println(name);
        System.out.println(mobile);
        System.out.println(mail);
        System.out.println(address);
    }

    /**
     * 通用脱敏方法,加*号
     * @param value
     * @return
     */
    public static String commonDisplay(String value) {

        if (null == value || "".equals(value)) {
            return value;
        }
        int len = value.length();
        int paramOne = len / 2;
        int paramTwo = paramOne - 1;
        int paramThree = len % 2;
        StringBuilder stringBuilder = new StringBuilder();
        if (len <= 2) {
            if (paramThree == 1) {
                return SYMBOL;
            }
            stringBuilder.append(SYMBOL);
            stringBuilder.append(value.charAt(len - 1));
        } else {
            if (paramTwo <= 0) {
                stringBuilder.append(value.substring(0, 1));
                stringBuilder.append(SYMBOL);
                stringBuilder.append(value.substring(len - 1, len));

            } else if (paramTwo >= SIZE / 2 && SIZE + 1 != len) {
                int paramFive = (len - SIZE) / 2;
                stringBuilder.append(value.substring(0, 1));
                for (int i = 0; i < SIZE; i++) {
                    stringBuilder.append(SYMBOL);
                }
                if ((paramThree == 0 && SIZE / 2 == 0) || (paramThree != 0 && SIZE % 2 != 0)) {
                    stringBuilder.append(value.substring(len - paramFive, len));
                } else {
                    stringBuilder.append(value.substring(len - (paramFive + 1), len));
                }
            } else {
                int paramFour = len - 2;
                stringBuilder.append(value.substring(0, 1));
                for (int i = 0; i < paramFour; i++) {
                    stringBuilder.append(SYMBOL);
                }
                stringBuilder.append(value.substring(len - 1, len));
            }
        }
        return stringBuilder.toString();
    }

}
