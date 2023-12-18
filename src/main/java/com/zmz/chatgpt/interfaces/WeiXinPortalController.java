package com.zmz.chatgpt.interfaces;

import com.zmz.chatgpt.application.IWeiXinValidateService;
import com.zmz.chatgpt.domain.receive.model.MessageTextEntity;
import com.zmz.chatgpt.infrastructure.util.XmlUtil;
import com.zmz.chatgpt.model.*;
import com.zmz.chatgpt.session.Configuration;
import com.zmz.chatgpt.session.OpenAiSession;
import com.zmz.chatgpt.session.OpenAiSessionFactory;
import com.zmz.chatgpt.session.defaults.DefaultOpenAiSessionFactory;
import lombok.extern.slf4j.Slf4j;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/wx/portal/{appid}")
public class WeiXinPortalController {

    private OpenAiSession openAiSession;

    @Resource
    private IWeiXinValidateService weiXinValidateService;

    @Value("${wx.config.originalid}")
    private String originalId;

    private Map<String, String> chatGPTMap = new ConcurrentHashMap<>();

    @Resource
    private ThreadPoolTaskExecutor taskExecutor;

    public WeiXinPortalController() {
        // 1. 配置文件
        Configuration configuration = new Configuration();
        configuration.setApiHost("https://open.bigmodel.cn/");
        configuration.setApiSecretKey("c8ebb130fc7fcf11c6fb826f6b55c412.dFYCXMJs3P6QDotr");
        configuration.setLevel(HttpLoggingInterceptor.Level.BODY);
        // 2. 会话工厂
        OpenAiSessionFactory factory = new DefaultOpenAiSessionFactory(configuration);
        // 3. 开启会话
        this.openAiSession = factory.openSession();
        log.info("初始化 openAiSession");
    }


    /**
     * 微信验证签名
     */
    @GetMapping(produces = "text/plain;charset=utf-8")
    public String validate(@PathVariable String appid,
                           @RequestParam(value = "signature", required = false) String signature,
                           @RequestParam(value = "timestamp", required = false) String timestamp,
                           @RequestParam(value = "nonce", required = false) String nonce,
                           @RequestParam(value = "echostr", required = false) String echostr) {
        try {
            log.info("微信公众号验签信息{}开始 [{}, {}, {}, {}]", appid, signature, timestamp, nonce, echostr);
            if (StringUtils.isAnyBlank(signature, timestamp, nonce, echostr)) {
                throw new IllegalArgumentException("请求参数非法，请核实!");
            }
            boolean check = weiXinValidateService.checkSign(signature, timestamp, nonce);
            log.info("微信公众号验签信息{}完成 check：{}", appid, check);
            if (!check) {
                return "验签失败";
            }
            return echostr;
        } catch (Exception e) {
            log.error("微信公众号验签信息{}失败 [{}, {}, {}, {}]", appid, signature, timestamp, nonce, echostr, e);
            return null;
        }
    }


    /**
     * 此处是处理微信服务器的消息转发的
     */
    @PostMapping(produces = "text/xml;charset=UTF-8")
    public String post(@PathVariable String appid,
                       @RequestBody String requestBody,
                       @RequestParam("openid") String openid) {
        try {
            log.info("接收微信公众号信息请求{}开始 {}", openid, requestBody);
            MessageTextEntity message = XmlUtil.xmlToBean(requestBody, MessageTextEntity.class);
            // 异步任务
            if (chatGPTMap.get(message.getContent().trim()) == null || "NULL".equals(chatGPTMap.get(message.getContent().trim()))) {
                // 反馈信息[文本]
                MessageTextEntity res = new MessageTextEntity();
                res.setToUserName(openid);
                res.setFromUserName(originalId);
                res.setCreateTime(String.valueOf(System.currentTimeMillis() / 1000L));
                res.setMsgType("text");
                res.setContent("消息处理中，请再回复我一句【" + message.getContent().trim() + "】");
                if (chatGPTMap.get(message.getContent().trim()) == null) {
                    doChatGPTTask(message.getContent().trim());
                }

                return XmlUtil.beanToXml(res);
            }

            // 反馈信息[文本]
            MessageTextEntity res = new MessageTextEntity();
            res.setToUserName(openid);
            res.setFromUserName(originalId);
            res.setCreateTime(String.valueOf(System.currentTimeMillis() / 1000L));
            res.setMsgType("text");
            res.setContent(chatGPTMap.get(message.getContent().trim()));
            String result = XmlUtil.beanToXml(res);
            // TODO 微信公众号未换行
            log.info("接收微信公众号信息请求{}完成 {}", openid, result);
            chatGPTMap.remove(message.getContent().trim());
            return result;
        } catch (Exception e) {
            log.error("接收微信公众号信息请求{}失败 {}", openid, requestBody, e);
            return "";
        }
    }

    public void doChatGPTTask(String content) {
        chatGPTMap.put(content, "NULL");
        taskExecutor.execute(() -> {
            // OpenAI 请求
            // 1. 创建参数
            ChatCompletionRequest chatCompletion = ChatCompletionRequest
                    .builder()
                    .model(Model.CHATGLM_TURBO)
                    .prompt(new ArrayList<ChatCompletionRequest.Prompt>() {
                        private static final long serialVersionUID = -7988151926241837899L;

                        {
                            add(ChatCompletionRequest.Prompt.builder()
                                    .role(Role.user.getCode())
                                    .content(content)
                                    .build());
                        }
                    })
                    .build();
            // 2. 发起请求
            String response;
//            try {
//                // 3. 解析结果
//                CompletableFuture<String> future = openAiSession.completions(chatCompletion);
//                response = future.get();
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            } catch (ExecutionException e) {
//                throw new RuntimeException(e);
//            }
            ChatCompletionSyncResponse chatCompletionSyncResponse;
            try {
                chatCompletionSyncResponse = openAiSession.completionsSync(chatCompletion);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            chatGPTMap.put(content, chatCompletionSyncResponse.getData().getChoices().get(0).getContent());
        });
    }


}
