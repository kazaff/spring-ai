package org.springframework.ai.mcp;

import io.modelcontextprotocol.spec.McpServerAuthenticator;
import io.modelcontextprotocol.spec.ToolFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.ToolId;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kazaff
 */
public class AutoToolFilter implements ToolFilter, SmartInitializingSingleton {

	private static final Logger logger = LoggerFactory.getLogger(AutoToolFilter.class);

	private final ApplicationContext applicationContext;

	private final Map<String, Map<String, String>> toolNameToPermissionKeyCache = new ConcurrentHashMap<>();

	public AutoToolFilter(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public void afterSingletonsInstantiated() throws BeansException {
		initializeToolPermissionCache();
	}

	// TODO 该方法是否能扫描出所有 @Tool 呢？是否所有包含 @Tool 的类都一定是 Bean 呢？
	private void initializeToolPermissionCache() {
		try {
			String[] beanNames = applicationContext.getBeanDefinitionNames();
			int toolCount = 0;

			for (String beanName : beanNames) {
				Object bean = applicationContext.getBean(beanName);
				Method[] methods = bean.getClass().getDeclaredMethods();

				for (Method method : methods) {
					if (method.isAnnotationPresent(Tool.class)) {
						Tool toolAnnotation = method.getAnnotation(Tool.class);
						String toolName = toolAnnotation.name();

						toolName = toolName.isEmpty() ? method.getName() : toolName;

						if (method.isAnnotationPresent(ToolId.class)) {
							ToolId toolIdAnnotation = method.getAnnotation(ToolId.class);

							Map<String, String> toolAccess = new HashMap<>();

							toolAccess.put("id", toolIdAnnotation.value());
							toolAccess.put("category", toolIdAnnotation.category());

							toolNameToPermissionKeyCache.put(toolName, toolAccess);
							toolCount++;
						}
					}
				}
			}

			logger.info("工具权限映射缓存初始化完成，共缓存 {} 个工具", toolCount);
		}
		catch (Exception e) {
			logger.error("初始化工具权限映射缓存失败", e);
		}
	}

	@Override
	public boolean accept(String toolName, String token, McpServerAuthenticator authenticator) {
		Map<String, String> toolAccess = this.getPermissionKeyFromCache(toolName);
		return toolAccess != null && authenticator.authorize(token, toolAccess.get("id"), toolAccess.get("category"));
	}

	private Map<String, String> getPermissionKeyFromCache(String toolName) {
		Map<String, String> toolAccess = toolNameToPermissionKeyCache.get(toolName);
		if (toolAccess != null) {
			return toolAccess;
		}

		logger.debug("缓存中未找到工具权限映射，使用工具名称作为权限标识: toolName={}", toolName);
		return null;
	}

}
