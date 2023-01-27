package com.paopao.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.paopao.reggie.dto.DishDto;
import com.paopao.reggie.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {

    // 新增菜品，同时插入菜品对应的口味数据，需要操作两张表： dish， dish_flavor
    public void saveWithFlavor(DishDto dishDto);

    //根据Id查询菜品信息和对应的口味信息
    public DishDto getByIdWithFlavor(Long id);

    //更新菜品信息，同时更新口味信息
    public void updateWithFlavor(DishDto dishDto);


    //批量删除菜品
    public void batchDeleteByIds(List<Long> ids);

    //批量修改菜品
    public boolean batchUpdateByIds(Integer status,List<Long> ids);
}
