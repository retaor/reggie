package com.paopao.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.paopao.reggie.entity.ShoppingCart;
import com.paopao.reggie.mapper.ShoppingCartMapper;
import com.paopao.reggie.service.ShoppingCartService;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
    /**
     * 删除购物车菜品
     *
     * @param shoppingCart
     */
    @Override
    public void deleteCart(ShoppingCart shoppingCart) {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(shoppingCart.getDishId() != null, ShoppingCart::getDishId, shoppingCart.getDishId());
        queryWrapper.eq(shoppingCart.getSetmealId() != null, ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        ShoppingCart one = this.getOne(queryWrapper);
        Integer number = one.getNumber();
        if (number > 1) {
            one.setNumber(number-1);
            this.updateById(one);
        } else {
            this.remove(queryWrapper);
        }

    }
}
