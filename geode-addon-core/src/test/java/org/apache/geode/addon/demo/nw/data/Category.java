package org.apache.geode.addon.demo.nw.data;

import org.apache.geode.pdx.PdxReader;
import org.apache.geode.pdx.PdxSerializable;
import org.apache.geode.pdx.PdxWriter;

public class Category implements PdxSerializable
{
	private String categoryId;
	private String categoryName;
	private String description;
	private String tag;
	private String picture;

	public Category()
	{
	}

	public void setCategoryId(String categoryId) {
		this.categoryId=categoryId;
	}

	public String getCategoryId() {
		return this.categoryId;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName=categoryName;
	}

	public String getCategoryName() {
		return this.categoryName;
	}

	public void setDescription(String description) {
		this.description=description;
	}

	public String getDescription() {
		return this.description;
	}

	public void setTag(String tag) {
		this.tag=tag;
	}

	public String getTag() {
		return this.tag;
	}

	public void setPicture(String picture) {
		this.picture=picture;
	}

	public String getPicture() {
		return this.picture;
	}

	@Override
	public String toString()
	{
		return "[categoryId=" + this.categoryId
			 + ", categoryName=" + this.categoryName
			 + ", description=" + this.description
			 + ", picture=" + this.picture
			 + ", tag=" + this.tag + "]";
	}

	@Override
	public void toData(PdxWriter writer) {
		writer.writeString("catetoryId", categoryId);
		writer.writeString("categoryName", categoryName);
		writer.writeString("description", description);
		writer.writeString("tag", tag);
		writer.writeString("picture", picture);
	}

	@Override
	public void fromData(PdxReader reader) {
		this.categoryId = reader.readString("categoryId");
		this.categoryName = reader.readString("categoryName");
		this.description = reader.readString("description");
		this.tag = reader.readString("tag");
		this.picture = reader.readString("picture");
	}
}
