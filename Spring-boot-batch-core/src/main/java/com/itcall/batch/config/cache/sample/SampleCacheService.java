package com.itcall.batch.config.cache.sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.itcall.batch.common.mapper.code.CommonCodeMapper;
import com.itcall.batch.config.cache.BaseCache;
import com.itcall.batch.config.support.SysCodeFlag;

@EnableScheduling
@Service("sampleCacheService")
public class SampleCacheService extends BaseCache<List<Map<String, Object>>> {

	/***************************************************************************
	 * <PRE>꼭.!!! Class에 선언한 bean이름과 <br>
	 * 동일한 이름을 CACHE_NAME으로 사용해야 한다.<br></PRE>
	 ***************************************************************************/
	private static final String CACHE_NAME = "sampleCacheService";

	@Resource
	private CommonCodeMapper commonCodeMapper;

	@Override
	public String getCacheName() {
		return CACHE_NAME;
	}

	@Override
	@Cacheable(value=CACHE_NAME /*, key="0"*/) // CACHE_NAME 키로 캐쉬가 저장된다. 인수가 없을경우 기본값은 "0"임
	protected List<Map<String, Object>> setCacheAndLoadData(){
		
		/********************************************************
		 * 캐쉬 reload timer 설정.(milliseconds)
		 ********************************************************/
		setClearAndReloadCacheTimer(SysCodeFlag.getCacheTimer());
		
		/*******************************************************
		 * Make a cacheDatas from anywhere...(normaly from Mapper)
		 * Will Test Map.Datas to return...
		 *******************************************************/
		List<Map<String, Object>> result = new ArrayList<>();
		while (System.currentTimeMillis()%3!=0) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("TestCacheKeyName", System.currentTimeMillis());
			map.put("TestCacheKeyNameTimers", System.currentTimeMillis()+" TestTimers");
			map.put("TestCacheKeyData", System.currentTimeMillis()+" TestData");
			result.add(map);
		}
		return result;
	}

//	@Override
//	@CacheEvict(value=CACHE_NAME /*, key="0"*/, allEntries=true) // CACHE_NAME 키로 저장된 모든 캐쉬키를 삭제한다. 인수가 없을경우 기본값은 "0"임
//	protected void clearCache() {
//		LOG.debug("CacheClear...[{}]", this.getClass().getName());
//	}





	/**********************************************
	 * 실제 업무적으로 제공할 전체 케쉬데이터
	 * @return loadedCacheData
	 **********************************************/
	public List<Map<String, Object>> getAllCacheData() {
		return getCacheData();
	}

	/**********************************************
	 * 전체 케쉬데이터에서 특정 조건으로 추출
	 * @return loadedCacheData
	 **********************************************/
	public Map<String, Object> getCacheData(String testCacheKeyName) {
		List<Map<String, Object>> cacheData =  getCacheData();
		if(cacheData!=null) {
			for (Map<String, Object> map : cacheData) {
				if(map.get("TestCacheKeyName")!=null && map.get("TestCacheKeyName") instanceof String && map.get("TestCacheKeyName").toString().equalsIgnoreCase(testCacheKeyName)) {
					return map;
				}
			}
		}
		return null;
	}

	/*****************************************
	 * 테스트를 위해서 주기적으로 호출함.
	 * @return
	 *****************************************/
	@Scheduled(fixedDelay=60 * 1000, initialDelay=10 * 1000) // (cron="0 0 * * * ?") // (fixedDelay=60_000)
	public List<Map<String, Object>> getCallTestCacheData() {
		String startYyyyMm = "201810";
		String endYyyyMm = "201810";
		
		long startTime = System.currentTimeMillis();
		LOG.warn("{}", commonCodeMapper.selectSysContlFlagList(null));
		LOG.warn("commonCodeMapper.selectSysContlFlagList(null) ::::  {}",  System.currentTimeMillis() - startTime );
		startTime = System.currentTimeMillis();
		LOG.warn("{}", commonCodeMapper.selectSysContlFlagList("TEST"));
		LOG.warn("commonCodeMapper.selectSysContlFlagList('TEST') ::::  {}", System.currentTimeMillis() - startTime );
		
		return getAllCacheData();
	}

}
