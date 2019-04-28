package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author YuTian
 * @date 2019/4/6 20:34
 */
@Service
public class SpecificationService {
    @Autowired
    private SpecGroupMapper specGroupMapper;
    @Autowired
    private SpecParamMapper specParamMapper;


    //参数分组列表查询
    public List<SpecGroup> getSpecGroupByCid(Long cid) {
        SpecGroup specGroup=new SpecGroup();
        specGroup.setCid(cid);

        List<SpecGroup> specGroups = specGroupMapper.select(specGroup);
        if(CollectionUtils.isEmpty(specGroups)){
            throw  new LyException(ExceptionEnum.SPECGROUP_NOT_FOUND);
        }
        return specGroups;

    }

    //参数分组编辑
    public void updateSpecGroupById(SpecGroup specGroup) {
        specGroupMapper.updateByPrimaryKey(specGroup);
    }

    public void saveSpecGroup(SpecGroup specGroup) {
        specGroupMapper.insert(specGroup);
    }

    public void deleteSpecGroup(long gid) {
        specGroupMapper.deleteByPrimaryKey(gid);
    }

    public List<SpecParam> querySpecParamByGroupId(Long gid, Long cid,  Boolean searching) {
        SpecParam specParam=new SpecParam();
        specParam.setCid(cid);
        specParam.setSearching(searching);
        specParam.setGroupId(gid);
        List<SpecParam> list = specParamMapper.select(specParam);
        if(CollectionUtils.isEmpty(list)){
            throw  new LyException(ExceptionEnum.SPECPARAM_NOT_FOUND);
        }
        return list;

    }

    public void savaSpecParam(SpecParam specParam) {
        specParamMapper.insert(specParam);
    }

    public void updateSpecParam(SpecParam specParam) {
        specParamMapper.updateByPrimaryKey(specParam);
    }

    public void deleteSpecParam(long pid) {
        specParamMapper.deleteByPrimaryKey(pid);
    }

    /**
     * 获取spec组和参数
     * @param cid
     * @return
     */
    public List<SpecGroup> querySpecsByCid(Long cid) {
        List<SpecGroup> specGroups = getSpecGroupByCid(cid);
        List<SpecParam> specParams = querySpecParamByGroupId(null, cid, null);
        //将specParams塞到specGroup，这里为了避免双重for循环，使用一个Map
        Map<Long,List<SpecParam>> map=new HashMap<>();
        for (SpecParam specParam : specParams) {
            if (!map.containsKey(specParam.getGroupId())){
                map.put(specParam.getGroupId(),new ArrayList<>());
            }
            map.get(specParam.getGroupId()).add(specParam);
        }
        //在map中找到对应的参数塞到组中
        for (SpecGroup specGroup : specGroups) {
            specGroup.setParams(map.get(specGroup.getId()));
        }
        return specGroups;

    }
}
