package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.upload.config.UploadProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * @author YuTian
 * @date 2019/4/4 9:59
 */
@Slf4j
@Service
@EnableConfigurationProperties(UploadProperties.class)
public class UploadService {
    @Autowired
    private FastFileStorageClient storageClient;
    @Autowired
    private UploadProperties prop;


    public String uploadImage(MultipartFile file) {

        try {
            //检验文件类型
            String contentType=file.getContentType();
            if(!prop.getAllowTypes().contains(contentType)){
                System.out.println(contentType);
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }
            //检验文件内容
            BufferedImage image=ImageIO.read(file.getInputStream());
            if(image==null){
                System.out.println(image);
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }
            //获取文件后缀
            String suffix = StringUtils.substringAfterLast(file.getOriginalFilename(), ".");
            //通过FDFS方法上传缩略图
            StorePath storePath = storageClient.uploadImageAndCrtThumbImage(file.getInputStream(), file.getSize(), suffix, null);
            // FullPath带分组的路径
            return prop.getBaseurl() +storePath.getFullPath();
        } catch (IOException e) {
          log.error("上传文件失败");
          throw  new LyException(ExceptionEnum.UPLOAD_FILE_ERROR);
        }


    }


}
