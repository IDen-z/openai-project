# 根据target中的jar构建镜像
docker build -f ./Dockerfile -t secretsiden/openai-api:v-1.0 .

# 推送镜像至远程docker仓库
docker push secretsiden/openai-api:v-1.0

# 服务器上进行更新操作
docker stop openai-api && docker rm openai-api && docker rmi secretsiden/openai-api:v-1.0

# 更新服务
docker run -p 8080:8080 --name openai-api -d secretsiden/openai-api:v-1.0