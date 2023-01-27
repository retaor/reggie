package com.paopao.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.paopao.reggie.common.CustomException;
import com.paopao.reggie.dto.SetmealDto;
import com.paopao.reggie.entity.Setmeal;
import com.paopao.reggie.entity.SetmealDish;
import com.paopao.reggie.mapper.SetmealMapper;
import com.paopao.reggie.service.SetmealDishService;
import com.paopao.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增套餐，同时需要 保存套餐和菜品的关联关系
     *
     * @param setmealDto
     */
    @Override
    @Transactional //添加事务注解，要么全成功要么全失败
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐的基本信息，操作setmeal，执行insert操作
        this.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList()); //获取setmealDishes的id并从新封装集合

        //保存套餐和菜品的关联信息，操作setmeal，执行insert操作
        setmealDishService.saveBatch(setmealDishes);

    }

    /**
     * 删除套餐，同时需要删除套餐和菜品关联数据
     */
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
//        select * from setmeal where id in(?,?,?) and status = 1
        //查询套餐的状态，确定是否可以删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus, 1);

        long count = this.count(queryWrapper);
        if (count >0 ){
            //如果能删除，抛出一个异常
            throw new CustomException("套餐正在售卖中，不能删除");
        }


        //如果可以删除，先删除套餐表中的数据
        this.removeByIds(ids);
        //删除关系表中的数据 setmeal_dish
        // delete form setmeal_dish where setmeal_id in (? , ? ,?...)
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(lambdaQueryWrapper);

    }

    /**
     * 根据Id查询套餐详情
     * @param id
     * @return
     */
    @Override
    public SetmealDto getByIdSetmealDto(Long id) {
        Setmeal setmeal = this.getById(id);
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal,setmealDto);

        LambdaQueryWrapper<SetmealDish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(SetmealDish::getSetmealId, setmeal.getId());
        List<SetmealDish> list = setmealDishService.list(dishLambdaQueryWrapper);
        setmealDto.setSetmealDishes(list);

        return setmealDto;
    }

    /**
     * 修改套餐信息
     * @param setmealDto
     */
    @Override
    @Transactional
    public void updateSetmeal(SetmealDto setmealDto) {
        //修改套餐信息要修改两个表，套餐表和套餐所含菜品表
        //修改套餐表
        this.updateById(setmealDto);
        //修改套餐详情表
        //1清除套餐Id下的旧数据
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(queryWrapper);
        //2更新新数据
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map(setmealDish -> {
            setmealDish.setSetmealId(setmealDto.getId());
            return setmealDish;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes);
    }

    @Override
    public boolean batchUpdateStatusBuIds(Integer status, List<Long> ids) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ids != null, Setmeal::getId, ids);
        List<Setmeal> list = this.list(queryWrapper);
        if (status != null){
            for (Setmeal setmeal : list) {
                setmeal.setStatus(status);
                this.updateById(setmeal);
            }
            return true;
        } else {
            return false;

        }

    }


}
