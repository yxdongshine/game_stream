package com.game.Mail;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

public final class MailBody {
	
	public enum ContentType {
		HTML("text/html;charset=UTF-8"), TEXT("text/plain;charset=UTF-8");
		public String value;
		ContentType(String value) {
			this.value = value;
		}
	}
	
	// 主题
	private String subject;
	// 内容
	private String content;
	// 内容类型
	private ContentType contentType = ContentType.HTML;
	// 内容中的图片
	private List<Map<String, Object>> images = new ArrayList<Map<String, Object>>();
	// 附件
	private List<Map<String, Object>> attachments = new ArrayList<Map<String, Object>>();

	/**
	 * 添加图片
	 * 
	 * @param image
	 * @param cid
	 * @author YXD
	 */
	public void addImage(File image, String cid) {
		if(!image.exists() || image.isDirectory()){
			throw new RuntimeException("file is not exist or is directory");
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("image", image);
		map.put("cid", cid);
		images.add(map);
	}
	
	/**
	 * 添加附件
	 * @param attachment
	 * @author YXD
	 */
	public void addAttachment(File attachment){
		if(!attachment.exists() || attachment.isDirectory()){
			throw new RuntimeException("file is not exist or is directory");
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("attachment", attachment);
		map.put("alias", attachment.getName());
		attachments.add(map);
	}
	
	/**
	 * 添加附件
	 * @param attachment
	 * @param alias
	 * @author YXD
	 */
	public void addAttachment(File attachment, String alias){
		if(!attachment.exists() || attachment.isDirectory()){
			throw new RuntimeException("file is not exist or is directory");
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("attachment", attachment);
		map.put("alias", alias);
		attachments.add(map);
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent() {
		if (content == null || content.equals("")) {
			content = "";
		}
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public ContentType getContentType() {
		return contentType;
	}

	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}

	public List<Map<String, Object>> getImages() {
		return images;
	}

	public List<Map<String, Object>> getAttachments() {
		return attachments;
	}

	@Override
	public String toString() {
		return JSONObject.toJSONString(this);
	}

}