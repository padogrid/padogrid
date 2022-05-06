package org.apache.geode.addon.demo.nw.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.geode.pdx.PdxReader;
import org.apache.geode.pdx.PdxSerializable;
import org.apache.geode.pdx.PdxWriter;

@Entity
@Table(name = "shippers")
public class Shipper extends BaseEntity implements PdxSerializable
{
	@Id
	private String shipperId;
	@Column(length = 50)
	private String companyName;
	@Column(length = 30)
	private String phone;

	public Shipper()
	{
	}

	public void setShipperId(String shipperId) {
		this.shipperId=shipperId;
	}

	public String getShipperId() {
		return this.shipperId;
	}

	public void setCompanyName(String companyName) {
		this.companyName=companyName;
	}

	public String getCompanyName() {
		return this.companyName;
	}

	public void setPhone(String phone) {
		this.phone=phone;
	}

	public String getPhone() {
		return this.phone;
	}

	@Override
	public String toString()
	{
		return "[companyName=" + this.companyName
			 + ", phone=" + this.phone
			 + ", shipperId=" + this.shipperId + "]";
	}

	@Override
	public void toData(PdxWriter writer) {
		super.toData(writer);
		writer.writeString("shipperId", shipperId);
		writer.writeString("companyName", companyName);
		writer.writeString("phone", phone);
	}

	@Override
	public void fromData(PdxReader reader) {
		super.fromData(reader);
		this.shipperId = reader.readString("shipperId");
		this.companyName = reader.readString("companyName");
		this.phone = reader.readString("phone");
	}
}
