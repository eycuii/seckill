package com.ey.seckill.controller;

import com.ey.seckill.entities.Goods;
import com.ey.seckill.service.IGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
}
