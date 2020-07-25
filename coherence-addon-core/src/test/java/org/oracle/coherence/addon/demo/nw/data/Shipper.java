package org.oracle.coherence.addon.demo.nw.data;

import java.io.IOException;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

public class Shipper implements PortableObject
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
	public void readExternal(PofReader reader) throws IOException {
		int i = 0;
		this.shipperId = reader.readString(i++);
		this.companyName = reader.readString(i++);
		this.phone = reader.readString(i++);
	}

	@Override
	public void writeExternal(PofWriter writer) throws IOException {
		int i = 0;
		writer.writeString(i++, shipperId);
		writer.writeString(i++, companyName);
		writer.writeString(i++, phone);
	}
}
