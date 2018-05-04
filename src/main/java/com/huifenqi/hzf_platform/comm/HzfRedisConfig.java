package com.huifenqi.hzf_platform.comm;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

import java.lang.reflect.Method;

/**
 * Created by lchb on 2017-7-22.
 */
@Configuration
@EnableCaching
public class HzfRedisConfig {

    @Value("${spring.redis.hzf.host}")
    private String host;
    @Value("${spring.redis.hzf.port}")
    private int port;
    @Value("${spring.redis.hzf.password}")
    private String pwd;
    @Value("${spring.redis.hzf.timeout}")
    private int timeout;

    @Value("${spring.redis.hzf.database}")
    private int database;


    @Primary
    @Bean(name="hzfRedisConnectionFactory")
    public JedisConnectionFactory hzfRedisConnectionFactory() {
        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setHostName(host);
        factory.setDatabase(database);
        factory.setPort(port);
        factory.setTimeout(timeout); //设置连接超时时间
        factory.setPassword(pwd);
        System.out.println("redis config========="+host);
        return factory;
    }

    @Bean
    public KeyGenerator wiselyKeyGenerator(){
        return new KeyGenerator() {
            @Override
            public Object generate(Object target, Method method, Object... params) {
                StringBuilder sb = new StringBuilder();
                sb.append(target.getClass().getName());
                sb.append(method.getName());
                for (Object obj : params) {
                    sb.append(obj.toString());
                }
                return sb.toString();
            }
        };
    }

    @Primary
    @Bean(name="hzfCache")
    public CacheManager cacheManager(@Qualifier("hzfRedisTemplate") RedisTemplate redisTemplate) {
        RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate);
        // Number of seconds before expiration. Defaults to unlimited (0)There is more than one bean of 'RedisTemplate' type
        cacheManager.setDefaultExpiration(10); //设置key-value超时时间
        return cacheManager;
    }


    @Primary
    @Bean(name = "hzfRedisTemplate")
    public StringRedisTemplate hzfRedisTemplate() {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(hzfRedisConnectionFactory());
       // setSerializer(template); //设置序列化工具，这样ReportBean不需要实现Serializable接口
        template.afterPropertiesSet();
        return template;
    }

    public JedisPoolConfig poolCofig(int maxIdle, int maxTotal,
                                     long maxWaitMillis, boolean testOnBorrow) {
        JedisPoolConfig poolCofig = new JedisPoolConfig();
        poolCofig.setMaxIdle(maxIdle);
        poolCofig.setMaxTotal(maxTotal);
        poolCofig.setMaxWaitMillis(maxWaitMillis);
        poolCofig.setTestOnBorrow(testOnBorrow);
        return poolCofig;
    }

    /*private void setSerializer(StringRedisTemplate template) {
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        template.setValueSerializer(jackson2JsonRedisSerializer);
    }*/

}