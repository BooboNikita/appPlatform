-- 为app_info表添加弹窗控制字段
use app_platform;

-- 添加弹窗控制字段
ALTER TABLE app_info 
ADD COLUMN showUpdatePopup BOOLEAN DEFAULT FALSE COMMENT '是否显示更新弹窗，false=不显示，true=显示';

-- 添加强制更新字段
ALTER TABLE app_info 
ADD COLUMN forceUpdate BOOLEAN DEFAULT FALSE COMMENT '是否强制更新，false=非强制，true=强制';

-- 为现有数据设置默认值（可选：根据需要设置某些版本默认显示弹窗）
-- UPDATE app_info SET showUpdatePopup = FALSE WHERE 1=1;
-- UPDATE app_info SET forceUpdate = FALSE WHERE 1=1;
