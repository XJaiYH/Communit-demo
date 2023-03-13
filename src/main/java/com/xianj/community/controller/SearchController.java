package com.xianj.community.controller;

import com.xianj.community.entity.DiscussPost;
import com.xianj.community.entity.Page;
import com.xianj.community.service.ElasticSearchService;
import com.xianj.community.service.LikeService;
import com.xianj.community.service.UserService;
import com.xianj.community.util.CommunityConstent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstent{

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    // /search?keyword=xxx
    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public String search(Model model, String keyword, Page page){
        // 搜索帖子
        org.springframework.data.domain.Page<DiscussPost> searchResult =
                elasticSearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());

        // 聚合数据 因为不仅返回帖子，还要返回user  likeCount
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if(searchResult != null){
            for(DiscussPost post : searchResult){
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                map.put("user", userService.findUserById(post.getUserId()));
                map.put("like", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("keyword", keyword);

        // 分页
        page.setPath("/search?keyword=" + keyword);
        page.setRows(searchResult == null? 0 : (int) searchResult.getTotalElements());
        return "/site/search";
    }

}
