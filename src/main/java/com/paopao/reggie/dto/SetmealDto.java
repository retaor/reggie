package com.paopao.reggie.dto;

import com.paopao.reggie.entity.Setmeal;
import com.paopao.reggie.entity.SetmealDish;
import lombok.Data;

import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
