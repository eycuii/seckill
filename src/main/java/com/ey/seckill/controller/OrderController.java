package com.ey.seckill.controller;

import com.ey.seckill.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    @RequestMapping(value = "", method = RequestMethod.POST)
    public int add(@RequestParam int goodsId, @RequestParam int count) {
        int result = 0;
        try {
            result = orderService.order(goodsId, count);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
