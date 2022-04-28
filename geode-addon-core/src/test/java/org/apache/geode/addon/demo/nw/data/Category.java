package org.apache.geode.addon.demo.nw.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.geode.pdx.PdxReader;
import org.apache.geode.pdx.PdxSerializable;
import org.apache.geode.pdx.PdxWriter;

@Entity
@Table(name = "categories")
public class Category extends BaseEntity implements PdxSerializable
{
	@Id
	private String categoryId;
	@Column(length = 100)
	private String categoryName;
	@Column(length = 100)
	private String description;
	@Column(length = 100)
	private String tag;
	@Column(length = 1000)
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
		super.toData(writer);
		writer.writeString("catetoryId", categoryId);
		writer.writeString("categoryName", categoryName);
		writer.writeString("description", description);
		writer.writeString("tag", tag);
		writer.writeString("picture", picture);
	}

	@Override
	public void fromData(PdxReader reader) {
		super.fromData(reader);
		this.categoryId = reader.readString("categoryId");
		this.categoryName = reader.readString("categoryName");
		this.description = reader.readString("description");
		this.tag = reader.readString("tag");
		this.picture = reader.readString("picture");
	}
}
