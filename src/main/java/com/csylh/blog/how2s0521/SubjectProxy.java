package com.csylh.blog.how2s0521;

/**
 * Description: 静态代理
 *
 * 会觉得 SubjectProxy 这个类定义出来纯属多余
 * 直接实例化实现类完成操作不就结了吗？
 * 后来随着业务庞大，你就会知道，实现proxy类对真实类的封装对于粒度的控制有着重要的意义。
 *
 * 但是静态代理这个模式本身有个大问题，如果类方法数量越来越多的时候，
 * 代理类的代码量是十分庞大的。所以引入动态代理来解决此类问题。
 *
 * 缺点：如果目标对象的接口有很多方法的话，那我们还是得一一实现，这样就会比较麻烦
 *
 * @Author: 留歌36
 * @Date: 2020/5/21 11:44
 */
public class SubjectProxy implements Subject{

    // 真正的实现类
    private RealSubject realSubject;

    // 构造方法1
    public SubjectProxy(RealSubject realSubject) {
        this.realSubject = realSubject;
    }

    // // 构造方法2： 只做  RealSubject  的代理，且RealSubject对外界透明，也叫做透明代理
    public SubjectProxy() {
        this.realSubject = new RealSubject();
    }

    /**
     *  实现接口的方法
     */
    public void doSomeThing() {

        // 这个方法 之前之后 还可以做一些其他的事情，就实现了类功能的增强
        SubjectProxy.increaseBefore();

        // 通过这个静态代理就可以使用一句代码顶真正实现类 的功能实现
        realSubject.doSomeThing();


        increaseAfter();

    }


    // 1.在方法之前做增强
    private static void increaseBefore() {
        System.out.println("1.在方法之前做增强");
    }

    // 2.在方法之后做增强
    void increaseAfter() {
        System.out.println("2.在方法之后做增强");
    }


}
