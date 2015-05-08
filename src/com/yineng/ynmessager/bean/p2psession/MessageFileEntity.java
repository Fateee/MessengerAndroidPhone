package com.yineng.ynmessager.bean.p2psession;

public class MessageFileEntity {
	private String Key;
	private String Name;
	private String Size;
	private String FileType;
	private String FileId;

	public String getKey() {
		return Key;
	}

	public void setKey(String key) {
		Key = key;
	}

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public String getSize() {
		return Size;
	}

	public void setSize(String size) {
		Size = size;
	}

	public String getFileType() {
		return FileType;
	}

	public void setFileType(String fileType) {
		FileType = fileType;
	}

	public String getFileId() {
		return FileId;
	}

	public void setFileId(String fileId) {
		FileId = fileId;
	}
}
