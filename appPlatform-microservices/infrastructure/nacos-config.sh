#!/bin/bash

# Nacos 配置导入脚本
# 使用方式: ./nacos-config.sh

NACOS_HOST=${NACOS_HOST:-localhost}
NACOS_PORT=${NACOS_PORT:-8848}
NACOS_NAMESPACE=${NACOS_NAMESPACE:-app-platform}

NACOS_URL="http://${NACOS_HOST}:${NACOS_PORT}"

echo "=========================================="
echo "Nacos 配置导入工具"
echo "Nacos地址: ${NACOS_URL}"
echo "命名空间: ${NACOS_NAMESPACE}"
echo "=========================================="

# 创建命名空间
echo "创建命名空间..."
curl -X POST "${NACOS_URL}/nacos/v1/console/namespaces" \
    -d "namespaceName=${NACOS_NAMESPACE}&namespaceDesc=AppPlatform微服务配置" 2>/dev/null || true

echo ""
echo "配置导入完成!"
echo "请手动在 Nacos 控制台创建以下配置:"
echo ""
echo "1. Data ID: common.yml, Group: DEFAULT_GROUP"
cat << 'EOF'
内容:
spring:
  redis:
    host: redis
    port: 6379
  rabbitmq:
    host: 172.16.110.45
    port: 5672
    username: rabbitmq
    password: fDAD4pRykTaNPtEr
EOF

echo ""
echo "2. 各服务配置已包含在 bootstrap.yml 中，无需额外配置"
echo ""
echo "=========================================="
