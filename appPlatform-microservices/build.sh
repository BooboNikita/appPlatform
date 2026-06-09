#!/bin/bash

# AppPlatform 微服务构建脚本
# 使用方式: ./build.sh [all|common|gateway|auth|app|log|event|crash|file|store|perf]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${SCRIPT_DIR}"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# 构建公共模块
build_common() {
    log_step "构建公共模块 (app-platform-common)..."
    cd "${SCRIPT_DIR}/app-platform-common"
    mvn clean install -DskipTests
    log_info "公共模块构建完成"
}

# 构建 Gateway
build_gateway() {
    log_step "构建 API Gateway..."
    cd "${SCRIPT_DIR}/app-platform-gateway"
    mvn clean package -DskipTests
    log_info "Gateway 构建完成"
}

# 构建认证服务
build_auth() {
    log_step "构建认证服务 (auth-service)..."
    cd "${SCRIPT_DIR}/auth-service"
    mvn clean package -DskipTests
    log_info "认证服务构建完成"
}

# 构建应用管理服务
build_app() {
    log_step "构建应用管理服务 (app-service)..."
    cd "${SCRIPT_DIR}/app-service"
    mvn clean package -DskipTests
    log_info "应用管理服务构建完成"
}

# 构建日志服务
build_log() {
    log_step "构建日志服务 (log-service)..."
    cd "${SCRIPT_DIR}/log-service"
    mvn clean package -DskipTests
    log_info "日志服务构建完成"
}

# 构建事件追踪服务
build_event() {
    log_step "构建事件追踪服务 (event-service)..."
    cd "${SCRIPT_DIR}/event-service"
    mvn clean package -DskipTests
    log_info "事件追踪服务构建完成"
}

# 构建崩溃报告服务
build_crash() {
    log_step "构建崩溃报告服务 (crash-service)..."
    cd "${SCRIPT_DIR}/crash-service"
    mvn clean package -DskipTests
    log_info "崩溃报告服务构建完成"
}

# 构建文件存储服务
build_file() {
    log_step "构建文件存储服务 (file-service)..."
    cd "${SCRIPT_DIR}/file-service"
    mvn clean package -DskipTests
    log_info "文件存储服务构建完成"
}

# 构建商店链接服务
build_store() {
    log_step "构建商店链接服务 (store-service)..."
    cd "${SCRIPT_DIR}/store-service"
    mvn clean package -DskipTests
    log_info "商店链接服务构建完成"
}

# 构建绩效评估服务
build_perf() {
    log_step "构建绩效评估服务 (perf-service)..."
    cd "${SCRIPT_DIR}/perf-service"
    mvn clean package -DskipTests
    log_info "绩效评估服务构建完成"
}

# 构建所有服务
build_all() {
    log_info "开始构建所有服务..."
    
    # 先构建父 POM
    log_step "构建父 POM..."
    cd "${SCRIPT_DIR}"
    mvn clean install -N -DskipTests
    
    # 构建公共模块
    build_common
    
    # 并行构建所有服务
    log_step "并行构建所有服务..."
    cd "${SCRIPT_DIR}"
    mvn clean package -DskipTests -pl app-platform-gateway,auth-service,app-service,log-service,event-service,crash-service,file-service,store-service,perf-service -am
    
    log_info "所有服务构建完成!"
}

# 构建 Docker 镜像
build_docker() {
    log_info "构建 Docker 镜像..."
    cd "${SCRIPT_DIR}"
    
    # 构建基础设施
    log_step "构建基础设施镜像..."
    cd "${SCRIPT_DIR}/infrastructure"
    docker-compose pull
    
    # 构建应用镜像
    log_step "构建应用服务镜像..."
    cd "${SCRIPT_DIR}"
    docker-compose -f docker-compose.app.yml build
    
    log_info "Docker 镜像构建完成!"
}

# 主函数
main() {
    case "${1:-all}" in
        all)
            build_all
            ;;
        common)
            build_common
            ;;
        gateway)
            build_gateway
            ;;
        auth)
            build_auth
            ;;
        app)
            build_app
            ;;
        log)
            build_log
            ;;
        event)
            build_event
            ;;
        crash)
            build_crash
            ;;
        file)
            build_file
            ;;
        store)
            build_store
            ;;
        perf)
            build_perf
            ;;
        docker)
            build_docker
            ;;
        *)
            echo "使用方式: $0 [all|common|gateway|auth|app|log|event|crash|file|store|perf|docker]"
            echo ""
            echo "命令说明:"
            echo "  all     - 构建所有服务 (默认)"
            echo "  common  - 只构建公共模块"
            echo "  gateway - 只构建 API Gateway"
            echo "  auth    - 只构建认证服务"
            echo "  app     - 只构建应用管理服务"
            echo "  log     - 只构建日志服务"
            echo "  event   - 只构建事件追踪服务"
            echo "  crash   - 只构建崩溃报告服务"
            echo "  file    - 只构建文件存储服务"
            echo "  store   - 只构建商店链接服务"
            echo "  perf    - 只构建绩效评估服务"
            echo "  docker  - 构建 Docker 镜像"
            exit 1
            ;;
    esac
}

main "$@"
