package com.csylh.blog.how2s0521;

/**
 * Description:
 *
 * @Author: 留歌36
 * @Date: 2020/5/21 11:46
 */
public class TestProxy {
    public static void main(String[] args) {

        /**
         * Description: 静态代理
         *
         * 会觉得SubjectProxy 这个类定义出来纯属多余
         * 直接实例化实现类完成操作不就结了吗？
         * 后来随着业务庞大，你就会知道，实现proxy类对真实类的封装对于粒度的控制有着重要的意义。
         *
         * 但是静态代理这个模式本身有个大问题，如果类方法数量越来越多的时候，
         * 代理类的代码量是十分庞大的。所以引入动态代理来解决此类问题。
         *
         *  缺点：如果目标对象的接口有很多方法的话，那我们还是得一一实现，这样就会比较麻烦
         *
         */
//        Subject subject = new SubjectProxy(new RealSubject()); // 静态代理
////        Subject subject = new SubjectProxy(); // 透明代理
//        subject.doSomeThing();

        /**
         *
         * 动态代理的作用是什么:
         * Proxy类的代码量被固定下来，不会因为业务的逐渐庞大而庞大；
         * 可以实现AOP编程，实际上静态代理也可以实现，总的来说，AOP可以算作是代理模式的一个典型应用；
         * 解耦，通过参数就可以判断真实类，不需要事先实例化，更加灵活多变。
         *
         *
         */
        ProxyHandler proxy = new ProxyHandler();
        // 绑定该类实现的所有接口
        Subject subject = (Subject) proxy.bind(new RealSubject());
        subject.doSomeThing();




    }
}
