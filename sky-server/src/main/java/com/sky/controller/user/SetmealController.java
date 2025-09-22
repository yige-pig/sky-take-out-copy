package com.sky.controller.user;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.sky.constant.StatusConstant;
import com.sky.entity.Setmeal;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@Api(tags = "C端-套餐浏览接口")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    //private static final String keyPrefix = "SETMEAL_";

    /**
     * 条件查询
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询套餐")
    public Result<List<Setmeal>> list(Long categoryId) {
       /* List<String> list = stringRedisTemplate.opsForList().
                range(keyPrefix + categoryId, 0, -1);
        List<Setmeal> setmeals = new ArrayList<>();
        if(!CollUtil.isEmpty(list)){
            for (String str : list) {
                setmeals.add(JSONUtil.toBean(str, Setmeal.class));
            }
            return Result.success(setmeals);
        }*/

        Setmeal setmeal = new Setmeal();
        setmeal.setCategoryId(categoryId);
        setmeal.setStatus(StatusConstant.ENABLE);

        List<Setmeal> list = setmealService.list(setmeal);
        return Result.success(list);

        /*setmeals = setmealService.list(setmeal);
        list = new ArrayList<>();
        for (Setmeal s : setmeals) {
            list.add(JSONUtil.toJsonStr(s));
        }
        stringRedisTemplate.opsForList()
                .leftPushAll(keyPrefix+categoryId, list);

        return Result.success(setmeals);*/
    }

    /**
     * 根据套餐id查询包含的菜品列表
     *
     * @param id
     * @return
     */
    @GetMapping("/dish/{id}")
    @ApiOperation("根据套餐id查询包含的菜品列表")
    public Result<List<DishItemVO>> dishList(@PathVariable("id") Long id) {
        List<DishItemVO> list = setmealService.getDishItemById(id);
        return Result.success(list);
    }
}