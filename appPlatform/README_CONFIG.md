# 应用平台配置管理功能说明

## 功能概述

本次更新为应用平台增加了两个重要的配置管理功能：

1. **应用商店链接配置管理** - 支持动态配置各品牌应用商店的下载链接
2. **版本更新弹窗控制管理** - 每个应用版本可独立控制弹窗显示和强制更新

## 数据库表结构

### 1. 应用商店链接配置表 (store_link_config)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| device_brand | VARCHAR(50) | 设备品牌（如：xiaomi、huawei等） |
| link_template | VARCHAR(500) | 链接模板（支持{packageName}占位符） |
| enabled | TINYINT | 是否启用（1=启用，0=禁用） |
| sort_order | INT | 排序权重 |
| remark | VARCHAR(200) | 备注 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 2. 应用信息表 (app_info) - 新增字段

版本更新弹窗控制直接在 `app_info` 表中添加了以下字段：

| 字段 | 类型 | 说明 | 默认值 |
|------|------|------|--------|
| show_update_popup | BOOLEAN | 是否显示更新弹窗 | false |
| force_update | BOOLEAN | 是否强制更新 | false |

## API接口

### 应用商店链接配置管理

#### 基础路径：`/api/store-link-config`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/enabled` | 获取所有启用的配置 |
| GET | `/brand/{deviceBrand}` | 根据设备品牌查询配置 |
| GET | `/` | 获取所有配置 |
| GET | `/{id}` | 根据ID查询配置 |
| POST | `/` | 创建配置 |
| PUT | `/{id}` | 更新配置 |
| DELETE | `/{id}` | 删除配置 |
| PUT | `/{id}/enabled` | 启用/禁用配置 |

#### 请求示例

**创建配置：**
```json
POST /api/store-link-config
{
    "deviceBrand": "xiaomi",
    "linkTemplate": "market://details?id={packageName}",
    "enabled": 1,
    "sortOrder": 1,
    "remark": "小米应用商店"
}
```

### 版本更新弹窗配置管理

#### 基础路径：`/api-app`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/app/{id}/popup-control` | 获取指定版本的弹窗控制状态 |
| POST | `/app/{id}/popup-control` | 设置指定版本的弹窗控制 |

#### 请求示例

**获取版本弹窗控制状态：**
```bash
GET /api-app/app/123/popup-control
```
响应：
```json
{
    "code": 200,
    "data": {
        "id": 123,
        "appName": "测试应用",
        "version": "1.2.0",
        "showUpdatePopup": false,
        "forceUpdate": false
    }
}
```

**设置版本弹窗控制：**
```bash
POST /api-app/app/123/popup-control?showUpdatePopup=true&forceUpdate=false
```

**设置强制更新：**
```bash
POST /api-app/app/123/popup-control?showUpdatePopup=true&forceUpdate=true
```

## 部署步骤

### 1. 执行数据库脚本

```bash
# 执行商店链接配置表脚本
mysql -u username -p database_name < src/main/resources/sql/store_link_config.sql

# 执行app_info表弹窗控制字段脚本
mysql -u username -p database_name < src/main/resources/sql/add_popup_control_to_app_info.sql
```

### 2. 重启应用

确保新的Mapper XML文件被正确加载：
- `src/main/resources/mapper/StoreLinkConfigMapper.xml`

## 使用说明

### 商店链接配置

1. **链接模板支持占位符**：使用 `{packageName}` 作为包名占位符
2. **默认配置**：系统会查找 `device_brand = 'default'` 的配置作为兜底
3. **优先级**：具体品牌配置 > 默认配置

### 弹窗控制配置

1. **版本级控制**：每个应用版本可独立设置是否显示弹窗和强制更新
2. **灵活配置**：可以针对不同版本设置不同的弹窗策略
3. **实时生效**：配置修改后立即生效，无需重启应用
4. **精细管理**：支持只开启弹窗不强制更新，或同时开启强制更新

## 注意事项

1. **数据库连接**：确保应用有权限访问新创建的表
2. **缓存考虑**：配置变更后可能需要清理相关缓存
3. **向后兼容**：原有的硬编码配置方式已被数据库配置替代
4. **权限控制**：建议为配置管理接口添加适当的权限验证

## 示例配置

### 商店链接默认配置

系统已预置以下品牌的应用商店配置：
- 小米：`market://details?id={packageName}`
- 华为：`appmarket://details?id={packageName}`
- 荣耀：`honormarket://details?id={packageName}`
- OPPO：`oppomarket://details?id={packageName}`
- vivo：`vivomarket://details?id={packageName}`
- 魅族：`meizu://details?id={packageName}`
- 默认：`https://play.google.com/store/apps/details?id={packageName}`

### 弹窗控制默认配置

系统默认所有版本都不显示更新弹窗且不强制更新：
- `show_update_popup`: false（默认不显示弹窗）
- `force_update`: false（默认非强制更新）

可以通过API接口为特定版本设置弹窗控制：
- 为版本123开启弹窗：`POST /api-app/app/123/popup-control?showUpdatePopup=true`
- 为版本123开启强制更新：`POST /api-app/app/123/popup-control?showUpdatePopup=true&forceUpdate=true`

### 典型使用场景

1. **正常更新**：只开启弹窗，不强制更新
   ```bash
   POST /api-app/app/123/popup-control?showUpdatePopup=true&forceUpdate=false
   ```

2. **强制更新**：同时开启弹窗和强制更新
   ```bash
   POST /api-app/app/123/popup-control?showUpdatePopup=true&forceUpdate=true
   ```

3. **静默更新**：关闭弹窗，不提示用户
   ```bash
   POST /api-app/app/123/popup-control?showUpdatePopup=false&forceUpdate=false
   ```
