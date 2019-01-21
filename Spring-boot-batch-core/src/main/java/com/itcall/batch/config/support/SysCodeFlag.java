package com.itcall.batch.config.support;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.itcall.batch.common.mapper.code.CommonCodeMapper;

@Component
public class SysCodeFlag {

	private final static Logger LOG = LoggerFactory.getLogger(SysCodeFlag.class);

	private final static long DEF_CACHE_TIMER_SECONDS = 1 * 60;
	private static List<Map<String, String>> sysCodeFlagList;
	private static long keepTimePosition = 0L;
	private static long keepTimerCache = 60 * 60 * 1000L;

	private static CommonCodeMapper staticCommonCodeMapper;
	@Resource
	private CommonCodeMapper commonCodeMapper;


	@PostConstruct
	@DependsOn({"commonCodeMapper"})
	public void initialize() {
		if(commonCodeMapper!=null) {
			SysCodeFlag.staticCommonCodeMapper = commonCodeMapper;
			reload();
		}
	}

	public static boolean clear() {
		keepTimePosition = 0L;
		return reload();
	}
	private static boolean reload() {
		if(staticCommonCodeMapper!=null && ( keepTimePosition <= 0 || (System.currentTimeMillis() - keepTimePosition) > keepTimerCache)) {
			try {
				sysCodeFlagList = staticCommonCodeMapper.selectSysContlFlagList(null);
				LOG.debug("Loading new System.Code.Flag List.Length[{}], at loaded time[{}], Before keeped time[{}]", sysCodeFlagList.size(), System.currentTimeMillis(), keepTimePosition);
			} catch (Exception e) {
				sysCodeFlagList = null;
				LOG.info("Loading failed from Database. Using default System.Code.Flag!!! at loaded time[{}], Before keeped time[{}]", System.currentTimeMillis(), keepTimePosition);
			} finally {
				keepTimePosition = System.currentTimeMillis();
				keepTimerCache = getCacheTimer(); // 기다릴 캐쉬를 읽어온 DB테이블에서 새로 정의한다.
			}
			return true;
		}
		return false;
	}
	private static String getSysCodeFlag(String sysKey) {
		reload();
		if(sysCodeFlagList!=null && sysCodeFlagList.size()>0) {
			for (Map<String, String> map : sysCodeFlagList) {
				if(map!=null && map.get("contlTgtId")!=null && map.get("contlTgtId").equalsIgnoreCase(sysKey)) {
					if(map.get("aplyYn")!=null)
						return map.get("aplyYn");
					else
						return "";
				}
			}
		}
		return "";
	}



	/**
	 * 지정된 ContlTgtId의 ROW의 APLY_YN값이 "N"인경우 false
	 * 이외의 경우는 true를 반환한다.
	 * @param sysKey
	 * @return
	 */
	public static boolean isUseSysCodeFlag(String sysKey) {
		if(getSysCodeFlag(sysKey).equalsIgnoreCase("N"))
			return false;
		return true;
	}

	/**
	 * 지정된 ContlTgtId의 ROW의 APLY_YN값이 있는경우 해당값을 반환하고,
	 * 이외의 경우는 defValue를 반환한다.
	 * @param sysKey
	 * @param defValue
	 * @return
	 */
	public static String getSysCodeFlag(String sysKey, String defValue) {
		String result = getSysCodeFlag(sysKey);
		if(result.isEmpty()) {
			return defValue;
		}
		return result;
	}

	/**
	 * 지정된 ContlTgtId의 ROW의 APLY_YN값이 "Y"인경우만 해당ROW의 APLY_TGT 값을 반환한다.
	 * 이외의 경우는 defValue를 반환한다.
	 * @param sysKey
	 * @param defValue
	 * @return
	 */
	public static String getAplyTgt(String sysKey, String defValue) {
		reload();
		if(sysCodeFlagList!=null && sysCodeFlagList.size()>0) {
			for (Map<String, String> map : sysCodeFlagList) {
				if(map!=null && map.get("contlTgtId")!=null && map.get("contlTgtId").equalsIgnoreCase(sysKey)) {
					if(map.get("aplyYn")!=null && map.get("aplyYn").equalsIgnoreCase("Y"))
						return map.get("aplyTgt");
					else
						return defValue;
				}
			}
		}
		return defValue;
	}

	/**
	 * Captcha 사용여부 조회
	 * @return
	 */
	public static boolean isUseCaptcha() {
		return isUseSysCodeFlag("CAPTCHA");
	}

	/**
	 * OTP 사용여부 조회
	 * @return
	 */
	public static boolean isUseOtp() {
		return isUseSysCodeFlag("OTP");
	}

	/**
	 * 현재 DB에 적용된 캐쉬 RELOAD 간격설정값을 MILLISECONDS 단위로 읽어온다.
	 * @return  ::: DB에 저장된 수치는 초단위로 저장된것으로 본다.
	 */
	public static long getCacheTimer() {
		try {
			return Long.parseLong(getSysCodeFlag("CACHE_TIMER",DEF_CACHE_TIMER_SECONDS + "")) * 1000;
		} catch (Exception e) {}
		return DEF_CACHE_TIMER_SECONDS * 1000L;
	}
	/**
	 * 현재 DB에 적용된 캐쉬 RELOAD 간격설정값을 TimeUnit을 지정하여 읽어온다.
	 * @param timeUnit  ::: DB에 저장된 수치는 초단위로 저장된것으로 본다.
	 * @return
	 */
	public static long getCacheTimer(TimeUnit timeUnit) {
		long cacheTimer = DEF_CACHE_TIMER_SECONDS; // 한시간, 초단위.
		try {
			String timerDelayValue = getSysCodeFlag("CACHE_TIMER", cacheTimer + ""); // 기본 초단위 : 한시간
			cacheTimer = Long.parseLong(timerDelayValue);
		} catch (Exception e) {}finally {
			if(cacheTimer <= 0L) {
				cacheTimer = DEF_CACHE_TIMER_SECONDS; // 한시간, 초단위.
			}
		}
		switch (timeUnit) {
		case DAYS:
			cacheTimer = TimeUnit.SECONDS.toDays(cacheTimer);
			break;
		case HOURS:
			cacheTimer = TimeUnit.SECONDS.toHours(cacheTimer);
			break;
		case MINUTES:
			cacheTimer = TimeUnit.SECONDS.toMinutes(cacheTimer);
			break;
		case MICROSECONDS:
			cacheTimer = TimeUnit.SECONDS.toMicros(cacheTimer);
			break;
		case MILLISECONDS:
			cacheTimer = TimeUnit.SECONDS.toMillis(cacheTimer);
			break;
		case NANOSECONDS:
			cacheTimer = TimeUnit.SECONDS.toNanos(cacheTimer);
			break;
		default:
			break;
		}
		if(cacheTimer <= 0L) {
			cacheTimer=1L;
		}
		return cacheTimer;
	}

}
