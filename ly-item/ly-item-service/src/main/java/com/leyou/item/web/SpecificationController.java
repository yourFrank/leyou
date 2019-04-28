package com.leyou.item.web;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author YuTian
 * @date 2019/4/6 19:14
 */
@Controller
@RequestMapping("spec")
public class SpecificationController {

    @Autowired
    private SpecificationService specificationService;

    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> getSpecGroupByCid(@PathVariable("cid")Long cid){


        return ResponseEntity.ok(specificationService.getSpecGroupByCid(cid));

    }


    //修改参数分组
    @PutMapping("group")
    public ResponseEntity<Void> updateSpecGroupById(SpecGroup specGroup){
        specificationService.updateSpecGroupById(specGroup);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }
    @PostMapping("group")
    public ResponseEntity<Void> saveSpecGroup(SpecGroup specGroup){
        specificationService.saveSpecGroup(specGroup);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }
    @DeleteMapping("group/{gid}")
    public ResponseEntity<Void> deleteSpecGroup(@PathVariable("gid")long gid){
        specificationService.deleteSpecGroup(gid);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> querySpecParamByGroupId(
            @RequestParam(value = "gid" ,required = false) Long gid,
            @RequestParam(value = "cid" ,required = false) Long cid,
            @RequestParam(value = "searching" ,required = false) Boolean searching
            ){
      return ResponseEntity.ok(specificationService.querySpecParamByGroupId(gid,cid,searching)) ;
    }
    //---------------------------------------------------------------------------
    //新增参数
    @PostMapping("param")
    public ResponseEntity<Void> savaSpecParam(SpecParam specParam){
        specificationService.savaSpecParam(specParam);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }
    //修改参数
    @PutMapping("param")
    public ResponseEntity<Void> updateSpecParam(SpecParam specParam){
        specificationService.updateSpecParam(specParam);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    //删除参数
    @DeleteMapping("param/{pid}")
    public ResponseEntity<Void> deleteSpecParam(@PathVariable("pid")long pid){
        specificationService.deleteSpecParam(pid);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    /**
     * 查询spec组
     * @param cid
     * @return
     */
    @GetMapping("group")
    public ResponseEntity<List<SpecGroup>> querySpecsByCid(@RequestParam("cid") Long cid){

        return ResponseEntity.ok(specificationService.querySpecsByCid(cid));
    }

}
