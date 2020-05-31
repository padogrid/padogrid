package org.oracle.coherence.addon.demo.nw.data;

import java.io.IOException;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

public class Region implements PortableObject
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
	public void readExternal(PofReader reader) throws IOException {
		int i = 0;
		this.regionId = reader.readString(i++);
		this.regionDescription = reader.readString(i++);
	}

	@Override
	public void writeExternal(PofWriter writer) throws IOException {
		int i = 0;
		writer.writeString(i++, regionId);
		writer.writeString(i++, regionDescription);
	}
}
