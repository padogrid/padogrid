package org.apache.geode.addon.demo.nw.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.geode.pdx.PdxReader;
import org.apache.geode.pdx.PdxSerializable;
import org.apache.geode.pdx.PdxWriter;

@Entity
@Table(name = "regions")
public class Region extends BaseEntity implements PdxSerializable
{
	@Id
	private String regionId;
	@Column(length = 100)
	private String regionDescription;

	public Region()
	{
	}

	public void setRegionId(String regionId) {
		this.regionId=regionId;
	}

	public String getRegionId() {
		return this.regionId;
	}

	public void setRegionDescription(String regionDescription) {
		this.regionDescription=regionDescription;
	}

	public String getRegionDescription() {
		return this.regionDescription;
	}
    
	@Override
	public String toString()
	{
		return "[regionDescription=" + this.regionDescription
			 + ", regionId=" + this.regionId + "]";
	}

	@Override
	public void toData(PdxWriter writer) {
		super.toData(writer);
		writer.writeString("regionId", regionId).markIdentityField("regionId");
		writer.writeString("regionDescription", regionDescription);
	}

	@Override
	public void fromData(PdxReader reader) {
		super.fromData(reader);
		this.regionId = reader.readString("regionId");
		this.regionDescription = reader.readString("regionDescription");
	}
}
