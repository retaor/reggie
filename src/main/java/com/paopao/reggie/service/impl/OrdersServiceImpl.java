package com.paopao.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.paopao.reggie.common.BaseContext;
import com.paopao.reggie.common.CustomException;
import com.paopao.reggie.entity.*;
import com.paopao.reggie.mapper.OrdersMapper;
import com.paopao.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private UserService userService;
    @Autowired
    private AddressBookSeivice addressBookSeivice;

    @Autowired
    private OrderDetailService orderDetailService;
    /**
     * 用户下单
     * 操作3张表
     * @param orders
     */
    @Override
    @Transactional //事务控制
    public void submit(@RequestBody Orders orders) {
        //获取用户id
        Long currentId = BaseContext.getCurrentId();

        //查询用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        if (list == null || list.size() == 0){
            throw new CustomException("购物车为空，不能下单");
        }
        //查询用户数据
        User user = userService.getById(currentId);

        //查询地址
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookSeivice.getById(addressBookId);
        if (addressBook == null){
            throw new CustomException("用户地址信息有误，不能下单");
        }

        long orderId = IdWorker.getId(); //生成订单号

        AtomicInteger amount = new AtomicInteger(0); //原子操作，累加计算总金额


        List<OrderDetail> ordersDetailList = list.stream().map((item) -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue()); //计算购物车总金额 单价*分数
            return orderDetail;
        }).collect(Collectors.toList());


        //向订单表插入数据，一条数据
        orders.setNumber(String.valueOf(orderId));
        orders.setStatus(2);
        orders.setUserId(currentId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setAmount(new BigDecimal(amount.get()));
        orders.setUserId(currentId);
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null? "" : addressBook.getProvinceName())+
                (addressBook.getCityName() == null? "" : addressBook.getCityName())+
                (addressBook.getDistrictName() == null? "" : addressBook.getDistrictName())+
                (addressBook.getDetail() == null? "" : addressBook.getDetail()));




        this.save(orders);

        //向订单明细表插入数据

        orderDetailService.saveBatch(ordersDetailList);

        //清空购物车数据
        shoppingCartService.remove(queryWrapper);


    }

    @Override
    public void OrderRecord(Integer page, Integer pageSize) {
        Page<Orders> ordersPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByDesc(Orders::getOrderTime);
        this.page(ordersPage, queryWrapper);
    }

}
