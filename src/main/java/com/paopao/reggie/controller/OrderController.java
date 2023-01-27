package com.paopao.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paopao.reggie.common.BaseContext;
import com.paopao.reggie.common.R;
import com.paopao.reggie.dto.OrdersDto;
import com.paopao.reggie.entity.OrderDetail;
import com.paopao.reggie.entity.Orders;
import com.paopao.reggie.service.OrderDetailService;
import com.paopao.reggie.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrdersService ordersService;
    @Autowired
    private OrderDetailService orderDetailService;



    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("用户下单数据：{}",orders);
        ordersService.submit(orders);

        return R.success("下单成功");
    }

    /**
     * 订单页分页查询数据
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
    */
    @GetMapping("/page")
    public R<Page> page(Integer page, Integer pageSize, Long number, String beginTime, String endTime){
        log.info("测试传入的page对象：{},{},{},{},{}",page,pageSize,number,beginTime,endTime);
        Page<Orders> ordersPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(number != null,Orders::getNumber,number)
                .gt(StringUtils.isNotEmpty(beginTime),Orders::getOrderTime,beginTime)
                .lt(StringUtils.isNotEmpty(endTime),Orders::getOrderTime,endTime);
        queryWrapper.orderByDesc(Orders::getOrderTime);
        ordersService.page(ordersPage, queryWrapper);

        return R.success(ordersPage);

    }

    /**
     * 修改订单状态
     * @param orders
     * @return
     */
    @PutMapping
    public R<Orders> updateStatus(@RequestBody Orders orders){
        log.info("修改订单状态传入数据：{}", orders);

        Integer status = orders.getStatus();
        if (status != null){
            orders.setStatus(3);
        }
        ordersService.updateById(orders);
        return R.success(orders);
    }

    //个人中心（最新订单查询、历史订单、地址管理-修改地址、地址管理-删除地址）
    //购物车（删除购物车中的商品)

    /**
     * 客户端历史订单分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> orderRecordPage(Integer page,Integer pageSize){
        log.info("历史订单页分页查询：{},{}" ,page,pageSize);
        //需要两个分页构造器
        Page<Orders> ordersPage = new Page<>(page, pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>();

        //条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        Long userId = BaseContext.getCurrentId();
        queryWrapper.eq(Orders::getUserId, userId);
        queryWrapper.orderByDesc(Orders::getOrderTime);
        //执行分页查询
        ordersService.page(ordersPage, queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(ordersPage,ordersDtoPage,"records");

        List<Orders> records = ordersPage.getRecords();
        List<OrdersDto> list = records.stream().map(orders -> {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(orders,ordersDto);

            Long orderId = Long.valueOf(orders.getNumber());

//            OrderDetail orderDetail = orderDetailService.getById(orderId);

            //查询订单下的菜品
            //select * from order_detail where order_id = ?
            LambdaQueryWrapper<OrderDetail> orderDetailLambdaQueryWrapper = new LambdaQueryWrapper<>();
            orderDetailLambdaQueryWrapper.eq(OrderDetail::getOrderId,orderId);
            List<OrderDetail> orderDetails = orderDetailService.list(orderDetailLambdaQueryWrapper);
            ordersDto.setOrderDetails(orderDetails);
            return ordersDto;
        }).collect(Collectors.toList());

        ordersDtoPage.setRecords(list);

        return R.success(ordersDtoPage);
    }


}
