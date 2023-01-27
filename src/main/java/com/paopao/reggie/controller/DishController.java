package com.paopao.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paopao.reggie.common.CustomException;
import com.paopao.reggie.common.R;
import com.paopao.reggie.dto.DishDto;
import com.paopao.reggie.entity.Category;
import com.paopao.reggie.entity.Dish;
import com.paopao.reggie.entity.DishFlavor;
import com.paopao.reggie.service.CategoryService;
import com.paopao.reggie.service.DishFlavorService;
import com.paopao.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private DishService dishService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }


    /**
     * 菜品分页信息查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){

        //构造分页构造器
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();
        //构造条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        // 添加过滤条件
        queryWrapper.like( name != null, Dish::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行分页查询
        dishService.page(pageInfo,queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list =records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);

            Long categoryId = item.getCategoryId();//获得菜品分类Id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if (category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);

            }

            return dishDto;

        }).collect(Collectors.toList()); //把categoryName赋予dishDto后重新封装list集合


        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }

    /**
     * 根据Id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){

        DishDto dishdto = dishService.getByIdWithFlavor(id);

        return R.success(dishdto);
    }

    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.updateWithFlavor(dishDto);
        return R.success("修改菜品成功");
    }

/*    *//**
     * 根据条件查询对应的菜品数据
     * @param dish
     * @return
     *//*
     //TODO
    @GetMapping("/list")
    public R<List<Dish>> list(Dish dish){

        //构建查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        //添加条件，查询状态是起售状态
        queryWrapper.eq(Dish::getStatus, 1);

        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);


        return R.success(list);
    }*/

    /**
     * 根据条件查询对应的菜品数据
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){

        //构建查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        //添加条件，查询状态是起售状态
        queryWrapper.eq(Dish::getStatus, 1);

        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        List<DishDto> dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);

            //当前菜品Id
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> LambdaQueryWrapper = new LambdaQueryWrapper<>();
            LambdaQueryWrapper.eq(DishFlavor::getDishId, dishId);
            List<DishFlavor> dishFlavorList = dishFlavorService.list(LambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());


        return R.success(dishDtoList);
    }

    /**
     * 批量修改售卖状态
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateSaleStatus(@PathVariable("status") Integer status,@RequestParam List<Long> ids){
        log.info("批量修改菜品状态为：{}, id：{}", status, ids );

        if(status != 0 && status != 1){
            throw new CustomException("菜品售卖状态异常");
        }
        if (dishService.batchUpdateByIds(status,ids)){
            return R.success("销售状态修改成功");
        } else {
            return R.error("销售状态修改失败");
        }


    }

    /**
     * 删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> deleteSaleStatus(@RequestParam List<Long> ids){
        log.info("批量删除菜品id：{}", ids );
        dishService.batchDeleteByIds(ids);

        return R.success("菜品删除成功");
    }



}
