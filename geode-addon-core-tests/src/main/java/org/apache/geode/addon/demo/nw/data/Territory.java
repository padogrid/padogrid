package org.apache.geode.addon.demo.nw.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.geode.pdx.PdxReader;
import org.apache.geode.pdx.PdxSerializable;
import org.apache.geode.pdx.PdxWriter;

@Entity
@Table(name = "territories")
public class Territory extends BaseEntity implements PdxSerializable
{
	@Id
	private String territoryId;
	@Column(length = 100)
	private String territoryDescription;
	@Column(length = 30)
	private String regionId;

	public Territory()
	{
	}

	public void setTerritoryId(String territoryId) {
		this.territoryId=territoryId;
	}

	public String getTerritoryId() {
		return this.territoryId;
	}

	public void setTerritoryDescription(String territoryDescription) {
		this.territoryDescription=territoryDescription;
	}

	public String getTerritoryDescription() {
		return this.territoryDescription;
	}

	public void setRegionId(String regionId) {
		this.regionId=regionId;
	}

	public String getRegionId() {
		return this.regionId;
	}
    
	@Override
	public String toString()
	{
		return "[regionId=" + this.regionId
			 + ", territoryDescription=" + this.territoryDescription
			 + ", territoryId=" + this.territoryId + "]";
	}

	@Override
	public void toData(PdxWriter writer) {
		super.toData(writer);
		writer.writeString("territoryId", territoryId).markIdentityField("territoryId");
		writer.writeString("territoryDescription", territoryDescription);
		writer.writeString("regionId", regionId);
	}

	@Override
	public void fromData(PdxReader reader) {
		super.fromData(reader);
		this.territoryId = reader.readString("territoryId");
		this.territoryDescription = reader.readString("territoryDescription");
		this.regionId = reader.readString("regionId");
	}
}
