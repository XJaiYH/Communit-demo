package com.xianj.community;

import com.xianj.community.dao.DiscussPostMapper;
import com.xianj.community.dao.LoginTicketMapper;
import com.xianj.community.dao.MessageMapper;
import com.xianj.community.dao.UserMapper;
import com.xianj.community.entity.DiscussPost;
import com.xianj.community.entity.LoginTicket;
import com.xianj.community.entity.Message;
import com.xianj.community.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class) // 使用main里面类的配置
public class MapperTests {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MessageMapper messageMapper;
    @Test
    public  void testInsertLoginTicket(){
        LoginTicket loginTicket1 = new LoginTicket();
        loginTicket1.setUserId(101);
        loginTicket1.setTicket("000");
        loginTicket1.setStatus(1);
        loginTicket1.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));
        loginTicketMapper.insertLoginTicket(loginTicket1);
    }
    @Test
    public void testSelectAndUpdateLoginicket(){
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("000");
        System.out.println(loginTicket);

        loginTicketMapper.updateStatus("000", 0);
        loginTicket = loginTicketMapper.selectByTicket("000");
        System.out.println(loginTicket);
    }
    @Test
    public void testSelectDiscussPost(){
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(101,0,10);
        for(DiscussPost post : list) {
            System.out.println(post);
        }

        int rows = discussPostMapper.selectDiscussPostRows(101);
        System.out.println(rows);
    }


    @Test
    public void testSelectUser(){
        User user = userMapper.selectById(101);
        System.out.println(user);

        user = userMapper.selectByName("liubei");
        System.out.println(user);

        user = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);
    }

    @Test
    public void testInsertUser(){
        User user = new User();
        user.setUsername("zhangsan");
        user.setPassword("123456");
        user.setSalt("sdjfs");
        user.setType(0);
        user.setEmail("111@163.com");
        user.setActivationCode("101");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());
        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void testUpdateUser(){
        int rows = userMapper.updateStatus(150, 2);// 返回修改的行数
        System.out.println(rows);

        rows = userMapper.updateHeader(150, "http://www.nowcoder.com/105.png");// 返回修改的行数
        System.out.println(rows);

        rows = userMapper.updatePassword(150, "654321");// 返回修改的行数
        System.out.println(rows);

        User user = userMapper.selectById(150);
        System.out.println(user);
    }

    @Test
    public void testMessage(){
        List<Message> messages = messageMapper.selectConversations(111, 0, 10);
        for(Message message : messages){
            System.out.println(message.toString());
        }
        int count = messageMapper.selectConversationCount(111);
        System.out.println(count);

        messages = messageMapper.selectMessagesForSingleConv("111_112", 0, 10);
        for(Message message : messages){
            System.out.println(message.toString());
        }
        count = messageMapper.selectMessageCountForSingleConv("111_112");
        System.out.println(count);
        count = messageMapper.selectMessageUnreadCount(111, "111_131");
        System.out.println(count);
        count = messageMapper.selectMessageUnreadCount(131, null);
        System.out.println(count);

    }
}
