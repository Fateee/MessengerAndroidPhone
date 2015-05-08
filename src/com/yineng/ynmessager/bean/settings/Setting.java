//***************************************************************
//*    2015-4-24  上午10:10:42
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：sumknot@foxmail.com
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.bean.settings;

/**
 * 用户的相关设置
 * 
 * @author 贺毅柳
 * 
 */
public class Setting
{
	private int id; // 主键自增ID（其实数据库该表中应该只会有一条数据，所以id也只会是1）
	private int distractionFree; // 是否开启免打扰
	private int distractionFree_begin_h; // 免打扰开始小时
	private int distractionFree_begin_m; // 免打扰开始分钟
	private int distractionFree_end_h; // 免打扰结束小时
	private int distractionFree_end_m; // 免打扰结束分钟
	private int audio; // 是否开启声音
	private int audio_group; // 是否开启群讨论组声音
	private int vibrate; // 是否震动
	private int vibrate_group; // 是否开启群讨论组震动
	private int receiveWhenExit; // 是否退出后仍然接收消息
	private int fontSize; // 字体大小
	private int skin; // 皮肤
	private int alwaysAutoReceiveImg; // 移动网络下是否自动接收图片
	/**
	 * @return the id
	 */
	public int getId()
	{
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id)
	{
		this.id = id;
	}
	/**
	 * @return the distractionFree
	 */
	public int getDistractionFree()
	{
		return distractionFree;
	}
	/**
	 * @param distractionFree the distractionFree to set
	 */
	public void setDistractionFree(int distractionFree)
	{
		this.distractionFree = distractionFree;
	}
	/**
	 * @return the distractionFree_begin_h
	 */
	public int getDistractionFree_begin_h()
	{
		return distractionFree_begin_h;
	}
	/**
	 * @param distractionFree_begin_h the distractionFree_begin_h to set
	 */
	public void setDistractionFree_begin_h(int distractionFree_begin_h)
	{
		this.distractionFree_begin_h = distractionFree_begin_h;
	}
	/**
	 * @return the distractionFree_begin_m
	 */
	public int getDistractionFree_begin_m()
	{
		return distractionFree_begin_m;
	}
	/**
	 * @param distractionFree_begin_m the distractionFree_begin_m to set
	 */
	public void setDistractionFree_begin_m(int distractionFree_begin_m)
	{
		this.distractionFree_begin_m = distractionFree_begin_m;
	}
	/**
	 * @return the distractionFree_end_h
	 */
	public int getDistractionFree_end_h()
	{
		return distractionFree_end_h;
	}
	/**
	 * @param distractionFree_end_h the distractionFree_end_h to set
	 */
	public void setDistractionFree_end_h(int distractionFree_end_h)
	{
		this.distractionFree_end_h = distractionFree_end_h;
	}
	/**
	 * @return the distractionFree_end_m
	 */
	public int getDistractionFree_end_m()
	{
		return distractionFree_end_m;
	}
	/**
	 * @param distractionFree_end_m the distractionFree_end_m to set
	 */
	public void setDistractionFree_end_m(int distractionFree_end_m)
	{
		this.distractionFree_end_m = distractionFree_end_m;
	}
	/**
	 * @return the audio
	 */
	public int getAudio()
	{
		return audio;
	}
	/**
	 * @param audio the audio to set
	 */
	public void setAudio(int audio)
	{
		this.audio = audio;
	}
	/**
	 * @return the audio_group
	 */
	public int getAudio_group()
	{
		return audio_group;
	}
	/**
	 * @param audio_group the audio_group to set
	 */
	public void setAudio_group(int audio_group)
	{
		this.audio_group = audio_group;
	}
	/**
	 * @return the vibrate
	 */
	public int getVibrate()
	{
		return vibrate;
	}
	/**
	 * @param vibrate the vibrate to set
	 */
	public void setVibrate(int vibrate)
	{
		this.vibrate = vibrate;
	}
	/**
	 * @return the vibrate_group
	 */
	public int getVibrate_group()
	{
		return vibrate_group;
	}
	/**
	 * @param vibrate_group the vibrate_group to set
	 */
	public void setVibrate_group(int vibrate_group)
	{
		this.vibrate_group = vibrate_group;
	}
	/**
	 * @return the receiveWhenExit
	 */
	public int getReceiveWhenExit()
	{
		return receiveWhenExit;
	}
	/**
	 * @param receiveWhenExit the receiveWhenExit to set
	 */
	public void setReceiveWhenExit(int receiveWhenExit)
	{
		this.receiveWhenExit = receiveWhenExit;
	}
	/**
	 * @return the fontSize
	 */
	public int getFontSize()
	{
		return fontSize;
	}
	/**
	 * @param fontSize the fontSize to set
	 */
	public void setFontSize(int fontSize)
	{
		this.fontSize = fontSize;
	}
	/**
	 * @return the skin
	 */
	public int getSkin()
	{
		return skin;
	}
	/**
	 * @param skin the skin to set
	 */
	public void setSkin(int skin)
	{
		this.skin = skin;
	}
	/**
	 * @return the alwaysAutoReceiveImg
	 */
	public int getAlwaysAutoReceiveImg()
	{
		return alwaysAutoReceiveImg;
	}
	/**
	 * @param alwaysAutoReceiveImg the alwaysAutoReceiveImg to set
	 */
	public void setAlwaysAutoReceiveImg(int alwaysAutoReceiveImg)
	{
		this.alwaysAutoReceiveImg = alwaysAutoReceiveImg;
	}

}
