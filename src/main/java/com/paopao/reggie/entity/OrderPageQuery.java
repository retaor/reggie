package com.paopao.reggie.entity;

import lombok.Data;

/**
 * 订单页查询实体类
 */
@Data
public class OrderPageQuery {
    //当前所在页
    private Integer page;
    //每页显示条数
    private Integer pageSize;
    //查询订单号码
    private Long number;
    //查询开始时间
    private String beginTime;
    //查询结束时间
//    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private String endTime;

}
