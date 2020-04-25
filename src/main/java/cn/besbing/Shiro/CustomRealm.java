package cn.besbing.Shiro;

import cn.besbing.Dao.SmUserMapper;
import cn.besbing.Entities.SmUser;
import cn.besbing.Service.ISmuserService;
import cn.besbing.Service.Impl.SmuserServiceImpl;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ContextLoader;


public class CustomRealm extends AuthorizingRealm {

    /**
     * 自动注入
     */
    @Autowired
    private SmuserServiceImpl smuserService;

     Logger logger = LoggerFactory.getLogger(this.getClass());

    {
        super.setName("customRealm");
    }


    /**
     * @param principalCollection 传过来的凭证
     * @return add了roles和permissions后的AuthorizationInfo
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return null;
    }

    /**
     *
     * @param authenticationToken   usernameandpasswordtoken
     * @return   传进来的token
     * @throws AuthenticationException   校验不通过的返回
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {



        //获取用户
        String username = (String) authenticationToken.getPrincipal();
        System.out.println("=============="+username);
        SmUser smUser  = smuserService.selectUserPasswordByUserCode(username);

        if(smUser == null || smUser.getUserPassword() == null ){
            logger.info("认证失败，空返回");
            return null;
        }

        SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(smUser.getUserName(),smUser.getUserPassword(),"customRealm");
        logger.info("认证完成，顺利返回");
        return authenticationInfo;
    }
}
