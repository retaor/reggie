package com.paopao.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.paopao.reggie.dto.SetmealDto;
import com.paopao.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    /**
     * 新增套餐，同时需要 保存套餐和菜品的关联关系
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐，同时需要删除套餐和菜品关联数据
     */
    public void removeWithDish(List<Long> ids);

    /**
     * 根据Id查询套餐数据
     * @param id
     * @return
     */
    public SetmealDto getByIdSetmealDto(Long id);

    /**
     * 修改套餐信息
     * @param setmealDto
     */
    public void updateSetmeal(SetmealDto setmealDto);

    /**
     * 修改售卖状态
     * @param status
     * @param ids
     */
    public boolean batchUpdateStatusBuIds(Integer status,List<Long> ids);

}
