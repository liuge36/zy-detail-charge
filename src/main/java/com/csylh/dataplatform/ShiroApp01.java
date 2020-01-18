package com.csylh.dataplatform;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.realm.SimpleAccountRealm;
import org.apache.shiro.realm.text.IniRealm;
import org.apache.shiro.subject.Subject;
import org.junit.Test;

/**
 * Description:
 *
 *
 * @Author: 留歌36
 * @Date: 2019/10/29 14:22
 */
public class ShiroApp01 {


    @Test
    public void test01(){
        // step1: 构建SecurityManager
        DefaultSecurityManager securityManager = new DefaultSecurityManager();

        // step4: 构建Realm
        SimpleAccountRealm realm = new SimpleAccountRealm();
        realm.addAccount("liuge","123456");
        securityManager.setRealm(realm);

        SecurityUtils.setSecurityManager(securityManager);

        // step2: 构建Subject
        Subject subject = SecurityUtils.getSubject();


        // step3: 登录
        AuthenticationToken token = new UsernamePasswordToken("liuge","123456");

        subject.login(token);

        System.out.println(subject.isAuthenticated());

        // 登出
        subject.logout();
        System.out.println(subject.isAuthenticated());
    }

    @Test
    public void test02(){
        // step1: 构建SecurityManager
        DefaultSecurityManager securityManager = new DefaultSecurityManager();
        // step4: 构建Realm
        SimpleAccountRealm realm = new SimpleAccountRealm();
        realm.addAccount("liuge","123456","admin","guest");

        securityManager.setRealm(realm);

        SecurityUtils.setSecurityManager(securityManager);
        // step2: 构建Subject
        Subject subject = SecurityUtils.getSubject();


        // step3: 登录
        AuthenticationToken token = new UsernamePasswordToken("liuge","123456");

        subject.login(token);

        System.out.println(subject.isAuthenticated());

        subject.checkRole("admin");
        subject.checkRole("guest");
        subject.checkRoles("admin","guest");
    }

    @Test
    public void testIniRealm(){
        // step1: 构建SecurityManager
        DefaultSecurityManager securityManager = new DefaultSecurityManager();


        // step4: 构建Realm
        IniRealm iniRealm = new IniRealm("classpath:shiro.ini");
        securityManager.setRealm(iniRealm);

        SecurityUtils.setSecurityManager(securityManager);
        // step2: 构建Subject
        Subject subject = SecurityUtils.getSubject();


        // step3: 登录
        AuthenticationToken token = new UsernamePasswordToken("liuge","123456");

        subject.login(token);

        System.out.println(subject.isAuthenticated());

        subject.checkRole("admin");
        subject.checkRole("hr");
        subject.checkRoles("admin","hr");

        subject.checkPermission("user:delete");
        subject.checkPermission("user:update");
//        subject.checkPermission("user:insert");

    }



}
//shiro:
//        authentication 认证
//        authorization 授权
//
//        cryptography 加密
//        session management 会话管理
//
//        数据平台地址：bigdata.liugedata.com   《=== admin/admin 登录上去
//
//        zeppelin嵌入到数据平台里面去【iframe】,还需要根据用户名和密码进行第二次登录，这样就很难受。如何把数据平台和zepplelin进行一个打通呢？
//
//        答：把Realm 对接到自己的平台上去
//
//        登录数据平台之后，有权限就可以看见zeppelin ,没有权限就没法操作zeppelein
//        并有权限可以直接点击进行，不用再次登录zeppelin
//
//        数据平台内嵌shiro 数据平台跨域问题？浏览器兼容性问题？
//
//        Subject 主体
//        Security Manager 安全管理器
//        Authenticator  认证器
//        Authorizer   授权器
//        Realm 可以有1..N个，安全
//
//        step1:SecurityManager
//        step2:构建Subject
//        step3:登录
//        step4:构建Realm
//
//
//        如何在生产中使用JdbcRealm 来作为安全数据源？