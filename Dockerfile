FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 复制Maven构建的jar包
COPY target/wallet-system-1.0.0.jar app.jar

# 创建初始化脚本目录
RUN mkdir -p /app/init-scripts

# 暴露应用端口
EXPOSE 8080

# 启动Spring Boot应用
CMD ["java", "-jar", "app.jar"]