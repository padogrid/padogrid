package org.hazelcast.demo.nw.data;

import java.io.IOException;


import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.nio.serialization.VersionedPortable;

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
public class Shipper extends BaseEntity implements VersionedPortable
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
	public int getClassId() 
	{
		return PortableFactoryImpl.Shipper_CLASS_ID;
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
		writer.writeUTF("shipperId", shipperId);
		writer.writeUTF("companyName", companyName);
		writer.writeUTF("phone", phone);
	}

	@Override
	public void readPortable(PortableReader reader) throws IOException {
		super.readPortable(reader);
		this.shipperId = reader.readUTF("shipperId");
		this.companyName = reader.readUTF("companyName");
		this.phone = reader.readUTF("phone");
	}
    
	@Override
	public String toString()
	{
		return "[companyName=" + this.companyName
			 + ", phone=" + this.phone
			 + ", shipperId=" + this.shipperId + "]";
	}
}
