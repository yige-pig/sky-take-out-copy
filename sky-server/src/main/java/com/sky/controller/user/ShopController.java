package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.ShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("userShopController")
@RequestMapping("/user/shop")
@Api(tags = "店铺相关接口")
public class ShopController {

    @Autowired
    private ShopService shopService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public static final String key = "SHOP_STATUS_KEY";

    /**
     * 查询营业状态
     * @return
     */
    @GetMapping("/status")
    @ApiOperation("查询营业状态")
    public Result getStatus(){
        String status = stringRedisTemplate.opsForValue().get(key);
        return Result.success(Integer.valueOf(status));
    }
}
