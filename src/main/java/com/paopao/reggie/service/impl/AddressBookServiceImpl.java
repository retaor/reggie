package com.paopao.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.paopao.reggie.entity.AddressBook;
import com.paopao.reggie.mapper.AddressBookMapper;
import com.paopao.reggie.service.AddressBookSeivice;
import org.springframework.stereotype.Service;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookSeivice {

}
