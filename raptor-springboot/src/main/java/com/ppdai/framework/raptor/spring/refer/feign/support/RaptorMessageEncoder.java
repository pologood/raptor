package com.ppdai.framework.raptor.spring.refer.feign.support;

import com.ppdai.framework.raptor.annotation.RaptorMessage;
import com.ppdai.framework.raptor.spring.converter.RaptorMessageConverter;
import com.ppdai.framework.raptor.spring.utils.RaptorMessageUtils;
import feign.RequestTemplate;
import feign.codec.EncodeException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.core.annotation.AnnotationUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * @author yinzuolong
 */
public class RaptorMessageEncoder extends SpringEncoder {

    private RaptorMessageConverter raptorMessageConverter;

    public RaptorMessageEncoder(ObjectFactory<HttpMessageConverters> messageConverters, RaptorMessageConverter raptorMessageConverter) {
        super(messageConverters);
        this.raptorMessageConverter = raptorMessageConverter;
    }

    @Override
    public void encode(Object requestBody, Type bodyType, RequestTemplate request) throws EncodeException {
        Class<?> requestType = requestBody.getClass();
        if (AnnotationUtils.findAnnotation(requestType, RaptorMessage.class) != null) {
            if ("GET".equalsIgnoreCase(request.method())) {
                try {
                    Map<String, List<String>> map = RaptorMessageUtils.transferMessageToQuery(requestBody);
                    for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                        request.query(entry.getKey(), entry.getValue());
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Transfer requestBody to query string error.", e);
                }
            } else {
                try {
                    raptorMessageConverter.write(requestBody, null, new SpringEncoder.FeignOutputMessage(request));
                } catch (IOException ex) {
                    throw new EncodeException("Error converting request body", ex);
                }
            }
        }
        super.encode(requestBody, bodyType, request);
    }
}
