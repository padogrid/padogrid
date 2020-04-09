package org.apache.geode.addon.demo.nw.data;

import org.apache.geode.pdx.PdxReader;
import org.apache.geode.pdx.PdxSerializable;
import org.apache.geode.pdx.PdxWriter;

public class Shipper implements PdxSerializable
{
	private String shipperId;
	private String companyName;
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
		writer.writeString("shipperId", shipperId);
		writer.writeString("companyName", companyName);
		writer.writeString("phone", phone);
	}

	@Override
	public void fromData(PdxReader reader) {
		this.shipperId = reader.readString("shipperId");
		this.companyName = reader.readString("companyName");
		this.phone = reader.readString("phone");
	}
}
