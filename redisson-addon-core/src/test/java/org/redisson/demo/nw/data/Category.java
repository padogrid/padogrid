package org.redisson.demo.nw.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

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
public class Category extends BaseEntity implements Externalizable
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
	
	/**
	 * Writes the state of this object to the given <code>ObjectOutput</code>.
	 */
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeUTF(categoryId);
		out.writeUTF(categoryName);
		out.writeUTF(description);
		out.writeUTF(tag);
		out.writeUTF(picture);
	}

	/**
	 * Reads the state of this object from the given <code>ObjectInput</code>.
	 */
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		this.categoryId = in.readUTF();
		this.categoryName = in.readUTF();
		this.description = in.readUTF();
		this.tag = in.readUTF();
		this.picture = in.readUTF();
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
