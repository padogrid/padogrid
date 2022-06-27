package org.apache.geode.addon.demo.nw.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.geode.pdx.PdxReader;
import org.apache.geode.pdx.PdxSerializable;
import org.apache.geode.pdx.PdxWriter;

@Entity
@Table(name = "employee_territories")
public class EmployeeTerritory extends BaseEntity implements PdxSerializable
{
	@Id
	private String employeeId;
	@Column(length = 30)
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
	public String toString()
	{
		return "[employeeId=" + this.employeeId
			 + ", territoryId=" + this.territoryId + "]";
	}

	@Override
	public void toData(PdxWriter writer) {
		super.toData(writer);
		writer.writeString("employeeId", employeeId).markIdentityField("employeeId");
		writer.writeString("territoryId", territoryId);
	}

	@Override
	public void fromData(PdxReader reader) {
		super.fromData(reader);
		this.employeeId = reader.readString("employeeId");
		this.territoryId = reader.readString("territoryId");
	}
}
