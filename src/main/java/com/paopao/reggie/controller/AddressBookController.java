package com.paopao.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.paopao.reggie.common.BaseContext;
import com.paopao.reggie.common.R;
import com.paopao.reggie.entity.AddressBook;
import com.paopao.reggie.service.AddressBookSeivice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 地址簿管理
 */
@RestController
@RequestMapping("/addressBook")
@Slf4j
public class AddressBookController {

    @Autowired
    private AddressBookSeivice addressBookSeivice;

    //新增
    //设置默认地址
    //根据id查询地址
    //查询默认地址
    //查询指定用户的全部地址

    /**
     * 新增地址
     * @param addressBook
     * @return
     */
    @PostMapping
    public R<AddressBook> save(@RequestBody AddressBook addressBook){
        log.info("接收数据：{}",addressBook.toString());
        addressBook.setUserId(BaseContext.getCurrentId()); //从ThreadLocal中获取userid赋予对象
        log.info("addressBook：{}",addressBook);
        addressBookSeivice.save(addressBook);

        return R.success(addressBook);
    }

    /**
     * 查看地址列表
     * @param addressBook
     * @return
     */
    @GetMapping("list")
    public R<List<AddressBook>> list(AddressBook addressBook){
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("addressBool:{}",addressBook);

        //条件构造器
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(AddressBook::getUserId, addressBook.getUserId());
        queryWrapper.orderByDesc(AddressBook::getUpdateTime);

        //select * from addressBook where user_id = ? order by update_time desc

        return R.success(addressBookSeivice.list(queryWrapper));
    }

    /**
     * 设置默认地址
     * @param addressBook
     * @return
     */
    @PutMapping("/default")
    public R<AddressBook> defaultAddress(@RequestBody AddressBook addressBook){
        log.info("addressBook：{}",addressBook);
        LambdaUpdateWrapper<AddressBook> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        updateWrapper.set(AddressBook::getIsDefault, 0);

        addressBookSeivice.update(updateWrapper);

        addressBook.setIsDefault(1);
        addressBookSeivice.updateById(addressBook);



        return R.success(addressBook);
    }

    /**
     * 根据id查询对应的地址信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<AddressBook> get(@PathVariable Long id){
        AddressBook addressBook = addressBookSeivice.getById(id);
        return R.success(addressBook);
    }
    /**
     * 修改地址
     * @param addressBook
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody AddressBook addressBook){
        log.info("需要修改地址的id:{}",addressBook);

        addressBookSeivice.updateById(addressBook);



        return R.success("地址修改成功");
    }

    @DeleteMapping
    public R<String> delete(Long ids){
        log.info("ids:{}",ids);
        if (ids != null){
            addressBookSeivice.removeById(ids);
        }

        return R.success("地址删除成功");
    }


    @GetMapping("/default")
    public R<AddressBook> defaultAddress(){
        log.info("查询默认地址");
        Long currentId = BaseContext.getCurrentId();

        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId,currentId);
        queryWrapper.eq(AddressBook::getIsDefault, 1);

        AddressBook bookSeiviceOne = addressBookSeivice.getOne(queryWrapper);

        return R.success(bookSeiviceOne);
    }




}
