package com.paopao.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.paopao.reggie.common.CustomException;
import com.paopao.reggie.dto.DishDto;
import com.paopao.reggie.entity.Dish;
import com.paopao.reggie.entity.DishFlavor;
import com.paopao.reggie.mapper.DishMapper;
import com.paopao.reggie.service.DishFlavorService;
import com.paopao.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;


    /**
     * 新增菜品。同时保存对应的口味数据
     *
     * @param dishDto
     */
    @Override
    @Transactional //因为操作多张表，添加事务注解
    public void saveWithFlavor(DishDto dishDto) {
        // 保存菜品的基本信息到菜品表dish
        this.save(dishDto);

        Long dishId = dishDto.getId(); //菜品Id

        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        // 遍历集合，并把dishId添加进集合后重新封装
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        //保存菜品口味数据到菜品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);


    }

    /**
     * 根据Id查询菜品信息和对应的口味信息
     *
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息，从dish表查询
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);

        //查询菜品口味信息，从dishFlavor表查询
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());

        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);

        dishDto.setFlavors(flavors);

        return dishDto;
    }

    /**
     * 修改菜品
     * @param dishDto
     */
    @Override
    @Transactional  // 开启事务注解，保证事务的一致性
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish表信息
        this.updateById(dishDto);

        //清理当前菜品对应口味信息--- dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(queryWrapper);
        //添加当前提交过来的口味数据---dish_flavor表的update操作
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        //更新dishFlavor表信息
        dishFlavorService.saveBatch(flavors);

    }

    /**
     * 批量删除菜品
     *
     * @param ids
     */
    @Override
    @Transactional //开启事务管理
    public void batchDeleteByIds(List<Long> ids) {
        //
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ids != null, Dish::getId, ids);
        List<Dish> list = this.list(queryWrapper);
        for (Dish dish : list) {
            if (dish.getStatus() == 0) {
                this.removeByIds(ids);


            } else {
                throw new CustomException("有菜品正在售卖，无法全部删除！");
            }
        }

    }

    /**
     * 批量修改菜品状态为启售或停售
     * @param status
     * @param ids
     */
    @Override
    @Transactional //开启事务管理
    public boolean batchUpdateByIds(Integer status,List<Long> ids) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //select * from dish where dish_id in (?,?)
        queryWrapper.in(ids != null,Dish::getId,ids);
        List<Dish> list = this.list(queryWrapper);
        if (list != null){
            for (Dish dish : list) {
                dish.setStatus(status);
                this.updateById(dish);
            }
            return true;
        } else {
            return false;
        }


    }
}
