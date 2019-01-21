package com.itcall.batch.config.cache;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@EnableCaching
@Lazy // 업무에서 정의한 모든 캐쉬서비스가 컨테이너에 적체된 후 수행한다.
public class CacheConfig {

	private static final Logger LOG = LoggerFactory.getLogger(CacheConfig.class);

	private static final List<Cache> cacheList = new ArrayList<Cache>();

	private static ApplicationContext ctx;

	@Bean("cacheManager")
	public CacheManager cacheManager(ApplicationContext ctx) {
		CacheConfig.ctx = ctx;
		SimpleCacheManager simpleCacheManager = new SimpleCacheManager();
		String[] cacheBeanNames = ctx.getBeanNamesForType(BaseCache.class);
		if(cacheBeanNames!=null) {
			for (String beanName : cacheBeanNames) {
				String cacheName = ((BaseCache<?>)ctx.getBean(beanName)).getCacheName();
				cacheList.add(new ConcurrentMapCache(cacheName));
				LOG.debug("Cache manager Added to CacheData[{}] from Conainer[{}]", cacheName, beanName);
			}
		}
		simpleCacheManager.setCaches(cacheList);
		LOG.info("CacheManager loading complited...[{}]", simpleCacheManager);
		return simpleCacheManager;
	}

	public static List<Cache> getCacheList() {
		return cacheList;
	}

	public static boolean clearAllCache() {return clearCache(null);}
	public static boolean clearCache(String cacheName) {
		boolean result = false;
		for (Cache cache : cacheList) {
			if(cacheName==null || cache.getName().equals(cacheName)) {
				cache.clear();
				result = true;
			}
		}
		return result;
	}

	public static boolean reloadAllCache() {return reloadCache(null);}
	public static boolean reloadCache(String cacheName) {
		boolean result = false;
		clearCache(cacheName);
		String[] beanNames = ctx.getBeanNamesForType(BaseCache.class);
		for (String beanName : beanNames) {
			String findCacheName = ((BaseCache<?>)ctx.getBean(beanName)).getCacheName();
			if(cacheName==null || cacheName.equals(findCacheName)) {
				((BaseCache<?>)ctx.getBean(beanName)).getCacheData();
				result = true;
			}
		}
		return result;
	}

}
