package com.ey.seckill.controller;

import com.ey.seckill.entities.Goods;
import com.ey.seckill.service.IGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private IGoodsService goodsService;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Goods get(@PathVariable("id") int id) {
        Goods goods = goodsService.getById(id);
        return goods;
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public int add(@RequestParam String name, @RequestParam int count) {
        int result = 0;
        try {
            Goods goods = new Goods();
            goods.setName(name);
            goods.setCount(count);
            result = goodsService.add(goods);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public int update(@PathVariable("id") int id, @RequestParam int count) {
        int result = 0;
        try {
            Goods goods = new Goods();
            goods.setId(id);
            goods.setCount(count);
            result = goodsService.update(goods);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
