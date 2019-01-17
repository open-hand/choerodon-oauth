package io.choerodon.oauth.infra.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.config.annotation.web.http.SpringHttpSessionConfiguration;
import org.springframework.session.data.redis.RedisFlushMode;
import org.springframework.session.data.redis.RedisOperationsSessionRepository;
import org.springframework.session.web.http.SessionRepositoryFilter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;
import org.springframework.web.filter.RequestContextFilter;

import java.util.Arrays;
import java.util.Map;

@ConditionalOnProperty(name = "choerodon.redisHttpSession.enabled", havingValue = "true", matchIfMissing = true)
@Configuration
@EnableScheduling
public class ChoerodonRedisHttpSessionConfiguration extends SpringHttpSessionConfiguration implements EmbeddedValueResolverAware, ImportAware {
    private Integer maxInactiveIntervalInSeconds = 1800;
    private String redisNamespace = "";
    private RedisFlushMode redisFlushMode;
    private StringValueResolver embeddedValueResolver;

    public ChoerodonRedisHttpSessionConfiguration() {
        this.redisFlushMode = RedisFlushMode.ON_SAVE;
    }

    @Bean
    @ConditionalOnMissingBean(RequestContextFilter.class)
    public RequestContextFilter requestContextFilter() {
        return new RequestContextFilter();
    }

    @Bean
    public FilterRegistrationBean requestContextFilterChainRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean(requestContextFilter());
        registration.setOrder(SessionRepositoryFilter.DEFAULT_ORDER + 1);
        registration.setName("requestContextFilter");
        return registration;
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory, RedisOperationsSessionRepository messageListener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(messageListener, Arrays.asList(new PatternTopic("__keyevent@*:del"), new PatternTopic("__keyevent@*:expired")));
        container.addMessageListener(messageListener, Arrays.asList(new PatternTopic(messageListener.getSessionCreatedChannelPrefix() + "*")));
        return container;
    }

    @Bean
    public CustomJdkSerializationRedisSerializer customJdkSerializationRedisSerializer() {
        return new CustomJdkSerializationRedisSerializer();
    }

    @Bean
    public RedisTemplate<Object, Object> sessionRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setDefaultSerializer(customJdkSerializationRedisSerializer());
        template.setConnectionFactory(connectionFactory);
        return template;
    }

    @Bean
    public RedisOperationsSessionRepository sessionRepository(@Qualifier("sessionRedisTemplate") RedisOperations<Object, Object> sessionRedisTemplate, ApplicationEventPublisher applicationEventPublisher) {
        RedisOperationsSessionRepository sessionRepository = new RedisOperationsSessionRepository(sessionRedisTemplate);
        sessionRepository.setApplicationEventPublisher(applicationEventPublisher);
        sessionRepository.setDefaultMaxInactiveInterval(this.maxInactiveIntervalInSeconds);
        sessionRepository.setDefaultSerializer(customJdkSerializationRedisSerializer());
        String tempRedisNamespace = this.getRedisNamespace();
        if (StringUtils.hasText(tempRedisNamespace)) {
            sessionRepository.setRedisKeyNamespace(tempRedisNamespace);
        }
        sessionRepository.setRedisFlushMode(this.redisFlushMode);
        return sessionRepository;
    }

    public void setMaxInactiveIntervalInSeconds(int maxInactiveIntervalInSeconds) {
        this.maxInactiveIntervalInSeconds = maxInactiveIntervalInSeconds;
    }

    public void setRedisNamespace(String namespace) {
        this.redisNamespace = namespace;
    }

    public void setRedisFlushMode(RedisFlushMode redisFlushMode) {
        Assert.notNull(redisFlushMode, "redisFlushMode cannot be null");
        this.redisFlushMode = redisFlushMode;
    }

    private String getRedisNamespace() {
        return StringUtils.hasText(this.redisNamespace) ? this.redisNamespace : System.getProperty("spring.session.redis.namespace", "");
    }

    public void setImportMetadata(AnnotationMetadata importMetadata) {
        Map<String, Object> enableAttrMap = importMetadata.getAnnotationAttributes(EnableRedisHttpSession.class.getName());
        AnnotationAttributes enableAttrs = AnnotationAttributes.fromMap(enableAttrMap);
        this.maxInactiveIntervalInSeconds = enableAttrs.getNumber("maxInactiveIntervalInSeconds");
        String redisNamespaceValue = enableAttrs.getString("redisNamespace");
        if (StringUtils.hasText(redisNamespaceValue)) {
            this.redisNamespace = this.embeddedValueResolver.resolveStringValue(redisNamespaceValue);
        }

        this.redisFlushMode = enableAttrs.getEnum("redisFlushMode");
    }

    @Autowired(
            required = false
    )

    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.embeddedValueResolver = resolver;
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

}
