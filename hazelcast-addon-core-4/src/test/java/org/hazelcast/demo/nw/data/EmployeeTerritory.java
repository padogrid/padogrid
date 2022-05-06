package org.hazelcast.demo.nw.data;

import java.io.IOException;


import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.nio.serialization.VersionedPortable;

/**
  * EmployeeTerritory is generated code. To modify this class, you must follow the
  * guidelines below.
  * <ul>
  * <li>Always add new fields and do NOT delete old fields.</li>
  * <li>If new fields have been added, then make sure to increment the version number.</li>
  * </ul>
  *
  * @generator com.netcrest.pado.tools.hazelcast.VersionedPortableClassGenerator
  * @schema employee_territories.schema
  * @date Fri May 17 20:50:06 EDT 2019
**/
public class EmployeeTerritory extends BaseEntity implements VersionedPortable
{
	private String employeeId;
	private String territoryId;

	public EmployeeTerritory()
	{
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId=employeeId;
	}

	public String getEmployeeId() {
		return this.employeeId;
	}

	public void setTerritoryId(String territoryId) {
		this.territoryId=territoryId;
	}

	public String getTerritoryId() {
		return this.territoryId;
	}


	@Override
	public int getClassId() 
	{
		return PortableFactoryImpl.EmployeeTerritory_CLASS_ID;
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
		writer.writeUTF("employeeId", employeeId);
		writer.writeUTF("territoryId", territoryId);
	}

	@Override
	public void readPortable(PortableReader reader) throws IOException {
		super.readPortable(reader);
		this.employeeId = reader.readUTF("employeeId");
		this.territoryId = reader.readUTF("territoryId");
	}
    
	@Override
	public String toString()
	{
		return "[employeeId=" + this.employeeId
			 + ", territoryId=" + this.territoryId + "]";
	}
}
