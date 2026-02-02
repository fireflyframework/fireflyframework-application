/*
 * Copyright 2025 Firefly Software Solutions Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.firefly.common.application.plugin.config;

import com.firefly.common.application.plugin.ProcessPlugin;
import com.firefly.common.application.plugin.ProcessPluginRegistry;
import com.firefly.common.application.plugin.loader.PluginLoader;
import com.firefly.common.application.plugin.loader.SpringBeanPluginLoader;
import com.firefly.common.application.plugin.service.ProcessMappingService;
import com.firefly.common.application.plugin.service.ProcessPluginExecutor;
import com.firefly.common.application.security.SecurityAuthorizationService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;

/**
 * Auto-configuration for the Firefly Plugin Architecture.
 * 
 * <p>This configuration:</p>
 * <ul>
 *   <li>Enables plugin configuration properties</li>
 *   <li>Creates the plugin registry</li>
 *   <li>Initializes all plugin loaders in priority order</li>
 *   <li>Discovers and registers plugins from all sources</li>
 *   <li>Configures the ProcessPluginExecutor</li>
 * </ul>
 * 
 * <h3>Activation</h3>
 * <p>This configuration is activated when {@code firefly.application.plugin.enabled=true}
 * (which is the default).</p>
 * 
 * @author Firefly Development Team
 * @since 1.0.0
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(PluginProperties.class)
@ConditionalOnProperty(name = "firefly.application.plugin.enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "com.firefly.common.application.plugin")
@RequiredArgsConstructor
public class PluginAutoConfiguration {
    
    private final PluginProperties properties;
    private final List<PluginLoader> pluginLoaders;
    private final ProcessPluginRegistry registry;
    
    /**
     * Creates the ProcessPluginRegistry bean if not already defined.
     */
    @Bean
    @ConditionalOnMissingBean
    public ProcessPluginRegistry processPluginRegistry() {
        return new ProcessPluginRegistry();
    }
    
    /**
     * Creates the SpringBeanPluginLoader bean if not already defined.
     */
    @Bean
    @ConditionalOnMissingBean
    public SpringBeanPluginLoader springBeanPluginLoader(PluginProperties properties) {
        return new SpringBeanPluginLoader(properties);
    }
    
    /**
     * Creates the ProcessPluginExecutor bean if not already defined.
     */
    @Bean
    @ConditionalOnMissingBean
    public ProcessPluginExecutor processPluginExecutor(
            ProcessPluginRegistry registry,
            ProcessMappingService mappingService,
            SecurityAuthorizationService authorizationService,
            PluginProperties properties) {
        return new ProcessPluginExecutor(registry, mappingService, authorizationService, properties);
    }
    
    /**
     * Creates a default ProcessMappingService if not already defined.
     * This basic implementation returns a default mapping; override in applications
     * that integrate with config-mgmt.
     */
    @Bean
    @ConditionalOnMissingBean
    public ProcessMappingService processMappingService() {
        return new DefaultProcessMappingService();
    }
    
    /**
     * Initializes the plugin system after the application context is ready.
     */
    @PostConstruct
    public void initializePluginSystem() {
        if (!properties.isEnabled()) {
            log.info("Plugin system is disabled");
            return;
        }
        
        log.info("Initializing Firefly Plugin System...");
        
        // Sort loaders by priority
        List<PluginLoader> sortedLoaders = pluginLoaders.stream()
                .filter(PluginLoader::isEnabled)
                .sorted(Comparator.comparingInt(PluginLoader::getPriority))
                .toList();
        
        log.info("Found {} enabled plugin loaders", sortedLoaders.size());
        
        // Initialize loaders and discover plugins
        Flux.fromIterable(sortedLoaders)
                .flatMap(loader -> {
                    log.debug("Initializing loader: {} (priority: {})", 
                            loader.getLoaderType(), loader.getPriority());
                    return loader.initialize()
                            .then(Mono.just(loader));
                })
                .flatMap(loader -> {
                    log.debug("Discovering plugins from loader: {}", loader.getLoaderType());
                    return loader.discoverPlugins();
                })
                .flatMap(plugin -> {
                    log.debug("Registering plugin: {} v{}", 
                            plugin.getProcessId(), plugin.getVersion());
                    return plugin.onInit()
                            .then(registry.register(plugin))
                            .thenReturn(plugin);
                })
                .doOnComplete(() -> {
                    log.info("Plugin system initialized. Registered {} processes ({} total versions)",
                            registry.size(), registry.totalVersionCount());
                    
                    if (registry.size() == 0 && properties.getRegistry().isFailOnEmpty()) {
                        throw new IllegalStateException("No process plugins found and failOnEmpty is true");
                    }
                })
                .doOnError(error -> log.error("Failed to initialize plugin system", error))
                .subscribe();
    }
    
    /**
     * Default ProcessMappingService implementation.
     * Returns a vanilla mapping that uses the operationId as the processId.
     */
    private static class DefaultProcessMappingService implements ProcessMappingService {
        
        @Override
        public Mono<com.firefly.common.application.plugin.ProcessMapping> resolveMapping(
                java.util.UUID tenantId,
                String operationId,
                java.util.UUID productId,
                String channelType) {
            
            // Default: use operationId as processId (vanilla fallback)
            return Mono.just(com.firefly.common.application.plugin.ProcessMapping.builder()
                    .operationId(operationId)
                    .processId(operationId)  // Same as operationId for vanilla
                    .build());
        }
        
        @Override
        public Mono<Void> invalidateCache(java.util.UUID tenantId) {
            return Mono.empty();
        }
    }
}
