package org.oracle.coherence.addon.demo.nw.data;

import java.io.IOException;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

public class Category implements PortableObject {
	private String categoryId;
	private String categoryName;
	private String description;
	private String tag;
	private String picture;

	public Category() {
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	public String getCategoryId() {
		return this.categoryId;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getCategoryName() {
		return this.categoryName;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return this.description;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getTag() {
		return this.tag;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

	public String getPicture() {
		return this.picture;
	}

	@Override
	public String toString() {
		return "[categoryId=" + this.categoryId + ", categoryName=" + this.categoryName + ", description="
				+ this.description + ", picture=" + this.picture + ", tag=" + this.tag + "]";
	}

	@Override
	public void readExternal(PofReader reader) throws IOException {
		int i = 0;
		this.categoryId = reader.readString(i++);
		this.categoryName = reader.readString(i++);
		this.description = reader.readString(i++);
		this.tag = reader.readString(i++);
		this.picture = reader.readString(i++);
	}

	@Override
	public void writeExternal(PofWriter writer) throws IOException {
		int i = 0;
		writer.writeString(i++, categoryId);
		writer.writeString(i++, categoryName);
		writer.writeString(i++, description);
		writer.writeString(i++, tag);
		writer.writeString(i++, picture);
	}
}
