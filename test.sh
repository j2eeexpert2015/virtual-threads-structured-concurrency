#!/bin/bash

# Simple script to run the Project Loom comparison applications

# Compile Java files
compile() {
    echo "Compiling Java files..."
    mkdir -p build
    javac --enable-preview --release 21 -d build src/main/java/com/example/dummyapp/*.java
    echo "Compilation completed."
}

# Run backend server
run_backend() {
    echo "Starting backend server on port 8080 with 333ms delay..."
    java --enable-preview -cp build com.example.dummyapp.BackendServer 8080 333
}

# Run frontend with platform threads
run_frontend_platform() {
    PORT=${1:-9000}
    echo "Starting frontend with platform threads on port $PORT..."

    # Check if port is available
    if lsof -Pi :$PORT -sTCP:LISTEN -t >/dev/null 2>&1; then
        echo "Port $PORT is already in use. Trying port $((PORT+1))..."
        PORT=$((PORT+1))
    fi

    java --enable-preview -cp build com.example.dummyapp.FrontendServer $PORT platform http://localhost:8080 1000
}

# Run frontend with virtual threads
run_frontend_virtual() {
    PORT=${1:-9001}
    echo "Starting frontend with virtual threads on port $PORT..."

    # Check if port is available
    if lsof -Pi :$PORT -sTCP:LISTEN -t >/dev/null 2>&1; then
        echo "Port $PORT is already in use. Trying port $((PORT+1))..."
        PORT=$((PORT+1))
    fi

    java --enable-preview -cp build com.example.dummyapp.FrontendServer $PORT virtual http://localhost:8080
}

# Kill all application processes
kill_servers() {
    echo "Stopping all servers..."
    pkill -f "com.example.dummyapp" 2>/dev/null || true
    sleep 2
    echo "All servers stopped."
}

# Check server status
check_servers() {
    echo "Checking server status..."

    echo "=== Java Processes ==="
    jps -v | grep "com.example.dummyapp" || echo "No application processes running"

    echo -e "\n=== Port Status ==="
    for port in 8080 9000 9001; do
        if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
            echo "Port $port: IN USE"
        else
            echo "Port $port: Available"
        fi
    done
}

# Simple test
test_endpoints() {
    echo "Testing endpoints..."

    echo "Platform threads (port 9000):"
    curl -s http://localhost:9000/api/process | head -c 100
    echo "..."

    echo -e "\nVirtual threads (port 9001):"
    curl -s http://localhost:9001/api/process | head -c 100
    echo "..."
}

# Help function
help() {
    echo "Usage: $0 [command]"
    echo ""
    echo "Commands:"
    echo "  compile                      - Compile Java files"
    echo "  backend                      - Run backend server (port 8080)"
    echo "  frontend-platform [port]     - Run platform threads frontend (default: 9000)"
    echo "  frontend-virtual [port]      - Run virtual threads frontend (default: 9001)"
    echo "  kill-servers                 - Stop all running servers"
    echo "  check-servers                - Check server status and port usage"
    echo "  test                         - Quick test of both endpoints"
    echo "  help                         - Show this help"
    echo ""
    echo "Quick Start:"
    echo "  1. ./test.sh compile"
    echo "  2. ./test.sh backend              (terminal 1)"
    echo "  3. ./test.sh frontend-platform    (terminal 2)"
    echo "  4. ./test.sh frontend-virtual     (terminal 3)"
    echo "  5. ./test.sh test                 (terminal 4)"
    echo ""
    echo "Then use your existing load_test.jmx for load testing."
}

# Main script logic
case "$1" in
    compile)
        compile
        ;;
    backend)
        run_backend
        ;;
    frontend-platform)
        run_frontend_platform $2
        ;;
    frontend-virtual)
        run_frontend_virtual $2
        ;;
    kill-servers)
        kill_servers
        ;;
    check-servers)
        check_servers
        ;;
    test)
        test_endpoints
        ;;
    help|"")
        help
        ;;
    *)
        echo "Unknown command: $1"
        help
        exit 1
        ;;
esac