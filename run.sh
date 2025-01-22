#!/bin/bash

# 显示使用方法
show_usage() {
    echo "使用方法:"
    echo "  ./run.sh [command] [target]"
    echo ""
    echo "参数:"
    echo "  command: build 或 dev"
    echo "  target: frontend 或 backend"
    echo ""
    echo "例子:"
    echo "  ./run.sh build frontend  # 构建前端"
    echo "  ./run.sh dev backend     # 开发后端"
}

# 检查参数数量
if [ $# -ne 2 ]; then
    show_usage
    exit 1
fi

command=$1
target=$2

# 执行对应的命令
case "$command" in
    "build")
        case "$target" in
            "frontend")
                echo "构建前端..."
                pnpm --filter tokomo_FE run build
                ;;
            "backend")
                echo "构建后端..."
                pnpm --filter tokomo_BE run build
                ;;
            *)
                echo "错误: target 必须是 frontend 或 backend"
                show_usage
                exit 1
                ;;
        esac
        ;;
    "dev")
        case "$target" in
            "frontend")
                echo "启动前端开发服务器..."
                pnpm --filter tokomo_FE run dev
                ;;
            "backend")
                echo "启动后端开发服务器..."
                pnpm --filter tokomo_BE run dev
                ;;
            *)
                echo "错误: target 必须是 frontend 或 backend"
                show_usage
                exit 1
                ;;
        esac
        ;;
    *)
        echo "错误: command 必须是 build 或 dev"
        show_usage
        exit 1
        ;;
esac
