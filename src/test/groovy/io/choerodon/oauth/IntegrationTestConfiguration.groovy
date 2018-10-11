package io.choerodon.oauth

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.kafka.core.KafkaTemplate
import spock.mock.DetachedMockFactory

@TestConfiguration
class IntegrationTestConfiguration {

    private final detachedMockFactory = new DetachedMockFactory()

    @Bean
    KafkaTemplate<byte[], byte[]> kafkaTemplate() {
        detachedMockFactory.Mock(KafkaTemplate)
    }
}
