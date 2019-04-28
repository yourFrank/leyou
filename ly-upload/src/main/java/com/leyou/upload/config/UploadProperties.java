package com.leyou.upload.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author YuTian
 * @date 2019/4/4 10:06
 */

@Data
@ConfigurationProperties(prefix = "ly.upload")
public class UploadProperties {
    private List<String> allowTypes;
    private String baseurl;
}
