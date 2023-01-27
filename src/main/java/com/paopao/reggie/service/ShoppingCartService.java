package com.paopao.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.paopao.reggie.entity.ShoppingCart;

public interface ShoppingCartService extends IService<ShoppingCart> {
    /**
     * 删除购物车菜品
     * @param shoppingCart
     */
    public void deleteCart(ShoppingCart shoppingCart);
}
