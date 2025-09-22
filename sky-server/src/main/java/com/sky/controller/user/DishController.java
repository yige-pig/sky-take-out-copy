package com.sky.controller.user;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.support.json.JSONUtils;
import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String keyPrefix = "DISH_";

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        List<String> list = stringRedisTemplate.
                opsForList().range(keyPrefix+categoryId, 0, -1);
        List<DishVO> dishVOS = new ArrayList<>();
        if(!CollUtil.isEmpty(list)){
            for (String str : list) {
                dishVOS.add(JSONUtil.toBean(str, DishVO.class));
            }
            return Result.success(dishVOS);
        }

        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品

        dishVOS = dishService.listWithFlavor(dish);
        list = new ArrayList<>();
        for (DishVO dishVO : dishVOS) {
            list.add(JSONUtil.toJsonStr(dishVO));
        }
        stringRedisTemplate.opsForList().leftPushAll(keyPrefix+categoryId, list);

        return Result.success(dishVOS);
    }

    /**
     * 菜品起售停售
     *
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售停售")
    public Result<String> startOrStop(@PathVariable Integer status, Long id) {
        dishService.startOrStop(status, id);
        return Result.success();
    }

}