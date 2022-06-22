package org.redis.demo.nw.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
  * Order is generated code. To modify this class, you must follow the
  * guidelines below.
  * <ul>
  * <li>Always add new fields and do NOT delete old fields.</li>
  * <li>If new fields have been added, then make sure to increment the version number.</li>
  * </ul>
  *
  * @generator com.netcrest.pado.tools.hazelcast.VersionedPortableClassGenerator
  * @schema orders.schema
  * @date Fri May 17 20:50:06 EDT 2019
**/
@Entity
@Table(name = "orders")
public class Order extends BaseEntity implements Externalizable, Comparable<Order>
{
	@Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(length = 20)
	private String orderId;
	@Column(length = 20)
	private String customerId;
	@Column(length = 20)
	private String employeeId;
	@Column
	private Date orderDate;
	@Column
	private Date requiredDate;
	@Column
	private Date shippedDate;
	@Column(length = 50)
	private String shipVia;
	@Column
	private double freight;
	@Column(length = 50)
	private String shipName;
	@Column(length = 100)
	private String shipAddress;
	@Column(length = 50)
	private String shipCity;
	@Column(length = 10)
	private String shipRegion;
	@Column(length = 10)
	private String shipPostalCode;
	@Column(length = 100)
	private String shipCountry;

	public Order()
	{
	}

	public void setOrderId(String orderId) {
		this.orderId=orderId;
	}

	public String getOrderId() {
		return this.orderId;
	}

	public void setCustomerId(String customerId) {
		this.customerId=customerId;
	}

	public String getCustomerId() {
		return this.customerId;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId=employeeId;
	}

	public String getEmployeeId() {
		return this.employeeId;
	}

	public void setOrderDate(Date orderDate) {
		this.orderDate=orderDate;
	}

	public Date getOrderDate() {
		return this.orderDate;
	}

	public void setRequiredDate(Date requiredDate) {
		this.requiredDate=requiredDate;
	}

	public Date getRequiredDate() {
		return this.requiredDate;
	}

	public void setShippedDate(Date shippedDate) {
		this.shippedDate=shippedDate;
	}

	public Date getShippedDate() {
		return this.shippedDate;
	}

	public void setShipVia(String shipVia) {
		this.shipVia=shipVia;
	}

	public String getShipVia() {
		return this.shipVia;
	}

	public void setFreight(double freight) {
		this.freight=freight;
	}

	public double getFreight() {
		return this.freight;
	}

	public void setShipName(String shipName) {
		this.shipName=shipName;
	}

	public String getShipName() {
		return this.shipName;
	}

	public void setShipAddress(String shipAddress) {
		this.shipAddress=shipAddress;
	}

	public String getShipAddress() {
		return this.shipAddress;
	}

	public void setShipCity(String shipCity) {
		this.shipCity=shipCity;
	}

	public String getShipCity() {
		return this.shipCity;
	}

	public void setShipRegion(String shipRegion) {
		this.shipRegion=shipRegion;
	}

	public String getShipRegion() {
		return this.shipRegion;
	}

	public void setShipPostalCode(String shipPostalCode) {
		this.shipPostalCode=shipPostalCode;
	}

	public String getShipPostalCode() {
		return this.shipPostalCode;
	}

	public void setShipCountry(String shipCountry) {
		this.shipCountry=shipCountry;
	}

	public String getShipCountry() {
		return this.shipCountry;
	}
	
	/**
	 * Writes the state of this object to the given <code>ObjectOutput</code>.
	 */
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeUTF(orderId);
		out.writeUTF(customerId);
		out.writeUTF(employeeId);
		if (this.orderDate == null) {
			out.writeLong(-1L);
		} else {
			out.writeLong(this.orderDate.getTime());
		}
		if (this.requiredDate == null) {
			out.writeLong(-1L);
		} else {
			out.writeLong(this.requiredDate.getTime());
		}
		if (this.shippedDate == null) {
			out.writeLong(-1L);
		} else {
			out.writeLong(this.shippedDate.getTime());
		}
		out.writeUTF(shipVia);
		out.writeDouble(freight);
		out.writeUTF(shipName);
		out.writeUTF(shipAddress);
		out.writeUTF(shipCity);
		out.writeUTF(shipRegion);
		out.writeUTF(shipPostalCode);
		out.writeUTF(shipCountry);
	}

	/**
	 * Reads the state of this object from the given <code>ObjectInput</code>.
	 */
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		this.orderId = in.readUTF();
		this.customerId = in.readUTF();
		this.employeeId = in.readUTF();
		long l = in.readLong();
		if (l != -1L) {
			this.orderDate = new Date(l);
		}
		l = in.readLong();
		if (l != -1L) {
			this.requiredDate = new Date(l);
		}
		l = in.readLong();
		if (l != -1L) {
			this.shippedDate = new Date(l);
		}
		this.shipVia = in.readUTF();
		this.freight = in.readDouble();
		this.shipName = in.readUTF();
		this.shipAddress = in.readUTF();
		this.shipCity = in.readUTF();
		this.shipRegion = in.readUTF();
		this.shipPostalCode = in.readUTF();
		this.shipCountry = in.readUTF();
	}
    
	@Override
	public String toString()
	{
		return "[customerId=" + this.customerId
			 + ", employeeId=" + this.employeeId
			 + ", freight=" + this.freight
			 + ", orderDate=" + this.orderDate
			 + ", orderId=" + this.orderId
			 + ", requiredDate=" + this.requiredDate
			 + ", shipAddress=" + this.shipAddress
			 + ", shipCity=" + this.shipCity
			 + ", shipCountry=" + this.shipCountry
			 + ", shipName=" + this.shipName
			 + ", shipPostalCode=" + this.shipPostalCode
			 + ", shipRegion=" + this.shipRegion
			 + ", shipVia=" + this.shipVia
			 + ", shippedDate=" + this.shippedDate + "]";
	}

	@Override
	public int compareTo(Order o) {
		// Order
		if (o == null) {
			return -1;
		}
		// customerId
		if (this.customerId == null || o.customerId == null) {
			return -1;
		}
		int c = this.customerId.compareTo(o.customerId);
		if (c != 0) {
			return c;
		}
		// orderDate
		if (this.orderDate == null || o.orderDate == null) {
			return -1;
		}
		c = this.orderDate.compareTo(o.orderDate);
		if (c != 0) {
			return c;
		}
		// shippedDate
		if (this.shippedDate == null || o.shippedDate == null) {
			return -1;
		}
		c = this.shippedDate.compareTo(o.shippedDate);
		if (c != 0) {
			return c;
		}
		return 0;
	}
}
