package com.xianj.community.entity;
/*
* 封装分页信息
* */
public class Page {

    private int current = 1;// 当前页码
    private int limit = 10;// 每页显示上限
    private int rows;//数据总数，服务端查出，用于计算总页数
    private String path;//查询路径，复用分页链接

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if(current >= 1) {
            this.current = current;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if(limit >= 1 && limit <= 20) {
            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if(rows >= 0) {
            this.rows = rows;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取当前页起始行
     * @return
     */
    public int getOffset(){
        return (current - 1) * limit;
    }

    /**
     * 获取总页数
     * @return
     */
    public int getTotal(){
        if(rows % limit == 0)
            return (rows / limit);
        else{
            return (rows / limit) + 1;
        }
    }

    /**
     * 获取起始页码
     * @return
     */
    public int getFrom(){
        if(current > 2)
            return current - 2;
        else return 1;
    }
    /**
     * 获取结束页码
     * @return
     */
    public int getTo(){
        int total = getTotal();
        if(current < total - 2)
            return current + 2;
        else return total;
    }
}
