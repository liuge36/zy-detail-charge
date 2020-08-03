package com.csylh.blog.how2s0525;

/**
 * Description:
 *
 * @Author: 留歌36
 * @Date: 2020/5/25 11:02
 */
public class TestDecorator {
    public static void main(String[] args) {
        // 单调的 Liuge36
        Programmer liuge36 = new Liuge36();
        liuge36.coding();

        System.out.println("================写代码之前的留歌的状态=======================");

        // 装饰过得 Liuge36
        liuge36 = new BeforeLiuge36Dec(liuge36);
        liuge36.coding();

        System.out.println("================写代码之后的留歌的状态=======================");

        // 装饰过得 Liuge36
        liuge36 = new AfterLiuge36Dec(liuge36);
        liuge36.coding();

    }
}
