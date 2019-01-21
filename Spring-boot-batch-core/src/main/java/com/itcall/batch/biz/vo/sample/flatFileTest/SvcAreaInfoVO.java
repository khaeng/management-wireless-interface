package com.itcall.batch.biz.vo.sample.flatFileTest;

public class SvcAreaInfoVO {

	private final String testFinal = "";
	private static String testStatic = "";
	private String dongCd;
	private String addrNoType;
	private String startAddrNo;
	private String startAddrHo;
	private String endAddrNo;
	private String endAdddrHo;
	private String acptOfcCd;
	private String rssFlag;
	
	public String getDongCd() {
		return dongCd;
	}
	public void setDongCd(String dongCd) {
		this.dongCd = dongCd;
	}
	public String getAddrNoType() {
		return addrNoType;
	}
	public void setAddrNoType(String addrNoType) {
		this.addrNoType = addrNoType;
	}
	public String getStartAddrNo() {
		return startAddrNo;
	}
	public void setStartAddrNo(String startAddrNo) {
		this.startAddrNo = startAddrNo;
	}
	public String getStartAddrHo() {
		return startAddrHo;
	}
	public void setStartAddrHo(String startAddrHo) {
		this.startAddrHo = startAddrHo;
	}
	public String getEndAddrNo() {
		return endAddrNo;
	}
	public void setEndAddrNo(String endAddrNo) {
		this.endAddrNo = endAddrNo;
	}
	public String getEndAdddrHo() {
		return endAdddrHo;
	}
	public void setEndAdddrHo(String endAdddrHo) {
		this.endAdddrHo = endAdddrHo;
	}
	public String getAcptOfcCd() {
		return acptOfcCd;
	}
	public void setAcptOfcCd(String acptOfcCd) {
		this.acptOfcCd = acptOfcCd;
	}
	public String getRssFlag() {
		return rssFlag;
	}
	public void setRssFlag(String rssFlag) {
		this.rssFlag = rssFlag;
	}
	@Override
	public String toString() {
		return "SvcAreaInfoVO [dongCd=" + dongCd + ", addrNoType=" + addrNoType + ", startAddrNo=" + startAddrNo
				+ ", startAddrHo=" + startAddrHo + ", endAddrNo=" + endAddrNo + ", endAdddrHo=" + endAdddrHo
				+ ", acptOfcCd=" + acptOfcCd + ", rssFlag=" + rssFlag + "]";
	}

}
