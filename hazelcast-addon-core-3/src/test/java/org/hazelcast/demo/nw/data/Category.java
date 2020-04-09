package org.hazelcast.demo.nw.data;

import java.io.IOException;


import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.nio.serialization.VersionedPortable;

/**
  * Category is generated code. To modify this class, you must follow the
  * guidelines below.
  * <ul>
  * <li>Always add new fields and do NOT delete old fields.</li>
  * <li>If new fields have been added, then make sure to increment the version number.</li>
  * </ul>
  *
  * @generator com.netcrest.pado.tools.hazelcast.VersionedPortableClassGenerator
  * @schema categories.schema
  * @date Fri May 17 20:50:06 EDT 2019
**/
public class Category implements VersionedPortable
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
	public int getClassId() 
	{
		return PortableFactoryImpl.Category_CLASS_ID;
	}

	@Override
	public int getFactoryId() {
		return PortableFactoryImpl.FACTORY_ID;
	}
	
	@Override
	public int getClassVersion() {
		return 1;
	}

	@Override
	public void writePortable(PortableWriter writer) throws IOException {
		writer.writeUTF("categoryId", categoryId);
		writer.writeUTF("categoryName", categoryName);
		writer.writeUTF("description", description);
		writer.writeUTF("tag", tag);
		writer.writeUTF("picture", picture);
	}

	@Override
	public void readPortable(PortableReader reader) throws IOException {
		this.categoryId = reader.readUTF("categoryId");
		this.categoryName = reader.readUTF("categoryName");
		this.description = reader.readUTF("description");
		this.tag = reader.readUTF("tag");
		this.picture = reader.readUTF("picture");
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
}
