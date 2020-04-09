package org.apache.geode.addon.demo.nw.data;

import org.apache.geode.pdx.PdxReader;
import org.apache.geode.pdx.PdxSerializable;
import org.apache.geode.pdx.PdxWriter;

public class Region implements PdxSerializable
{
	private String regionId;
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
		writer.writeString("regionId", regionId);
		writer.writeString("regionDescription", regionDescription);
	}

	@Override
	public void fromData(PdxReader reader) {
		this.regionId = reader.readString("regionId");
		this.regionDescription = reader.readString("regionDescription");
	}
}
