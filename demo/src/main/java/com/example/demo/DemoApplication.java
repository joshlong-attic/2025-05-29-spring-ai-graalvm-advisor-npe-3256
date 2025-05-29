package com.example.demo;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Component
    static class MyAdvisor
            implements BaseAdvisor {


        @Override
        public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
            System.out.println("before " + BaseAdvisor.DEFAULT_SCHEDULER);
            return chatClientRequest;
        }

        @Override
        public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
            System.out.println("after " + BaseAdvisor.DEFAULT_SCHEDULER);

            return chatClientResponse;
        }

        @Override
        public int getOrder() {
            return 0;
        }
    }


    @Bean
    MessageChatMemoryAdvisor messageChatMemoryAdvisor() {
        return MessageChatMemoryAdvisor
                .builder(new ChatMemory() {
                    @Override
                    public void add(String conversationId, List<Message> messages) {
                        System.out.println("adding " + conversationId + ":" + messages);
                    }

                    @Override
                    public List<Message> get(String conversationId) {
                        return List.of();
                    }

                    @Override
                    public void clear(String conversationId) {
                        // yup.
                    }
                })
                .build();
    }



    // ignored
    //    @Component
    static class Proxy implements BeanPostProcessor {

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof BaseAdvisor baseAdvisor) {
                var proxy = new ProxyFactoryBean();
                proxy.addAdvice((MethodInterceptor) invocation -> {
                    System.out.println("proceeding for " + invocation.getMethod().getName() + '.');
                    return invocation.proceed();
                });
                proxy.setTarget(baseAdvisor);
                for (var c : baseAdvisor.getClass().getInterfaces())
                    proxy.addInterface(c);
                proxy.setProxyTargetClass(true);
                return proxy.getObject();
            }
            return bean;
        }
    }

    @Bean
    ApplicationRunner runner(MessageChatMemoryAdvisor messageChatMemoryAdvisor, ChatClient.Builder builder,
                             MyAdvisor advisor) {
        return _ -> {
            var ai = builder
                    .defaultAdvisors(advisor, messageChatMemoryAdvisor)
                    .build();
            System.out.println(ai.prompt("tell me a joke").call().content());
        };
    }
}

