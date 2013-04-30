package com.idega.mobile.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.idega.util.CoreConstants;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PayloadItem implements Serializable {

	private static final long serialVersionUID = 4816512993351712600L;

	private String key, value;

	public PayloadItem() {
		super();
	}

	public PayloadItem(String key, String value) {
		this();

		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return CoreConstants.EMPTY + getKey() + ": " + getValue();
	}

}