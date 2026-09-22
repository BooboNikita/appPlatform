package com.app.appplatform.service.impl;

import com.app.appplatform.entity.DynamicConfig;
import com.app.appplatform.entity.DynamicConfigHistory;
import com.app.appplatform.entity.DynamicConfigItem;
import com.app.appplatform.entity.DynamicConfigItemGrayUser;
import com.app.appplatform.entity.DynamicConfigItemHistory;
import com.app.appplatform.mapper.primary.DynamicConfigHistoryMapper;
import com.app.appplatform.mapper.primary.DynamicConfigItemGrayUserMapper;
import com.app.appplatform.mapper.primary.DynamicConfigItemHistoryMapper;
import com.app.appplatform.mapper.primary.DynamicConfigItemMapper;
import com.app.appplatform.mapper.primary.DynamicConfigMapper;
import com.app.appplatform.mapper.secondary.UserSecondaryMapper;
import com.app.appplatform.service.DynamicConfigService;
import com.app.appplatform.util.GrayReleaseUtil;
import com.app.appplatform.util.JsonUtil;
import com.app.appplatform.util.VersionUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DynamicConfigServiceImpl implements DynamicConfigService {

    private static final String LIST_TYPE_WHITE = "WHITE";
    private static final String LIST_TYPE_BLACK = "BLACK";

    @Autowired
    private DynamicConfigMapper dynamicConfigMapper;

    @Autowired
    private DynamicConfigHistoryMapper historyMapper;

    @Autowired
    private DynamicConfigItemMapper itemMapper;

    @Autowired
    private DynamicConfigItemGrayUserMapper grayUserMapper;

    @Autowired
    private DynamicConfigItemHistoryMapper itemHistoryMapper;

    @Autowired
    private UserSecondaryMapper userSecondaryMapper;

    // ==================== 上传 / 更新（整包 JSON 拆解入库） ====================

    @Override
    public DynamicConfig uploadConfig(MultipartFile file, String versionRange, String env, String remark) throws Exception {
        String content = file == null ? null : new String(file.getBytes(), StandardCharsets.UTF_8);
        Map<String, Map<String, Object>> parsed = parseConfigContent(content);
        return doUpload(parsed, versionRange, env, remark, "system");
    }

    private DynamicConfig doUpload(Map<String, Map<String, Object>> parsed, String versionRange, String env,
                                   String remark, String operator) throws Exception {
        DynamicConfig config = dynamicConfigMapper.findByEnvAndVersionRange(env, versionRange);
        boolean created = config == null;
        if (created) {
            config = new DynamicConfig();
            config.setVersionRange(versionRange);
            config.setEnv(env);
            config.setRemark(remark);
            dynamicConfigMapper.insert(config);
        } else {
            if (remark != null) {
                config.setRemark(remark);
                dynamicConfigMapper.update(config);
            }
        }
        upsertItems(config, parsed, operator);
        saveHistory(config, created ? "CREATE" : "UPDATE", operator);
        return config;
    }

    /**
     * 解析并校验整包 JSON：兼容 {metadata, configs} 包装（取 configs），必须为 { 域: { 配置项: 值 } } 两层结构
     */
    @SuppressWarnings("unchecked")
    private Map<String, Map<String, Object>> parseConfigContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("配置内容为空");
        }
        Object obj = JsonUtil.toObject(content, Object.class);
        if (!(obj instanceof Map)) {
            throw new RuntimeException("配置内容格式错误：必须为 JSON 对象");
        }
        Map<String, Object> root = (Map<String, Object>) obj;
        Object configsObj = root.containsKey("configs") ? root.get("configs") : root;
        if (!(configsObj instanceof Map)) {
            throw new RuntimeException("配置内容格式错误：必须为 { 域: { 配置项: 值 } } 两层结构");
        }
        Map<String, Object> configs = (Map<String, Object>) configsObj;
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> domainEntry : configs.entrySet()) {
            String domain = domainEntry.getKey();
            if (!(domainEntry.getValue() instanceof Map)) {
                throw new RuntimeException("配置内容格式错误：域 " + domain + " 下必须是对象（{ 配置项: 值 }）");
            }
            Map<String, Object> items = (Map<String, Object>) domainEntry.getValue();
            Map<String, Object> domainItems = new LinkedHashMap<>();
            for (Map.Entry<String, Object> itemEntry : items.entrySet()) {
                domainItems.put(itemEntry.getKey(), itemEntry.getValue());
            }
            result.put(domain, domainItems);
        }
        return result;
    }

    /**
     * 按业务键 (config_id, domain, item_key) UPSERT：
     * 已存在仅更新值/类型（保留灰度开关、百分比与名单）；不存在则新建；本次未出现的旧 key 删除并级联清名单
     */
    private void upsertItems(DynamicConfig config, Map<String, Map<String, Object>> parsed, String operator) throws Exception {
        List<DynamicConfigItem> existingItems = itemMapper.findByConfigId(config.getId());
        // 整包上传只管理「继承配置版本范围」的配置项；带独立版本范围的项不受整包影响
        List<DynamicConfigItem> inheritedItems = new ArrayList<>();
        for (DynamicConfigItem it : existingItems) {
            if (config.getVersionRange().equals(it.getVersionRange())) {
                inheritedItems.add(it);
            }
        }
        Map<String, DynamicConfigItem> existingByKey = new HashMap<>();
        for (DynamicConfigItem it : inheritedItems) {
            existingByKey.put(it.getDomain() + "." + it.getItemKey(), it);
        }

        Set<String> newKeys = new HashSet<>();
        for (Map.Entry<String, Map<String, Object>> domainEntry : parsed.entrySet()) {
            for (Map.Entry<String, Object> itemEntry : domainEntry.getValue().entrySet()) {
                String domain = domainEntry.getKey();
                String itemKey = itemEntry.getKey();
                String key = domain + "." + itemKey;
                newKeys.add(key);

                String itemValue = JsonUtil.toJson(itemEntry.getValue());
                String valueType = resolveValueType(itemEntry.getValue());
                DynamicConfigItem existing = existingByKey.get(key);
                if (existing != null) {
                    if (!Objects.equals(existing.getItemValue(), itemValue)
                            || !Objects.equals(existing.getValueType(), valueType)) {
                        existing.setItemValue(itemValue);
                        existing.setValueType(valueType);
                        itemMapper.update(existing);
                        saveItemHistory(existing, "UPDATE", operator);
                    }
                } else {
                    DynamicConfigItem item = new DynamicConfigItem();
                    item.setConfigId(config.getId());
                    item.setVersionRange(config.getVersionRange());
                    item.setEnv(config.getEnv());
                    item.setDomain(domain);
                    item.setItemKey(itemKey);
                    item.setItemValue(itemValue);
                    item.setValueType(valueType);
                    item.setEnabled(1);
                    item.setGrayEnabled(0);
                    item.setGrayPercent(0);
                    itemMapper.insert(item);
                    saveItemHistory(item, "CREATE", operator);
                }
            }
        }

        // 删除本次未出现的旧 key（先级联删名单，再写 DELETE 历史）；只针对继承版本范围的项
        for (DynamicConfigItem old : inheritedItems) {
            if (!newKeys.contains(old.getDomain() + "." + old.getItemKey())) {
                grayUserMapper.deleteByItemId(old.getId());
                itemMapper.deleteById(old.getId());
                saveItemHistory(old, "DELETE", operator);
            }
        }
    }

    private String resolveValueType(Object value) {
        if (value instanceof Boolean) return "BOOLEAN";
        if (value instanceof Number) return "NUMBER";
        if (value instanceof String) return "STRING";
        return "JSON";
    }

    // ==================== 基础查询 / 删除 / 更新 ====================

    @Override
    public DynamicConfig getConfigById(Long id) {
        return dynamicConfigMapper.findById(id);
    }

    @Override
    public List<DynamicConfig> getAllConfigs(String env) {
        return dynamicConfigMapper.findAll(env);
    }

    @Override
    public DynamicConfig getLatestConfigByVersion(String version, String env) {
        List<DynamicConfig> matchedConfigs = dynamicConfigMapper.findByEnv(env);
        return matchedConfigs.stream()
                .filter(config -> VersionUtil.isMatch(version, config.getVersionRange()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void deleteConfig(Long id) throws Exception {
        DynamicConfig config = dynamicConfigMapper.findById(id);
        if (config == null) {
            return;
        }
        // 删除前整包快照留档（含全部配置项与灰度名单，随 dynamic_config_history 持久保留）
        saveHistory(config, "DELETE", "system");

        List<DynamicConfigItem> items = itemMapper.findByConfigId(id);
        List<Long> itemIds = items.stream().map(DynamicConfigItem::getId).collect(Collectors.toList());
        if (!itemIds.isEmpty()) {
            grayUserMapper.deleteByItemIds(itemIds);
        }
        itemMapper.deleteByConfigId(id);
        itemHistoryMapper.deleteByConfigId(id);
        dynamicConfigMapper.deleteById(id);
    }

    @Override
    public DynamicConfig updateConfig(Long id, MultipartFile file, String versionRange, String env, String remark) throws Exception {
        DynamicConfig existingConfig = dynamicConfigMapper.findById(id);
        if (existingConfig == null) {
            throw new RuntimeException("配置不存在");
        }

        // 1. 元数据变更（唯一键冲突检查）
        boolean metaChanged = false;
        if (versionRange != null && !versionRange.equals(existingConfig.getVersionRange())) {
            DynamicConfig conflict = dynamicConfigMapper.findByEnvAndVersionRange(existingConfig.getEnv(), versionRange);
            if (conflict != null && !conflict.getId().equals(id)) {
                throw new RuntimeException("该版本范围与环境已存在配置");
            }
            existingConfig.setVersionRange(versionRange);
            metaChanged = true;
        }
        if (env != null && !env.equals(existingConfig.getEnv())) {
            DynamicConfig conflict = dynamicConfigMapper.findByEnvAndVersionRange(env, existingConfig.getVersionRange());
            if (conflict != null && !conflict.getId().equals(id)) {
                throw new RuntimeException("该版本范围与环境已存在配置");
            }
            existingConfig.setEnv(env);
            metaChanged = true;
        }
        if (metaChanged) {
            dynamicConfigMapper.update(existingConfig);
            itemMapper.updateMetaByConfigId(id, existingConfig.getVersionRange(), existingConfig.getEnv());
        }
        if (remark != null && !Objects.equals(remark, existingConfig.getRemark())) {
            existingConfig.setRemark(remark);
            dynamicConfigMapper.update(existingConfig);
        }

        // 2. 文件内容变更（复用拆解 UPSERT，保留已有灰度配置）
        if (file != null && !file.isEmpty()) {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            Map<String, Map<String, Object>> parsed = parseConfigContent(content);
            upsertItems(existingConfig, parsed, "system");
        }

        saveHistory(existingConfig, "UPDATE", "system");
        return existingConfig;
    }

    // ==================== 配置项管理 ====================

    @Override
    public List<DynamicConfigItem> getItems(String env, String versionRange, String domain, String keyword) {
        return itemMapper.findPage(env, versionRange, domain, keyword);
    }

    @Override
    public DynamicConfigItem addItem(DynamicConfigItem req, String operator) throws Exception {
        // 按环境 + 版本范围定位锚点配置；不存在则自动创建，无需先上传整包 JSON
        String env = req.getEnv() == null ? "" : req.getEnv().trim();
        String anchorRange = req.getVersionRange() == null ? "" : req.getVersionRange().trim();
        if (env.isEmpty()) {
            throw new RuntimeException("请选择环境");
        }
        if (anchorRange.isEmpty()) {
            throw new RuntimeException("请填写版本范围");
        }
        DynamicConfig config = dynamicConfigMapper.findByEnvAndVersionRange(env, anchorRange);
        if (config == null) {
            config = new DynamicConfig();
            config.setEnv(env);
            config.setVersionRange(anchorRange);
            dynamicConfigMapper.insert(config);
            saveHistory(config, "CREATE", operator);
        }
        String domain = req.getDomain() == null ? "" : req.getDomain().trim();
        String itemKey = req.getItemKey() == null ? "" : req.getItemKey().trim();
        if (domain.isEmpty() || itemKey.isEmpty()) {
            throw new RuntimeException("所属域与配置项名称不能为空");
        }
        if (req.getItemValue() == null || req.getItemValue().trim().isEmpty()) {
            throw new RuntimeException("配置项值不能为空");
        }
        Object parsed = JsonUtil.toObject(req.getItemValue(), Object.class);
        if (parsed == null) {
            throw new RuntimeException("配置项值必须是合法 JSON");
        }
        // 同一配置下允许同名配置项按不同版本范围各一条（后续可在编辑抽屉改为独立版本范围）
        if (itemMapper.findByConfigIdKeyAndVersion(config.getId(), domain, itemKey, anchorRange) != null) {
            throw new RuntimeException("该配置下已存在同名同版本范围的配置项：" + domain + "." + itemKey + " (" + anchorRange + ")");
        }

        DynamicConfigItem item = new DynamicConfigItem();
        item.setConfigId(config.getId());
        item.setVersionRange(anchorRange);
        item.setEnv(config.getEnv());
        item.setDomain(domain);
        item.setItemKey(itemKey);
        item.setItemValue(JsonUtil.toJson(parsed));
        item.setValueType(resolveValueType(parsed));
        item.setEnabled(1);
        item.setGrayEnabled(0);
        item.setGrayPercent(0);
        item.setRemark(req.getRemark());
        itemMapper.insert(item);
        saveItemHistory(item, "CREATE", operator);
        return item;
    }

    @Override
    public DynamicConfigItem updateItem(Long itemId, DynamicConfigItem req, String operator) throws Exception {
        DynamicConfigItem item = itemMapper.findById(itemId);
        if (item == null) {
            throw new RuntimeException("配置项不存在");
        }
        if (req.getItemValue() != null) {
            Object parsed = JsonUtil.toObject(req.getItemValue(), Object.class);
            if (parsed == null) {
                throw new RuntimeException("配置项值必须是合法 JSON");
            }
            item.setItemValue(JsonUtil.toJson(parsed));
            item.setValueType(resolveValueType(parsed));
        }
        if (req.getEnabled() != null) {
            item.setEnabled(req.getEnabled());
        }
        if (req.getGrayEnabled() != null) {
            item.setGrayEnabled(req.getGrayEnabled());
        }
        if (req.getGrayPercent() != null) {
            item.setGrayPercent(GrayReleaseUtil.clampPercent(req.getGrayPercent()));
        }
        if (req.getVersionRange() != null) {
            String vr = req.getVersionRange().trim();
            if (vr.isEmpty()) {
                // 清空则恢复继承所属配置的版本范围
                DynamicConfig cfg = dynamicConfigMapper.findById(item.getConfigId());
                if (cfg == null) {
                    throw new RuntimeException("所属配置不存在");
                }
                vr = cfg.getVersionRange();
            }
            if (!vr.equals(item.getVersionRange())) {
                DynamicConfigItem conflict = itemMapper.findByConfigIdKeyAndVersion(
                        item.getConfigId(), item.getDomain(), item.getItemKey(), vr);
                if (conflict != null && !conflict.getId().equals(item.getId())) {
                    throw new RuntimeException("已存在同名同版本范围的配置项："
                            + item.getDomain() + "." + item.getItemKey() + " (" + vr + ")");
                }
                item.setVersionRange(vr);
            }
        }
        if (req.getRemark() != null) {
            item.setRemark(req.getRemark());
        }
        itemMapper.update(item);
        saveItemHistory(item, "UPDATE", operator);
        return item;
    }

    @Override
    public void deleteItem(Long itemId, String operator) throws Exception {
        DynamicConfigItem item = itemMapper.findById(itemId);
        if (item == null) {
            throw new RuntimeException("配置项不存在");
        }
        grayUserMapper.deleteByItemId(itemId);
        itemMapper.deleteById(itemId);
        saveItemHistory(item, "DELETE", operator);
    }

    // ==================== 灰度名单 ====================

    @Override
    public List<DynamicConfigItemGrayUser> getGrayUsers(Long itemId, String listType, String keyword) {
        List<DynamicConfigItemGrayUser> list = grayUserMapper.findByItemId(itemId, listType, keyword);
        enrichGrayUserNicknames(list);
        return list;
    }

    /**
     * 为名单批量补充昵称（仅展示用）
     */
    private void enrichGrayUserNicknames(List<DynamicConfigItemGrayUser> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<String> usernames = new ArrayList<>();
        for (DynamicConfigItemGrayUser gu : list) {
            if (gu.getUsername() != null && !gu.getUsername().trim().isEmpty()) {
                usernames.add(gu.getUsername());
            }
        }
        if (usernames.isEmpty()) {
            return;
        }
        Map<String, String> nickMap = new HashMap<>();
        try {
            for (Map<String, Object> ui : userSecondaryMapper.findNicknamesByUsernames(new ArrayList<>(new LinkedHashSet<>(usernames)))) {
                if (ui != null && ui.get("username") != null && ui.get("nickname") != null) {
                    nickMap.put(String.valueOf(ui.get("username")), String.valueOf(ui.get("nickname")));
                }
            }
        } catch (Exception e) {
            log.warn("批量补充名单昵称失败: {}", e.getMessage());
            return;
        }
        for (DynamicConfigItemGrayUser gu : list) {
            gu.setNickname(nickMap.get(gu.getUsername()));
        }
    }

    @Override
    public int addGrayUsers(Long itemId, String listType, List<String> usernames, String operator) throws Exception {
        DynamicConfigItem item = itemMapper.findById(itemId);
        if (item == null) {
            throw new RuntimeException("配置项不存在");
        }
        if (!LIST_TYPE_WHITE.equals(listType) && !LIST_TYPE_BLACK.equals(listType)) {
            throw new RuntimeException("名单类型必须为 WHITE 或 BLACK");
        }
        if (usernames == null || usernames.isEmpty()) {
            return 0;
        }
        // 拆分、trim、去重、去空（支持逗号/中英文分号/空白符分隔）
        LinkedHashSet<String> distinct = new LinkedHashSet<>();
        for (String raw : usernames) {
            if (raw == null) continue;
            for (String u : raw.split("[,，;；\\s]+")) {
                if (u != null && !u.trim().isEmpty()) {
                    distinct.add(u.trim());
                }
            }
        }
        if (distinct.isEmpty()) {
            return 0;
        }
        // 支持输入用户名或昵称：先把昵称解析为用户名，仅保留真实存在的用户加入名单
        // token -> 解析后的用户名；用户名优先按 login_name 匹配，未命中再按 nick_name 反查
        List<String> distinctList = new ArrayList<>(distinct);
        Map<String, String> resolved = new HashMap<>();
        try {
            Set<String> matchedAsUsername = new HashSet<>();
            for (Map<String, Object> ui : userSecondaryMapper.findNicknamesByUsernames(distinctList)) {
                if (ui != null && ui.get("username") != null) {
                    matchedAsUsername.add(String.valueOf(ui.get("username")));
                    resolved.put(String.valueOf(ui.get("username")), String.valueOf(ui.get("username")));
                }
            }
            // 剩余未按用户名命中的 token，尝试按昵称反查
            List<String> nicknameCandidates = new ArrayList<>();
            for (String t : distinctList) {
                if (!matchedAsUsername.contains(t)) {
                    nicknameCandidates.add(t);
                }
            }
            if (!nicknameCandidates.isEmpty()) {
                for (Map<String, Object> ui : userSecondaryMapper.findUsernamesByNicknames(nicknameCandidates)) {
                    if (ui != null && ui.get("nickname") != null && ui.get("username") != null) {
                        resolved.put(String.valueOf(ui.get("nickname")), String.valueOf(ui.get("username")));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("批量校验名单用户存在性失败，按全部放行: {}", e.getMessage());
            // for (String t : distinctList) {
            //     resolved.put(t, t);
            // }
            throw new RuntimeException ( "用户存在性校验服务不可用，请稍后重试" );
        }
        Set<String> addedUsernames = new LinkedHashSet<>();
        String op = operator == null || operator.isEmpty() ? "system" : operator;
        List<DynamicConfigItemGrayUser> list = new ArrayList<>();
        for (String u : distinct) {
            String resolvedU = resolved.get(u);
            if (resolvedU == null || resolvedU.trim().isEmpty()) {
                continue;
            }
            if (!addedUsernames.add(resolvedU)) {
                continue; // 同名去重
            }
            DynamicConfigItemGrayUser gu = new DynamicConfigItemGrayUser();
            gu.setItemId(itemId);
            gu.setListType(listType);
            gu.setUsername(resolvedU);
            gu.setOperator(op);
            list.add(gu);
        }
        if (list.isEmpty()) {
            return 0;
        }
        int added = grayUserMapper.insertBatch(list);
        if (added > 0) {
            saveItemHistory(item, "UPDATE", op);
        }
        return added;
    }

    @Override
    public void deleteGrayUser(Long grayUserId, String operator) throws Exception {
        DynamicConfigItemGrayUser gu = grayUserMapper.findById(grayUserId);
        if (gu == null) {
            return;
        }
        grayUserMapper.deleteById(grayUserId);
        DynamicConfigItem item = itemMapper.findById(gu.getItemId());
        if (item != null) {
            saveItemHistory(item, "UPDATE", operator);
        }
    }

    @Override
    public void clearGrayUsers(Long itemId, String listType, String operator) throws Exception {
        DynamicConfigItem item = itemMapper.findById(itemId);
        if (item == null) {
            throw new RuntimeException("配置项不存在");
        }
        int deleted = grayUserMapper.deleteByItemIdAndType(itemId, listType);
        if (deleted > 0) {
            saveItemHistory(item, "UPDATE", operator);
        }
    }

    @Override
    public List<Long> findItemIdsByUsername(String username) {
        if (username == null || username.isEmpty()) {
            return Collections.emptyList();
        }
        return grayUserMapper.findItemIdsByUsername(username);
    }

    // ==================== 下发拼装 ====================

    @Override
    public Object matchConfigContent(String version, String env, String username) throws Exception {
        // 一次查询拉取该环境全部配置项（配置量小），按 item 自身版本范围匹配 + 同名覆盖：
        // 客户端版本可能同时命中多项（如 "1.0.0-2.0.0" 与 "1.9.0+"），由 item 级过滤决定是否下发；
        List<DynamicConfigItem> all = itemMapper.findByEnv(env); // 已按 config_id, id 升序
        Map<String, DynamicConfigItem> merged = new LinkedHashMap<>();
        for (DynamicConfigItem item : all) {
            if (VersionUtil.isMatch(version, item.getVersionRange())) {
                merged.put(item.getDomain() + "." + item.getItemKey(), item); // 后建 config 覆盖同名项
            }
        }
        if (merged.isEmpty()) {
            return null;
        }
        // metadata 仅用于展示，单次查询配置（不含 per-config 的 item 循环，消除 N+1）
        List<DynamicConfig> configs = dynamicConfigMapper.findByEnv(env);
        return buildContent(configs, new ArrayList<>(merged.values()), version, true, username);
    }

    @Override
    public Object getConfigContent(Long id, String env) throws Exception {
        DynamicConfig config = dynamicConfigMapper.findByIdAndEnv(id, env);
        if (config == null) {
            throw new RuntimeException("配置不存在或环境不匹配");
        }
        return buildContent(Collections.singletonList(config),
                itemMapper.findByConfigId(config.getId()), null, false, null);
    }

    /**
     * 拼装 {metadata, configs}；下发模式按配置项版本范围与灰度规则过滤；拼装后做格式校验再返回。
     * configs 仅用于 metadata 展示；下发已由调用方按 item 版本范围匹配好
     */
    private Object buildContent(List<DynamicConfig> configs, List<DynamicConfigItem> items,
                                String version, boolean filterForRelease, String username) throws Exception {
        Map<Long, Set<String>> whiteSets = new HashMap<>();
        Map<Long, Set<String>> blackSets = new HashMap<>();
        if (filterForRelease && !items.isEmpty()) {
            List<Long> itemIds = items.stream().map(DynamicConfigItem::getId).collect(Collectors.toList());
            List<DynamicConfigItemGrayUser> grayUsers = grayUserMapper.findByItemIds(itemIds);
            for (DynamicConfigItemGrayUser gu : grayUsers) {
                if (LIST_TYPE_WHITE.equals(gu.getListType())) {
                    whiteSets.computeIfAbsent(gu.getItemId(), k -> new HashSet<>()).add(gu.getUsername());
                } else if (LIST_TYPE_BLACK.equals(gu.getListType())) {
                    blackSets.computeIfAbsent(gu.getItemId(), k -> new HashSet<>()).add(gu.getUsername());
                }
            }
        }

        Map<String, Map<String, Object>> configMap = new LinkedHashMap<>();
        for (DynamicConfigItem item : items) {
            // 二级版本过滤：配置项版本范围与客户端版本不匹配则不下发（继承项必然匹配）
            if (filterForRelease && version != null
                    && !VersionUtil.isMatch(version, item.getVersionRange())) {
                continue;
            }
            if (filterForRelease && !GrayReleaseUtil.isReleased(item,
                    whiteSets.getOrDefault(item.getId(), Collections.emptySet()),
                    blackSets.getOrDefault(item.getId(), Collections.emptySet()),
                    username)) {
                continue;
            }
            Object value = parseItemValue(item);
            configMap.computeIfAbsent(item.getDomain(), k -> new LinkedHashMap<>()).put(item.getItemKey(), value);
        }

        DynamicConfig primary = configs.get(configs.size() - 1);
        String joinedRanges = configs.stream()
                .map(DynamicConfig::getVersionRange)
                .collect(Collectors.joining(","));
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("configId", configs.size() == 1 ? primary.getId() : null);
        metadata.put("versionRange", joinedRanges);
        metadata.put("publishTime", formatTime(primary.getUpdateTime() != null ? primary.getUpdateTime() : primary.getCreateTime()));
        metadata.put("env", primary.getEnv());
        metadata.put("remark", primary.getRemark());

        Map<String, Object> full = new LinkedHashMap<>();
        full.put("metadata", metadata);
        full.put("configs", configMap);

        // 拼装后格式校验
        String json = JsonUtil.toJson(full);
        Object validated = json == null ? null : JsonUtil.toObject(json, Object.class);
        if (validated == null) {
            throw new RuntimeException("配置拼装结果校验失败");
        }
        return validated;
    }

    private Object parseItemValue(DynamicConfigItem item) {
        Object v = JsonUtil.toObject(item.getItemValue(), Object.class);
        return v != null ? v : item.getItemValue();
    }

    // ==================== 整包历史快照 ====================

    /**
     * 整包快照：记录操作完成后状态（metadata + configs + items 含内嵌 grayUsers）
     */
    private void saveHistory(DynamicConfig config, String operationType, String operator) {
        DynamicConfigHistory history = new DynamicConfigHistory();
        history.setConfigId(config.getId());
        history.setVersionRange(config.getVersionRange());
        history.setEnv(config.getEnv());
        history.setRemark(config.getRemark());
        history.setOperationType(operationType);
        history.setOperator(operator);
        history.setSnapshotJson(buildSnapshotJson(config));
        historyMapper.insert(history);
    }

    private String buildSnapshotJson(DynamicConfig config) {
        List<DynamicConfigItem> items = itemMapper.findByConfigId(config.getId());

        // 一次性批量查询名单，避免 N+1
        Map<Long, List<DynamicConfigItemGrayUser>> grayByItem = new HashMap<>();
        if (!items.isEmpty()) {
            List<Long> itemIds = items.stream().map(DynamicConfigItem::getId).collect(Collectors.toList());
            for (DynamicConfigItemGrayUser gu : grayUserMapper.findByItemIds(itemIds)) {
                grayByItem.computeIfAbsent(gu.getItemId(), k -> new ArrayList<>()).add(gu);
            }
        }

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("versionRange", config.getVersionRange());
        metadata.put("env", config.getEnv());
        metadata.put("remark", config.getRemark());
        metadata.put("publishTime", formatTime(config.getUpdateTime() != null ? config.getUpdateTime() : config.getCreateTime()));

        Map<String, Map<String, Object>> configs = new LinkedHashMap<>();
        List<Map<String, Object>> itemMaps = new ArrayList<>();
        for (DynamicConfigItem item : items) {
            Map<String, Object> im = new LinkedHashMap<>();
            im.put("domain", item.getDomain());
            im.put("itemKey", item.getItemKey());
            im.put("itemValue", item.getItemValue());
            im.put("valueType", item.getValueType());
            im.put("enabled", item.getEnabled());
            im.put("grayEnabled", item.getGrayEnabled());
            im.put("grayPercent", item.getGrayPercent());
            im.put("remark", item.getRemark());
            List<Map<String, String>> gus = new ArrayList<>();
            for (DynamicConfigItemGrayUser gu : grayByItem.getOrDefault(item.getId(), Collections.emptyList())) {
                Map<String, String> gm = new LinkedHashMap<>();
                gm.put("listType", gu.getListType());
                gm.put("username", gu.getUsername());
                gus.add(gm);
            }
            im.put("grayUsers", gus);
            itemMaps.add(im);

            configs.computeIfAbsent(item.getDomain(), k -> new LinkedHashMap<>())
                    .put(item.getItemKey(), parseItemValue(item));
        }

        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("metadata", metadata);
        snapshot.put("configs", configs);
        snapshot.put("items", itemMaps);
        return JsonUtil.toJson(snapshot);
    }

    @Override
    public List<DynamicConfigHistory> getConfigHistory(Long configId) {
        return historyMapper.findByConfigId(configId);
    }

    @Override
    public List<DynamicConfigHistory> getAllHistory(String env, String versionRange) {
        return historyMapper.findHistoryWithFilters(env, versionRange);
    }

    @Override
    public Object getHistoryContent(Long historyId) throws Exception {
        DynamicConfigHistory history = historyMapper.findById(historyId);
        if (history == null) {
            throw new RuntimeException("历史版本不存在");
        }
        if (history.getSnapshotJson() == null || history.getSnapshotJson().trim().isEmpty()) {
            throw new RuntimeException("该历史版本为旧版文件存储，不支持查看与回溯");
        }
        return JsonUtil.toObject(history.getSnapshotJson(), Object.class);
    }

    @Override
    public DynamicConfigHistory getHistoryById(Long historyId) {
        return historyMapper.findById(historyId);
    }

    /**
     * 整包回溯：把整份配置回滚到某次发布的状态（覆盖全部配置项与名单）
     */
    @Override
    @SuppressWarnings("unchecked")
    public DynamicConfig revertToHistory(Long configId, Long historyId, String operator) throws Exception {
        DynamicConfigHistory history = historyMapper.findById(historyId);
        if (history == null) {
            throw new RuntimeException("历史版本不存在");
        }
        DynamicConfig config = dynamicConfigMapper.findById(configId);
        if (config == null) {
            throw new RuntimeException("当前配置不存在");
        }
        if (history.getSnapshotJson() == null || history.getSnapshotJson().trim().isEmpty()) {
            throw new RuntimeException("该历史版本为旧版文件存储，不支持查看与回溯");
        }
        Map<String, Object> snapshot = JsonUtil.toObject(history.getSnapshotJson(), new TypeReference<Map<String, Object>>() {});
        if (snapshot == null) {
            throw new RuntimeException("历史快照解析失败");
        }
        List<Map<String, Object>> items = (List<Map<String, Object>>) snapshot.get("items");

        // 1. 同步元数据
        config.setVersionRange(history.getVersionRange());
        config.setEnv(history.getEnv());
        config.setRemark("回溯到历史版本ID: " + historyId + ". " + (history.getRemark() == null ? "" : history.getRemark()));
        dynamicConfigMapper.update(config);

        // 2. 删除当前全部配置项及名单
        List<DynamicConfigItem> currentItems = itemMapper.findByConfigId(configId);
        List<Long> currentItemIds = currentItems.stream().map(DynamicConfigItem::getId).collect(Collectors.toList());
        if (!currentItemIds.isEmpty()) {
            grayUserMapper.deleteByItemIds(currentItemIds);
        }
        itemMapper.deleteByConfigId(configId);

        // 3. 按快照恢复配置项与名单
        if (items != null) {
            for (Map<String, Object> im : items) {
                DynamicConfigItem item = new DynamicConfigItem();
                item.setConfigId(configId);
                item.setVersionRange(config.getVersionRange());
                item.setEnv(config.getEnv());
                item.setDomain((String) im.get("domain"));
                item.setItemKey((String) im.get("itemKey"));
                item.setItemValue((String) im.get("itemValue"));
                item.setValueType((String) im.get("valueType"));
                item.setEnabled(im.get("enabled") instanceof Number ? ((Number) im.get("enabled")).intValue() : 1);
                item.setGrayEnabled(im.get("grayEnabled") instanceof Number ? ((Number) im.get("grayEnabled")).intValue() : 0);
                item.setGrayPercent(im.get("grayPercent") instanceof Number ? ((Number) im.get("grayPercent")).intValue() : 0);
                item.setRemark((String) im.get("remark"));
                itemMapper.insert(item);

                Object grayUsersObj = im.get("grayUsers");
                if (grayUsersObj instanceof List) {
                    restoreGrayUsersFromRaw(item.getId(), (List<?>) grayUsersObj, operator);
                }
                saveItemHistory(item, "REVERT", operator);
            }
        }

        // 4. 整包 REVERT 快照
        saveHistory(config, "REVERT", operator);
        return config;
    }

    // ==================== 配置项级历史与单项回溯 ====================

    @Override
    public List<DynamicConfigItemHistory> getItemHistory(Long itemId) {
        DynamicConfigItem item = itemMapper.findById(itemId);
        if (item == null) {
            throw new RuntimeException("配置项不存在");
        }
        List<DynamicConfigItemHistory> list =
                itemHistoryMapper.findByConfigIdAndKeyIgnoreVersion(item.getConfigId(), item.getDomain(), item.getItemKey());
        // 变更历史不包含当前版本：列表按时间倒序，第一条匹配当前版本范围的即为当前版本快照，剔除掉
        for (Iterator<DynamicConfigItemHistory> it = list.iterator(); it.hasNext(); ) {
            if (it.next().getVersionRange().equals(item.getVersionRange())) {
                it.remove();
                break;
            }
        }
        return list;
    }

    @Override
    public DynamicConfigItemHistory getItemHistoryById(Long historyId) {
        return itemHistoryMapper.findById(historyId);
    }

    /**
     * 单项回溯：只影响该配置项；历史记录为变更后状态，直接覆盖写回即可，操作本身可逆。
     * 历史与当前项版本范围一致则照常回填；不一致时同时恢复历史版本范围（完整还原该历史状态），
     * 若目标版本范围已被同名活动配置项占用则拒绝，避免产生重复项
     */
    @Override
    public DynamicConfigItem revertItemToHistory(Long itemId, Long historyId, String operator) throws Exception {
        DynamicConfigItem item = itemMapper.findById(itemId);
        if (item == null) {
            throw new RuntimeException("配置项不存在");
        }
        DynamicConfigItemHistory history = itemHistoryMapper.findById(historyId);
        if (history == null) {
            throw new RuntimeException("历史版本不存在");
        }
        if (!history.getConfigId().equals(item.getConfigId())
                || !history.getDomain().equals(item.getDomain())
                || !history.getItemKey().equals(item.getItemKey())) {
            throw new RuntimeException("历史记录与该配置项不匹配");
        }

        // 跨版本范围回溯：目标范围已被同名活动项占用时拒绝
        if (!history.getVersionRange().equals(item.getVersionRange())) {
            DynamicConfigItem conflict = itemMapper.findByConfigIdKeyAndVersion(
                    item.getConfigId(), item.getDomain(), item.getItemKey(), history.getVersionRange());
            if (conflict != null && !conflict.getId().equals(item.getId())) {
                throw new RuntimeException("目标版本范围「" + history.getVersionRange() + "」已被同名配置项占用，无法回溯");
            }
        }

        item.setItemValue(history.getItemValue());
        item.setValueType(history.getValueType());
        item.setEnabled(history.getEnabled());
        item.setGrayEnabled(history.getGrayEnabled());
        item.setGrayPercent(history.getGrayPercent());
        item.setRemark(history.getRemark());
        item.setVersionRange(history.getVersionRange());
        itemMapper.update(item);

        // 名单还原：先清空，再按快照插入
        grayUserMapper.deleteByItemId(itemId);
        List<Map<String, Object>> grayUsers = history.getGrayUsersJson() == null
                ? Collections.emptyList()
                : JsonUtil.toObject(history.getGrayUsersJson(), new TypeReference<List<Map<String, Object>>>() {});
        restoreGrayUsersFromRaw(itemId, grayUsers, operator);

        saveItemHistory(item, "REVERT", operator);
        return item;
    }

    /**
     * 从快照恢复名单（grayUsers: [{listType, username}]）
     */
    private void restoreGrayUsersFromRaw(Long itemId, List<?> grayUsers, String operator) {
        if (grayUsers == null || grayUsers.isEmpty()) {
            return;
        }
        String op = operator == null || operator.isEmpty() ? "system" : operator;
        List<DynamicConfigItemGrayUser> list = new ArrayList<>();
        for (Object o : grayUsers) {
            if (!(o instanceof Map)) continue;
            Map<?, ?> m = (Map<?, ?>) o;
            String listType = m.get("listType") == null ? null : String.valueOf(m.get("listType"));
            String username = m.get("username") == null ? null : String.valueOf(m.get("username"));
            if (username == null || username.trim().isEmpty()) continue;
            if (!LIST_TYPE_WHITE.equals(listType) && !LIST_TYPE_BLACK.equals(listType)) continue;
            DynamicConfigItemGrayUser gu = new DynamicConfigItemGrayUser();
            gu.setItemId(itemId);
            gu.setListType(listType);
            gu.setUsername(username.trim());
            gu.setOperator(op);
            list.add(gu);
        }
        if (!list.isEmpty()) {
            grayUserMapper.insertBatch(list);
        }
    }

    // ==================== 配置项历史写入 ====================

    /**
     * 写入一条配置项历史（记录变更后状态，名单以内嵌快照保存）；
     * 与最新一条完全一致则跳过，避免重复写入
     */
    private void saveItemHistory(DynamicConfigItem item, String operationType, String operator) {
        String grayUsersJson = buildGrayUsersJson(item.getId());
        DynamicConfigItemHistory latest =
                itemHistoryMapper.findLatestByConfigIdAndKey(item.getConfigId(), item.getDomain(), item.getItemKey(), item.getVersionRange());
        if (latest != null
                && Objects.equals(latest.getItemValue(), item.getItemValue())
                && Objects.equals(latest.getValueType(), item.getValueType())
                && Objects.equals(latest.getEnabled(), item.getEnabled())
                && Objects.equals(latest.getGrayEnabled(), item.getGrayEnabled())
                && Objects.equals(latest.getGrayPercent(), item.getGrayPercent())
                && Objects.equals(latest.getRemark(), item.getRemark())
                && Objects.equals(latest.getGrayUsersJson(), grayUsersJson)) {
            return;
        }

        DynamicConfigItemHistory h = new DynamicConfigItemHistory();
        h.setConfigId(item.getConfigId());
        h.setDomain(item.getDomain());
        h.setItemKey(item.getItemKey());
        h.setVersionRange(item.getVersionRange());
        h.setEnv(item.getEnv());
        h.setItemValue(item.getItemValue());
        h.setValueType(item.getValueType());
        h.setEnabled(item.getEnabled());
        h.setGrayEnabled(item.getGrayEnabled());
        h.setGrayPercent(item.getGrayPercent());
        h.setGrayUsersJson(grayUsersJson);
        h.setRemark(item.getRemark());
        h.setOperationType(operationType);
        h.setOperator(operator == null || operator.isEmpty() ? "system" : operator);
        itemHistoryMapper.insert(h);
    }

    private String buildGrayUsersJson(Long itemId) {
        List<Map<String, String>> arr = new ArrayList<>();
        for (DynamicConfigItemGrayUser gu : grayUserMapper.findByItemId(itemId, null, null)) {
            Map<String, String> m = new LinkedHashMap<>();
            m.put("listType", gu.getListType());
            m.put("username", gu.getUsername());
            arr.add(m);
        }
        return JsonUtil.toJson(arr);
    }

    private String formatTime(Date date) {
        return date == null ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }
}
