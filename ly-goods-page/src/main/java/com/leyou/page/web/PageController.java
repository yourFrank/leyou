package com.leyou.page.web;

import com.leyou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * @author YuTian
 * @date 2019/4/20 11:44
 */
@Controller
public class PageController {

    @Autowired
    private PageService pageService;
    @GetMapping("item/{id}.html")
    public String toItemPage(@PathVariable("id") Long id, Model model){
        Map<String,Object> map=pageService.loadModel(id);
        model.addAllAttributes(map);
        // 判断是否需要生成新的页面
        if(!this.pageService.exists(id)){
            this.pageService.syncCreateHtml(id);
        }
        return "item";
    }
}
