package org.gluu.casa.core.pojo;

public class ColumnDatatables {

	private String data;
	private String title;
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public ColumnDatatables(String data, String title) {
		super();
		this.data = data;
		this.title = title;
	}
	
}
