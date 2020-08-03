package com.csylh.blog.how2s0525;

/**
 * Description: 这里就实现了类功能的 增强
 *              将以前的类作为参数 传递进去
 *
 * @Author: 留歌36
 * @Date: 2020/5/25 10:54
 */
public class BeforeLiuge36Dec extends ProgrammerDecorate{

    /**
     * 我感觉这里也算核心之一，其实后面传递进来的对象，都是这个接口 实现类的 实例
     * @param programmer
     */
    public BeforeLiuge36Dec(Programmer programmer) {
        super(programmer);
    }

    /**
     * 重写coding方法
     */
    @Override
    public void coding() {

        draw();

        super.coding();
    }


    /**
     * 进行功能的增强
     */
    public void draw(){
        System.out.println("留歌在这之前呢，其实也是画画的？");
    }
}
