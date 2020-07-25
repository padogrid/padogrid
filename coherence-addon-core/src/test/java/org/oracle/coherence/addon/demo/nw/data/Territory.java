package org.oracle.coherence.addon.demo.nw.data;

import java.io.IOException;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

public class Territory implements PortableObject
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
	public void readExternal(PofReader reader) throws IOException {
		int i = 0;
		this.territoryId = reader.readString(i++);
		this.territoryDescription = reader.readString(i++);
		this.regionId = reader.readString(i++);
	}

	@Override
	public void writeExternal(PofWriter writer) throws IOException {
		int i = 0;
		writer.writeString(i++, territoryId);
		writer.writeString(i++, territoryDescription);
		writer.writeString(i++, regionId);
	}
}
