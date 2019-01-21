package com.itcall.batch.config.cache;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.TaskScheduler;

public abstract class BaseCache<T> {

	protected Logger LOG = LoggerFactory.getLogger(getClass());

	@javax.annotation.Resource
	private ApplicationContext ctx;

	@javax.annotation.Resource// (name="taskScheduler")
	private TaskScheduler taskScheduler;

	private ScheduledFuture<?> scheduledFuture;

	/*******************************************************************
	 * CacheData를 저장하는 정보의 기준이되는 CacheName을 반환한다.
	 * 여기서 반환되는 캐쉬이름으로 BeanName,CacheName등으로
	 * 동일하게 사용해야 한다.
	 *******************************************************************/
	public abstract String getCacheName();

	/*******************************************************************
	 * 최초에만 Method 내부의 코드가 실행되어 CacheData를 로드한다.
	 * 이후 clear Method가 호출되기전까지 캐쉬된 데이터만 반환된다.
	 * 구현 시 @Cacheable(value=CACHE_NAME)를 꼭 기입하고
	 * CACHE_NAME은 getCacheName()에서 반환되는 값과 동일하여야 한다.
	 * Container에 등록되는 bean이름도 동일하게 맞춰주는것을 권장한다.
	 *******************************************************************/
	protected abstract T setCacheAndLoadData();

	protected void setClearAndReloadCacheTimer(final long delayMillSeconds) {
		final BaseCache<T> cls = this;
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				ctx.getBean(cls.getClass()).clearCache();
				LOG.info("Cache.Clear [{}] CacheInfo[{}]", getCacheName(), cls.getClass());
				ctx.getBean(cls.getClass()).setCacheAndLoadData();
				LOG.info("Cache.Loaded [{}] cacheData...keepTimer[{}]ms, CacheInfo[{}]", getCacheName(), delayMillSeconds,  cls.getClass());
			}
		};
		ScheduledFuture<?> scheduledFuture = this.taskScheduler.schedule(runnable, new Date(System.currentTimeMillis() + delayMillSeconds));
		LOG.info("Cache.addReflash.Timer[{}],  CacheName[{}] CacheInfo[{}]", delayMillSeconds, getCacheName(), cls.getClass());
		this.scheduledFuture = changeScheduleFuture(scheduledFuture);
	}

	/*******************************************************************
	 * Cache된 Data를 삭제한다.
	 *******************************************************************/
	protected boolean clearCache() {
		return CacheConfig.clearCache(getCacheName());
	}

	@SuppressWarnings("unchecked")
	public T getCacheData(){
		return (T) getCtx().getBean(this.getClass()).setCacheAndLoadData();
	}

	protected ApplicationContext getCtx() {
		return ctx;
	}

	private synchronized ScheduledFuture<?> changeScheduleFuture(ScheduledFuture<?> scheduledFuture) {
		if(this.scheduledFuture!=null && !this.scheduledFuture.isDone()) {
			this.scheduledFuture.cancel(true);
		}
		return scheduledFuture;
	}

}
