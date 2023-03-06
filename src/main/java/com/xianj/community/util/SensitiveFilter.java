package com.xianj.community.util;

import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.logging.Logger;
import org.mybatis.logging.LoggerFactory;
import org.slf4j.LoggerFactoryFriend;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    private static final String REPLACEMENT="***";

    private TrieNode root = new TrieNode();

    @PostConstruct  // 表示这是一个初始化方法，当容器实例化该对象，调用构造器之后，该方法被自动调用
    public void init(){
        try(
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
            // 用缓冲流读取数据的效率较高，所以将字节流转为缓冲流
            BufferedReader buffer = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while((keyword = buffer.readLine()) != null){
                this.addKeyWord(keyword);// 添加敏感词
            }
        } catch (IOException e) {
            logger.error(()->"加载敏感词文件失败" + e.getMessage());

        }
    }
    // 添加敏感词
    private void addKeyWord(String keyword) {
        TrieNode tmpNode = root;
        for(int i=0;i<keyword.length();i++){
            Character ch = keyword.charAt(i);
            TrieNode subNode;
            subNode = root.getSubNode(ch);
            if(subNode == null){// 如果树中没有这个节点，新建
                subNode = new TrieNode();
                tmpNode.addSubNode(ch, subNode);
            }
            // 遍历子节点
            tmpNode = subNode;
            // 到单词为，设置为敏感词
            if(i == keyword.length() - 1){
                tmpNode.setKeywordEnd(true);
            }
        }
    }

    // 用于过滤敏感词，参数为待过滤文本，返回过滤后的文本，被外界调用使用
    public String filter(String text){
        if(StringUtils.isBlank(text)){
            return null;
        }
        // 指针1
        TrieNode tmpNode = this.root;
        // 指针2
        int begin = 0;
        // 指针3
        int end = 0;
        // 记录结果
        StringBuilder res = new StringBuilder();
        while(end < text.length()){
            Character ch = text.charAt(end);
            if(this.isSymbol(ch)){
                // 若指针1处于根，将符号计入结果，指针2向后走一步
                if(tmpNode == this.root){
                    res.append(ch);
                    begin++;
                }
                // 指针3都向后走一步
                //res.append(ch);
                end++;
                continue;
            }
            // 检查下级节点
            tmpNode = tmpNode.getSubNode(ch);
            if(tmpNode == null){// 以begin开头的字符串不是敏感词，记录begin对应的字符
                res.append(text.charAt(begin));
                begin++;
                end = begin;
                tmpNode = this.root;
            } else if(tmpNode.isKeywordEnd){
                // 发现敏感词，替换敏感词
                begin = end + 1;
                end = begin;
                tmpNode = this.root;
                res.append(REPLACEMENT);
            } else{
                // 检查下一个字符

                end++;
            }
        }
        res.append(text.substring(begin, end));
        return res.toString();
    }

    // 是否为符号，是返回true
    private boolean isSymbol(Character ch){
        return !CharUtils.isAsciiAlphanumeric(ch) && (ch < 0x2E80 || ch > 0x9FFF);// 要在东亚的文字范围之外
    }
    // 前缀树
    private class TrieNode{
        // 关键词结束标识
        private boolean isKeywordEnd = false;
        // 子节点, key为下级节点字符，value指向下级节点
        private Map<Character, TrieNode> subNode = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // 添加子节点
        public void addSubNode(Character ch, TrieNode node){
            subNode.put(ch, node);
        }

        // 获取节点
        public TrieNode getSubNode(Character ch){
            return subNode.get(ch);
        }
    }

}
