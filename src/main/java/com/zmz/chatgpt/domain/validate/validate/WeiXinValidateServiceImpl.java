package com.zmz.chatgpt.domain.validate.validate;

import com.zmz.chatgpt.application.IWeiXinValidateService;
import com.zmz.chatgpt.infrastructure.util.sdk.SignatureUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WeiXinValidateServiceImpl implements IWeiXinValidateService {

    @Value("${wx.config.token}")
    private String token;

    @Override
    public boolean checkSign(String signature, String timestamp, String nonce) {
        return SignatureUtil.check(token, signature, timestamp, nonce);
    }

}
