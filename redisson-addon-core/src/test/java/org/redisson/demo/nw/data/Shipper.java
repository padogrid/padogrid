package org.redisson.demo.nw.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
  * Shipper is generated code. To modify this class, you must follow the
  * guidelines below.
  * <ul>
  * <li>Always add new fields and do NOT delete old fields.</li>
  * <li>If new fields have been added, then make sure to increment the version number.</li>
  * </ul>
  *
  * @generator com.netcrest.pado.tools.hazelcast.VersionedPortableClassGenerator
  * @schema shippers.schema
  * @date Fri May 17 20:50:06 EDT 2019
**/
public class Shipper extends BaseEntity implements Externalizable
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
    
	/**
	 * Writes the state of this object to the given <code>ObjectOutput</code>.
	 */
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeUTF(shipperId);
		out.writeUTF(companyName);
		out.writeUTF(phone);
	}

	/**
	 * Reads the state of this object from the given <code>ObjectInput</code>.
	 */
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		this.shipperId = in.readUTF();
		this.companyName = in.readUTF();
		this.phone = in.readUTF();
	}
	@Override
	public String toString()
	{
		return "[companyName=" + this.companyName
			 + ", phone=" + this.phone
			 + ", shipperId=" + this.shipperId + "]";
	}
}
