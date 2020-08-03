package com.csylh.blog.how2s0521;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Description: 动态代理
 *
 * 代理对象的生成，是利用JDK API，动态地在内存中构建
 * 代理对象(需要我们指定创建 代理对象/目标对象 实现的接口的类型)，
 * 并且会默认实现接口的全部方法
 *
 * @Author: 留歌36
 * @Date: 2020/5/21 11:50
 */
public class ProxyHandler implements InvocationHandler {

    private Object tar;

    // 绑定委托对象，并返回代理类
    public Object bind(Object tar)
    {
        this.tar = tar;

        /**
         * 参数一：生成代理对象使用哪个类装载器【一般我们使用的是被代理类的装载器】
         *
         * 参数二：生成哪个对象的代理对象，通过接口指定【指定要被代理类的接口】
         *
         * 参数三：生成的代理对象的方法里干什么事【实现handler接口，我们想怎么实现就怎么实现】
         */
        // 代理对象拥有目标对象相同的方法【因为参数二指定了对象的接口，代理对象会实现接口的所有方法】
        return Proxy.newProxyInstance(

                tar.getClass().getClassLoader(),

                tar.getClass().getInterfaces(),

                this // 这里的this,即上面定义的Object,代表实现的所有方法 交由Object 的 invoke() 反射方法进行处理
        );
    }

    // 用户调用代理对象的什么方法，都是在调用处理器的invoke方法
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Object result = null;

        //这里就可以进行所谓的AOP编程了，不管你调用 生成对象的什么方法，invoke都是会触发，这里就可以做一些前置或后置的方法
        // increaseBefore() 方法

        //在调用具体函数方法前，执行功能处理【这里就是调用生成对象的 具体方法】
        result = method.invoke(tar,args);

        // increaseAfter() 方法

        //在调用具体函数方法后，执行功能处理
        return result;

    }
}
