package org.redis.demo.nw.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

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
public class EmployeeTerritory extends BaseEntity implements Externalizable
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
	
	/**
	 * Writes the state of this object to the given <code>ObjectOutput</code>.
	 */
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeUTF(employeeId);
		out.writeUTF(territoryId);
	}

	/**
	 * Reads the state of this object from the given <code>ObjectInput</code>.
	 */
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		this.employeeId = in.readUTF();
		this.territoryId = in.readUTF();
	}
    
	@Override
	public String toString()
	{
		return "[employeeId=" + this.employeeId
			 + ", territoryId=" + this.territoryId + "]";
	}
}
