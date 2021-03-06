package com.aelkz.blueprint.configuration.infinispan;

import java.util.Objects;
import org.apache.camel.component.infinispan.processor.idempotent.InfinispanIdempotentRepository;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
// not import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.commons.api.BasicCacheContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Configuration
@ConfigurationProperties(prefix = "infinispan")
//@Component
public class InfinispanAutoConfiguration {

    private Logger logger = LoggerFactory.getLogger(getClass());

    // The name of the Infinispan service.
    private String service = "datagrid-app-hotrod";

    // The name of the Infinispan cache.
    private String cacheName = "default";

    // Defines a bean named 'remoteCacheContainer' that points to the remote Infinispan cluster.
    @Bean(initMethod = "start", destroyMethod = "stop")
    public BasicCacheContainer remoteCacheContainer(Environment environment) {

        String serviceBaseName = service.toUpperCase().replace("-", "_");

        String host = environment.getProperty(serviceBaseName + "_SERVICE_HOST") == null ?
                "localhost" : environment.getProperty(serviceBaseName + "_SERVICE_HOST");

        String port = environment.getProperty(serviceBaseName + "_SERVICE_PORT") == null ?
                "11222" : environment.getProperty(serviceBaseName + "_SERVICE_PORT");

        String maxEntriesStr = environment.getProperty(serviceBaseName + "_MAX_ENTRIES") == null ?
                "1000" : environment.getProperty(serviceBaseName + "_MAX_ENTRIES");

        Integer maxEntries = 0;

        try {
            maxEntries = Integer.valueOf(maxEntriesStr);
        }catch (NumberFormatException ex) {
            logger.error("Error trying to get infinispan maxEntries value.");
        }

        Objects.requireNonNull(host, "Infinispan service host not found in the environment");
        Objects.requireNonNull(port, "Infinispan service port not found in the environment");

        String hostPort = host + ":" + port;
        logger.info("Connecting to the Infinispan service at {}", hostPort);

        ConfigurationBuilder builder = new ConfigurationBuilder()
                .forceReturnValues(true)
                .addServers(hostPort)
                .maxRetries(maxEntries)
                .security()
                .connectionTimeout(3000)
                ;

        // new RemoteCacheManagerFactory().getRemoteCacheManager()
        return new RemoteCacheManager(builder.create(), false);
    }

    // Defines a Camel idempotent repository based on the Infinispan cache container.
    @Bean
    public InfinispanIdempotentRepository infinispanRepository(BasicCacheContainer cacheContainer) {
        return InfinispanIdempotentRepository.infinispanIdempotentRepository(cacheContainer, cacheName);
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

}