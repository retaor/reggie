package com.paopao.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paopao.reggie.common.CustomException;
import com.paopao.reggie.common.R;
import com.paopao.reggie.dto.SetmealDto;
import com.paopao.reggie.entity.Category;
import com.paopao.reggie.entity.Setmeal;
import com.paopao.reggie.service.CategoryService;
import com.paopao.reggie.service.SetmealDishService;
import com.paopao.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequestMapping("/setmeal")
@RestController
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;


    /**
     * 添加套餐信息
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info("套餐信息：{}",setmealDto);

        setmealService.saveWithDish(setmealDto);

        return R.success("添加套餐成功");
    }

    /**
     * 分页查询套餐列表
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        //分页构造器
        Page<Setmeal> pageInfo = new Page(page, pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>();
        //条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper();
        //添加查询条件，根据name进行like模糊查询
        queryWrapper.like(StringUtils.isNotEmpty(name),Setmeal::getName,name);

        //添加排序条件，根据更新时间降序
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //执行查询
        setmealService.page(pageInfo, queryWrapper);

        //拷贝
        BeanUtils.copyProperties(pageInfo,setmealDtoPage,"records");

        List<Setmeal> records = pageInfo.getRecords();
        //
        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            //分类Id
            Long categoryId = item.getCategoryId();
            //根据分类Id查询category对象
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        setmealDtoPage.setRecords(list);


        return R.success(setmealDtoPage);
    }

    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("删除的套餐id:{}",ids);
        setmealService.removeWithDish(ids);
        return R.success("套餐数据删除成功");
    }

    /**
     * 根据条件查询套餐数据
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());

        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }



    /**
     * 根据Id查询套餐详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> get(@PathVariable Long id){
        log.info("要查询的套餐Id：{}",id);
        SetmealDto setmealDto = setmealService.getByIdSetmealDto(id);


        return R.success(setmealDto);
    }
    //修改套餐信息
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        log.info("修改的套餐信息：{}",setmealDto.toString());

        setmealService.updateSetmeal(setmealDto);


        return R.success("套餐修改成功");
    }

    /**
     * 修改售卖状态
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateSaleStatus(@PathVariable("status") Integer status,@RequestParam List<Long> ids){
        log.info("修改售卖状态status：{},修改Id：{}",status,ids);

        if(status != 0 && status != 1){
            throw new CustomException("菜品售卖状态异常");
        }
        if (setmealService.batchUpdateStatusBuIds(status, ids)) {
            return R.success("售卖状态修改成功");
        }else{
            return R.error("售卖状态修改失败");
        }


    }

}
