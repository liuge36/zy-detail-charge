package com.csylh.blog.how2s0525;

/**
 * Description: 抽象类 实现 接口
 * 创建实现了 Programmer 接口的抽象装饰类
 *
 * @Author: 留歌36
 * @Date: 2020/5/25 10:48
 */
public abstract class ProgrammerDecorate implements Programmer {
    // 以 Programmer 接口作为 本抽象类 构造方法 的参数
    protected Programmer programmer;

    // 构造方法
    public ProgrammerDecorate(Programmer decorateProgrammer){
        this.programmer = decorateProgrammer;
    }

    public void coding() {
        programmer.coding();
    }
}
