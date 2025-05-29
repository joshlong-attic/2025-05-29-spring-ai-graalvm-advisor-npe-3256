package com.example.demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

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
            System.out.println("before "  + DEFAULT_SCHEDULER);

            return chatClientRequest ;
        }

        @Override
        public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
            System.out.println("after "  + DEFAULT_SCHEDULER);
            return chatClientResponse ;
        }

        @Override
        public int getOrder() {
            return 0;
        }
    }

    @Bean
    ApplicationRunner runner(ChatClient.Builder builder, MyAdvisor advisor) {
        return args -> {
            var ai = builder
                    .defaultAdvisors(advisor)
                    .build();
            System.out.println(ai.prompt("tell me a joke").call().content());
        };
    }
}

