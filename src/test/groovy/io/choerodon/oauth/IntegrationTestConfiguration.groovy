package io.choerodon.oauth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Bean
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.kafka.core.KafkaTemplate
import spock.mock.DetachedMockFactory

@TestConfiguration
class IntegrationTestConfiguration {

    private final detachedMockFactory = new DetachedMockFactory()

    @Autowired
    TestRestTemplate testRestTemplate

    @Bean
    StringRedisTemplate stringRedisTemplate() {
        return detachedMockFactory.Mock(StringRedisTemplate)
    }

    @Bean
    KafkaTemplate<byte[], byte[]> kafkaTemplate() {
        detachedMockFactory.Mock(KafkaTemplate)
    }
}
