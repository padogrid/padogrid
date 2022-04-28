package org.oracle.coherence.addon.demo.nw.data;

import java.io.IOException;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

public class EmployeeTerritory extends BaseEntity implements PortableObject {
	private String employeeId;
	private String territoryId;

	public EmployeeTerritory() {
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}

	public String getEmployeeId() {
		return this.employeeId;
	}

	public void setTerritoryId(String territoryId) {
		this.territoryId = territoryId;
	}

	public String getTerritoryId() {
		return this.territoryId;
	}

	@Override
	public String toString() {
		return "[employeeId=" + this.employeeId + ", territoryId=" + this.territoryId + "]";
	}

	@Override
	public void readExternal(PofReader reader) throws IOException {
		int i = super.readExternal(0, reader);
		this.employeeId = reader.readString(i++);
		this.territoryId = reader.readString(i++);
	}

	@Override
	public void writeExternal(PofWriter writer) throws IOException {
		int i = super.writeExternal(0, writer);
		writer.writeString(i++, employeeId);
		writer.writeString(i++, territoryId);
	}
}
