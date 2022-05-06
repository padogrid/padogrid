package org.hazelcast.demo.nw.data;

import java.io.IOException;


import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.nio.serialization.VersionedPortable;

/**
  * Territory is generated code. To modify this class, you must follow the
  * guidelines below.
  * <ul>
  * <li>Always add new fields and do NOT delete old fields.</li>
  * <li>If new fields have been added, then make sure to increment the version number.</li>
  * </ul>
  *
  * @generator com.netcrest.pado.tools.hazelcast.VersionedPortableClassGenerator
  * @schema territories.schema
  * @date Fri May 17 20:50:06 EDT 2019
**/
public class Territory extends BaseEntity implements VersionedPortable
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
	public int getClassId() 
	{
		return PortableFactoryImpl.Territory_CLASS_ID;
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
		super.writePortable(writer);
		writer.writeUTF("territoryId", territoryId);
		writer.writeUTF("territoryDescription", territoryDescription);
		writer.writeUTF("regionId", regionId);
	}

	@Override
	public void readPortable(PortableReader reader) throws IOException {
		super.readPortable(reader);
		this.territoryId = reader.readUTF("territoryId");
		this.territoryDescription = reader.readUTF("territoryDescription");
		this.regionId = reader.readUTF("regionId");
	}
    
	@Override
	public String toString()
	{
		return "[regionId=" + this.regionId
			 + ", territoryDescription=" + this.territoryDescription
			 + ", territoryId=" + this.territoryId + "]";
	}
}
