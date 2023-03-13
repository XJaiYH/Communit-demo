package com.xianj.community.dao.elasticsearch;

import com.xianj.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscusPostRepository extends ElasticsearchRepository<DiscussPost, Integer> {

}

