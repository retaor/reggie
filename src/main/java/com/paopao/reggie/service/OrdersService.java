package com.paopao.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.paopao.reggie.entity.Orders;

public interface OrdersService extends IService<Orders> {


    /**
     * 用户下单
     * @param orders
     */
    public void submit(Orders orders);


    /**
     * 客户端查询历史订单
     * @param page
     * @param pageSize
     */
    public void OrderRecord(Integer page,Integer pageSize);
}
