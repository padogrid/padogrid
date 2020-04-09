package org.apache.geode.addon.demo.nw.data;

import org.apache.geode.pdx.PdxReader;
import org.apache.geode.pdx.PdxSerializable;
import org.apache.geode.pdx.PdxWriter;

public class Territory implements PdxSerializable
{
	private String territoryId;
	private String territoryDescription;
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
		writer.writeString("territoryId", territoryId);
		writer.writeString("territoryDescription", territoryDescription);
		writer.writeString("regionId", regionId);
	}

	@Override
	public void fromData(PdxReader reader) {
		this.territoryId = reader.readString("territoryId");
		this.territoryDescription = reader.readString("territoryDescription");
		this.regionId = reader.readString("regionId");
	}
}
