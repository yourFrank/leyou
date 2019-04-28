package com.leyou.item.api;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author YuTian
 * @date 2019/4/13 17:40
 */
public interface SpecificationApi {
    @GetMapping("spec/params")
    List<SpecParam> querySpecParamByGroupId(
            @RequestParam(value = "gid" ,required = false) Long gid,
            @RequestParam(value = "cid" ,required = false) Long cid,
            @RequestParam(value = "searching" ,required = false) Boolean searching
    );

    @GetMapping("spec/group")
    List<SpecGroup> querySpecsByCid(@RequestParam("cid") Long cid);
}
