package com.xianj.community;

import com.xianj.community.dao.DiscussPostMapper;
import com.xianj.community.dao.elasticsearch.DiscusPostRepository;
import com.xianj.community.entity.DiscussPost;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class) // 使用main里面类的配置
public class ElasticsearchTest {
    // 从MySQL中取到数据然后转存到elastic   这是elastic中数据的来源
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private DiscusPostRepository repository;
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @PostConstruct// 管理bean的生命周期
    public void init(){// 在构造函数之后执行
        // 解决netty启动冲突的问题（elasticsearch和mybatis都会启动netty，但elasticsearch会检查当前是否有netty启动，
        // 若有则抛出异常，所以我们需要修改使其不报异常，而正常执行）
        // see  Netty4Utils.setAvailableProcessors()
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }
    @Test
    // 往elastic中插入数据
    public void testInsert(){
        // 如果index不存在，会自动创建索引
        repository.save(discussPostMapper.selectDiscussPostById(241));
        repository.save(discussPostMapper.selectDiscussPostById(242));
        repository.save(discussPostMapper.selectDiscussPostById(243));
    }

    @Test
    // 网elastic中插入多条数据
    public void testInsertList(){
        // 如果index不存在，会自动创建索引
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(111, 0, 50);
        repository.saveAll(list);
        repository.saveAll(discussPostMapper.selectDiscussPosts(101, 0, 50));
        repository.saveAll(discussPostMapper.selectDiscussPosts(102, 0, 50));
        repository.saveAll(discussPostMapper.selectDiscussPosts(103, 0, 50));
        repository.saveAll(discussPostMapper.selectDiscussPosts(112, 0, 50));
        repository.saveAll(discussPostMapper.selectDiscussPosts(131, 0, 50));
        repository.saveAll(discussPostMapper.selectDiscussPosts(132, 0, 50));
        repository.saveAll(discussPostMapper.selectDiscussPosts(133, 0, 50));
        repository.saveAll(discussPostMapper.selectDiscussPosts(134, 0, 50));
    }

    @Test
    // 修改elastic中的数据
    public void testUpdate(){
        DiscussPost post = discussPostMapper.selectDiscussPostById(231);
        post.setContent("新人驾到，大家好呀！");
        repository.save(post);
    }

    @Test
    // 删除elastic中的数据
    public void testDelete(){
        repository.deleteById(231);
        // repository.deleteAll();// 删除所有数据
    }

    @Test
    // 搜索elastic中的数据  利用接口搜索
    public void testSearchByRepository(){
         // 构造搜索条件
    }
}
