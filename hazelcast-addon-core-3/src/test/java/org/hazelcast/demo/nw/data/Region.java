package org.hazelcast.demo.nw.data;

import java.io.IOException;


import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.nio.serialization.VersionedPortable;

/**
  * Region is generated code. To modify this class, you must follow the
  * guidelines below.
  * <ul>
  * <li>Always add new fields and do NOT delete old fields.</li>
  * <li>If new fields have been added, then make sure to increment the version number.</li>
  * </ul>
  *
  * @generator com.netcrest.pado.tools.hazelcast.VersionedPortableClassGenerator
  * @schema regions.schema
  * @date Fri May 17 20:50:06 EDT 2019
**/
public class Region extends BaseEntity implements VersionedPortable
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
	public int getClassId() 
	{
		return PortableFactoryImpl.Region_CLASS_ID;
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
		writer.writeUTF("regionId", regionId);
		writer.writeUTF("regionDescription", regionDescription);
	}

	@Override
	public void readPortable(PortableReader reader) throws IOException {
		super.readPortable(reader);
		this.regionId = reader.readUTF("regionId");
		this.regionDescription = reader.readUTF("regionDescription");
	}
    
	@Override
	public String toString()
	{
		return "[regionDescription=" + this.regionDescription
			 + ", regionId=" + this.regionId + "]";
	}
}
