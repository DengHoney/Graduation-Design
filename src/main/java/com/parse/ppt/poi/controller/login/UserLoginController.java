package com.parse.ppt.poi.controller.login;

import com.parse.ppt.poi.commom.ReturnCode;
import com.parse.ppt.poi.entity.User;
import com.parse.ppt.poi.service.cookie.CookieService;
import com.parse.ppt.poi.service.login.UserLoginService;
import com.parse.ppt.poi.service.mail.MailService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * @author Jupiter
 */
@Controller
@RequestMapping("/login")
public class UserLoginController {
    private Logger logger = LogManager.getLogger(this.getClass());

    @Autowired
    private UserLoginService userLoginService;
    @Autowired
    private CookieService cookieService;
    @Autowired
    private MailService mailService;

    @RequestMapping("/loadUserFromCookies")
    @ResponseBody
    public String loadUserFromCookies(HttpSession session) {
        logger.info("UserLoginController.loadUserFromCookies   ------->  start! ");

        User user = cookieService.loadUserCookie();
        if (user == null) {
            logger.info("UserLoginController.loadUserFromCookies   ------->  end! ");
            return ReturnCode.FAILED;
        } else {
            session.setAttribute("user", user);
        }
        logger.info("UserLoginController.loadUserFromCookies   ------->  end!  user = " + user);
        return ReturnCode.SUCCESS;
    }

    @RequestMapping("/userLogin")
    @ResponseBody
    public String userLogin(@RequestParam("account") String account,
                            @RequestParam("password") String password,
                            @RequestParam("rememberTag") String rememberTag,
                            HttpSession session) {
        logger.info("UserLoginController.checkUserLogin   ------->  start! " +
                "  account = " + account +
                "  password = " + password +
                "  rememberTag = " + rememberTag);

        User user = userLoginService.getUser(account);
        String result = userLoginService.verifyUser(user, password);
        // 如果密码校验通过
        if (result.equals(ReturnCode.SUCCESS)) {
            // 如果 前端用户点选了 1天内记住我
            if ("true".equals(rememberTag.toLowerCase())) {
                // 把用户名和密码保存到Cookie对象中
                result = cookieService.addUserCookie(user);
            }
            // 把该用户对象放在session中
            session.setAttribute("user", user);
        }
        logger.info("UserLoginController.checkUserLogin   ------->  end! " +
                "  result = " + result);
        return result;
    }

    @RequestMapping("/userRegister")
    @ResponseBody
    public String userRegister(@RequestParam("username") String username,
                               @RequestParam("email") String email,
                               @RequestParam("phoneNum") String phoneNum,
                               @RequestParam("password") String password,
                               HttpSession session) {
        logger.info("UserLoginController.userRegister   ------->  start! " +
                "  username = " + username +
                "  email = " + email +
                "  phoneNum = " + phoneNum +
                "  password = " + password);

        User user = new User(username, email, phoneNum, password);
        String result = userLoginService.registerUser(user);
        // 如果注册成功，则将当前用户信息写入session
        if (ReturnCode.SUCCESS.equals(result)) {
            session.setAttribute("user", user);
            // 并且将用户信息发送到用户邮箱
            result = mailService.sendSimpleWordMail(email, "亲爱的朋友，明天请过来接我~我在百货大楼下等你~");
        }
        logger.info("UserLoginController.userRegister   ------->  end! " +
                "result = " + result);
        return result;
    }

    @RequestMapping("/userLogout")
    @ResponseBody
    public String userLogout(HttpSession session) {
        logger.info("UserLoginController.userLogout   ------->  start! ");

        User user = (User) session.getAttribute("user");
        String result = cookieService.removeUserCookie(user);
        session.removeAttribute("user");
        logger.info("UserLoginController.userLogout   ------->  end! " +
                " result = " + result);
        return result;
    }
}
