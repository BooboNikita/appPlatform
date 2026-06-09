#!/bin/bash

# AppPlatform 微服务快速启动脚本
# 使用方式: ./start.sh [infra|app|all|stop]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
INFRA_DIR="${SCRIPT_DIR}/infrastructure"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 启动基础设施
start_infra() {
    log_info "启动基础设施..."
    cd "${INFRA_DIR}"
    
    docker-compose up -d
    
    log_info "等待服务启动..."
    sleep 10
    
    log_info "基础设施已启动:"
    echo "  - Nacos:      http://localhost:8848/nacos"
    echo "  - Prometheus: http://localhost:9090"
    echo ""
    echo "外部服务 (需自行启动):"
    echo "  - MySQL:      localhost:3306"
    echo "  - Redis:      localhost:6379"
    echo "  - RabbitMQ:   172.16.110.45:5672"
}

# 初始化数据库 (使用本地 MySQL)
init_db() {
    log_info "初始化数据库..."
    
    # 检查本地 MySQL 是否可连接
    log_info "检查本地 MySQL 连接..."
    until mysql -h 127.0.0.1 -P 3306 -u root -p123456 -e "SELECT 1" >/dev/null 2>&1; do
        log_warn "等待本地 MySQL 就绪..."
        sleep 2
    done
    
    # 执行初始化脚本
    log_info "执行数据库初始化脚本..."
    for sql_file in ${INFRA_DIR}/init-scripts/*.sql; do
        log_info "执行: $(basename $sql_file)"
        mysql -h 127.0.0.1 -P 3306 -u root -p123456 < "$sql_file" 2>/dev/null || true
    done
    
    log_info "数据库初始化完成"
}

# 启动应用服务
start_app() {
    log_info "启动应用服务..."
    cd "${SCRIPT_DIR}"
    
    # 检查网络是否存在
    if ! docker network ls | grep -q "app-platform-network"; then
        log_error "请先启动基础设施: ./start.sh infra"
        exit 1
    fi
    
    docker-compose -f docker-compose.app.yml up -d
    
    log_info "应用服务已启动:"
    echo "  - Gateway:      http://localhost:8080"
    echo "  - Auth Service: http://localhost:8001"
    echo "  - App Service:  http://localhost:8002"
    echo "  - Log Service:  http://localhost:8003"
    echo "  - Event Service:http://localhost:8004"
    echo "  - Crash Service:http://localhost:8005"
    echo "  - File Service: http://localhost:8007"
    echo "  - Store Service:http://localhost:8008"
    echo "  - Perf Service: http://localhost:8009"
}

# 停止所有服务
stop_all() {
    log_info "停止所有服务..."
    cd "${SCRIPT_DIR}"
    docker-compose -f docker-compose.app.yml down 2>/dev/null || true
    
    cd "${INFRA_DIR}"
    docker-compose down
    
    log_info "所有服务已停止"
}

# 查看服务状态
status() {
    log_info "服务状态:"
    echo ""
    echo "基础设施:"
    cd "${INFRA_DIR}"
    docker-compose ps
    
    echo ""
    echo "应用服务:"
    cd "${SCRIPT_DIR}"
    docker-compose -f docker-compose.app.yml ps 2>/dev/null || echo "  未启动"
}

# 主函数
main() {
    case "${1:-all}" in
        infra)
            start_infra
            ;;
        init-db)
            init_db
            ;;
        app)
            start_app
            ;;
        all)
            start_infra
            sleep 5
            init_db
            start_app
            log_info "所有服务已启动完成!"
            echo ""
            echo "访问地址:"
            echo "  - API Gateway:  http://localhost:8080"
            echo "  - Nacos:        http://localhost:8848/nacos"
            echo "  - Prometheus:   http://localhost:9090"
            ;;
        stop)
            stop_all
            ;;
        status)
            status
            ;;
        *)
            echo "使用方式: $0 [infra|init-db|app|all|stop|status]"
            echo ""
            echo "命令说明:"
            echo "  infra    - 只启动基础设施 (MySQL, Redis, Nacos, etc.)"
            echo "  init-db  - 初始化数据库"
            echo "  app      - 只启动应用服务"
            echo "  all      - 启动所有服务 (默认)"
            echo "  stop     - 停止所有服务"
            echo "  status   - 查看服务状态"
            exit 1
            ;;
    esac
}

main "$@"
