package com.xianj.community.service;

import com.mysql.cj.log.Log;
import com.xianj.community.dao.LoginTicketMapper;
import com.xianj.community.dao.UserMapper;
import com.xianj.community.entity.LoginTicket;
import com.xianj.community.entity.User;
import com.xianj.community.util.*;
import org.apache.commons.lang3.CharSet;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstent {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;
    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Autowired
    LoginTicketMapper loginTicketMapper;
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private RedisTemplate redisTemplate;
    public User findUserById(int id){
        User user = getCache(id);
        if(user == null){
            user = initCache(id);
        }
        return user;
        // return userMapper.selectById(id);
    }

    public Map<String, Object> register(User user){
        Map<String, Object> map = new HashMap<>();
        if(user == null){
            throw new IllegalArgumentException("参数不能为空！");
        }
        if(StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        if(user.getPassword().length() < 8){
            map.put("passwordMsg", "密码长度不能小于8位！");
            return map;
        }
        if(user.getPassword().contains(" ")){
            map.put("passwordMsg", "密码不能包含空格！");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg", "邮箱不能为空！");
            return map;
        }

        User u = userMapper.selectByName(user.getUsername());
        if(u != null){
            map.put("usernameMsg", "该账户名已存在！");
            return map;
        }
        u = userMapper.selectByEmail(user.getEmail());
        if(u != null){
            map.put("emailMsg", "该邮箱已被注册！");
            return map;
        }

        // 注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 给用户发注册邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // 激活路径？
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String conten = templateEngine.process("/mail/activation", context);
        mailClient.sendEmail(user.getEmail(), "账号激活通知",conten);
        return map;
    }

    public int activation(int userId, String code){// 在激活URL上拼接用户ID和激活码，就可以作未值传入
        User u = userMapper.selectById(userId);
        if(u.getStatus() == 1){
            return ACTIVATION_REPEAT;
        } else if (u.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            updateCache(userId);// 清理缓存
            return ACTIVATION_SUCCESS;
        }else{
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String, Object> login(String username, String password, int expiredSeconds){
        Map<String, Object> map = new HashMap<>();
        // 空值处理
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        // 验证账号
        User user = userMapper.selectByName(username);
        if(user == null){
            map.put("usernameMsg", "账号不存在！");
            return map;
        }
        // 验证激活状态
        if(user.getStatus() == 0) {
            map.put("usernameMsg", "账号未激活！");
        }
        // 验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if(!password.equals(user.getPassword())){
            map.put("passwordMsg", "密码错误！");
            return map;
        }
        // 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);// 0为有效态
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        loginTicket.setUserId(user.getId());
        //loginTicketMapper.insertLoginTicket(loginTicket);
        //将凭证存入redis
        String ticketKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(ticketKey, loginTicket);// redis会将对象序列化为JSON字符串保存

        // 浏览器只需要保留ticket，这样下次访问时，浏览器将ticket传给服务端，服务端检查ticket是否存在，是否过期，若存在且未过期，则保持登录状态，否则重新登录
        // 所以要将ticket返回给客户端，由于此处返回map，所以将ticket放入map中即可
        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    // 退出
    public void logout(String ticket){
        //loginTicketMapper.updateStatus(ticket, 1);
        // 使用redis
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(ticketKey, loginTicket);
    }

    // 查询凭证
    public LoginTicket findLoginTicket(String ticket){
        //return loginTicketMapper.selectByTicket(ticket);
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
    }
    // 更新用户头像文件的路径
    public int updateHeader(int userId, String headerUrl){
        // return userMapper.updateHeader(userId, headerUrl);
        int rows = userMapper.updateHeader(userId, headerUrl);
        updateCache(userId);// 清理缓存
        return rows;
    }

    // 更新用户密码
    public Map<String, Object> updatePassword(String oldPassword, String newPassword, String checkNewPassword){
        Map<String, Object> map = new HashMap<>();
        if(StringUtils.isBlank(oldPassword)){
            map.put("passwordOldMsg", "密码不能为空！");
            return map;
        }
        if(StringUtils.isBlank(newPassword)){
            map.put("passwordNewMsg", "密码不能为空！");
            return map;
        }
        if(StringUtils.isBlank(checkNewPassword)){
            map.put("passwordCheckMsg", "密码不能为空！");
            return map;
        }
        if(!newPassword.equals(checkNewPassword)){
            map.put("passwordNewMsg", "两次输入的密码不相同，请重新输入！");
            map.put("passwordCheckMsg", "两次输入的密码不相同，请重新输入！");
            return map;
        }
        if(newPassword.length() < 8){
            map.put("passwordNewMsg", "密码长度不能小于8位！");
            return map;
        }
        if(oldPassword.length() < 8){
            map.put("passwordOldMsg", "密码长度不能小于8位！");
            return map;
        }
        if(checkNewPassword.length() < 8){
            map.put("passwordCheckMsg", "密码长度不能小于8位！");
            return map;
        }
        if(newPassword.contains(" ")){
            map.put("passwordNewMsg", "密码不能包含空格！");
            return map;
        }
        if(checkNewPassword.contains(" ")){
            map.put("passwordCheckMsg", "密码不能包含空格！");
            return map;
        }

        User user = hostHolder.getUser();
        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        if(!oldPassword.equals(user.getPassword())){
            map.put("passwordOldMsg", "输入的旧密码错误，请重新输入！");
            return map;
        }
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        userMapper.updatePassword(user.getId(), newPassword);
        updateCache(user.getId());
        return map;
    }

    public User findUserByName(String username){
        return userMapper.selectByName(username);
    }

    // findUser时，优先从缓存中取值，若取到则返回，
    private User getCache(int userId){
        String userKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(userKey);
    }
    // 取不到则访问数据库，并更新到缓存，
    private User initCache(int userId){
        String userKey = RedisKeyUtil.getUserKey(userId);
        User user = userMapper.selectById(userId);
        redisTemplate.opsForValue().set(userKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }
    // 当更新用户数据时，也需要更新缓存（删除缓存中的该用户）
    private void updateCache(int userId){
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }

    // 某个用户的权限
    public Collection<? extends GrantedAuthority> getAuthorities(int userId){
        User user = this.findUserById(userId);
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()){
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }
}
