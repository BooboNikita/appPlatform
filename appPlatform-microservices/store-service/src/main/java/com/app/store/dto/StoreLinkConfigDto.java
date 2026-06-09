package com.app.store.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 应用商店链接配置DTO
 */
@Data
public class StoreLinkConfigDto {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 设备品牌
     */
    @NotBlank(message = "设备品牌不能为空")
    private String deviceBrand;

    /**
     * 品牌别名（多个别名用逗号分隔，如：redmi,mi,小米）
     */
    private String brandAliases;

    /**
     * 应用商店链接模板
     */
    @NotBlank(message = "链接模板不能为空")
    private String linkTemplate;

    /**
     * 是否启用
     */
    @NotNull(message = "启用状态不能为空")
    private Integer enabled;

    /**
     * 排序权重
     */
    private Integer sortOrder;

    /**
     * 备注
     */
    private String remark;
}
