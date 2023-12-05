#docker run -p 8080:8080 \
#--name chatgpt-api \
#-e PARAMS="
#    --sever.port=80
#    --chatgpt.host=https://sdk.xfg.im/
#    --chatgpt.apiKey=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
#    --wx.config.originalid=gh_c5ce6e4a0e0e
#    --wx.config.appid=wxad979c0307864a66
#    --wx.config.gatewayAddress=b8b6" \
#-d fuzhengwei/chatgpt-api
docker run -p 8080:8080 --name openai-api -d secretsiden/openai-api:v-1.0