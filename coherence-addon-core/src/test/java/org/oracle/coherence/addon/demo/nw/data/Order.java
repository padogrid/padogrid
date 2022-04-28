package org.oracle.coherence.addon.demo.nw.data;

import java.io.IOException;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.tangosol.internal.sleepycat.persist.model.Entity;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import nonapi.io.github.classgraph.json.Id;

@Entity
@Table(name = "orders")
public class Order extends BaseEntity implements PortableObject, Comparable<Order> {
	@Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(length = 20)
	private String orderId;
	@Column(length = 20)
	private String customerId;
	@Column(length = 20)
	private String employeeId;
	@Column
	@Temporal(TemporalType.DATE)
	private Date orderDate;
	@Column
	@Temporal(TemporalType.DATE)
	private Date requiredDate;
	@Column
	@Temporal(TemporalType.DATE)
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

	public Order() {
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getOrderId() {
		return this.orderId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public String getCustomerId() {
		return this.customerId;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}

	public String getEmployeeId() {
		return this.employeeId;
	}

	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}

	public Date getOrderDate() {
		return this.orderDate;
	}

	public void setRequiredDate(Date requiredDate) {
		this.requiredDate = requiredDate;
	}

	public Date getRequiredDate() {
		return this.requiredDate;
	}

	public void setShippedDate(Date shippedDate) {
		this.shippedDate = shippedDate;
	}

	public Date getShippedDate() {
		return this.shippedDate;
	}

	public void setShipVia(String shipVia) {
		this.shipVia = shipVia;
	}

	public String getShipVia() {
		return this.shipVia;
	}

	public void setFreight(double freight) {
		this.freight = freight;
	}

	public double getFreight() {
		return this.freight;
	}

	public void setShipName(String shipName) {
		this.shipName = shipName;
	}

	public String getShipName() {
		return this.shipName;
	}

	public void setShipAddress(String shipAddress) {
		this.shipAddress = shipAddress;
	}

	public String getShipAddress() {
		return this.shipAddress;
	}

	public void setShipCity(String shipCity) {
		this.shipCity = shipCity;
	}

	public String getShipCity() {
		return this.shipCity;
	}

	public void setShipRegion(String shipRegion) {
		this.shipRegion = shipRegion;
	}

	public String getShipRegion() {
		return this.shipRegion;
	}

	public void setShipPostalCode(String shipPostalCode) {
		this.shipPostalCode = shipPostalCode;
	}

	public String getShipPostalCode() {
		return this.shipPostalCode;
	}

	public void setShipCountry(String shipCountry) {
		this.shipCountry = shipCountry;
	}

	public String getShipCountry() {
		return this.shipCountry;
	}

	@Override
	public String toString() {
		return "[customerId=" + this.customerId + ", employeeId=" + this.employeeId + ", freight=" + this.freight
				+ ", orderDate=" + this.orderDate + ", orderId=" + this.orderId + ", requiredDate=" + this.requiredDate
				+ ", shipAddress=" + this.shipAddress + ", shipCity=" + this.shipCity + ", shipCountry="
				+ this.shipCountry + ", shipName=" + this.shipName + ", shipPostalCode=" + this.shipPostalCode
				+ ", shipRegion=" + this.shipRegion + ", shipVia=" + this.shipVia + ", shippedDate=" + this.shippedDate
				+ "]";
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

	@Override
	public void readExternal(PofReader reader) throws IOException {
		int i = super.readExternal(0, reader);
		this.orderId = reader.readString(i++);
		this.customerId = reader.readString(i++);
		this.employeeId = reader.readString(i++);
		this.orderDate = reader.readDate(i++);
		this.requiredDate = reader.readDate(i++);
		this.shippedDate = reader.readDate(i++);
		this.shipVia = reader.readString(i++);
		this.freight = reader.readDouble(i++);
		this.shipName = reader.readString(i++);
		this.shipAddress = reader.readString(i++);
		this.shipCity = reader.readString(i++);
		this.shipRegion = reader.readString(i++);
		this.shipPostalCode = reader.readString(i++);
		this.shipCountry = reader.readString(i++);
	}

	@Override
	public void writeExternal(PofWriter writer) throws IOException {
		int i = super.writeExternal(0, writer);
		writer.writeString(i++, orderId);
		writer.writeString(i++, customerId);
		writer.writeString(i++, employeeId);

		// date object type could be other than Date if
		// it is extracted from the database. writeDate() fails
		// if it is a subclass of Date.
		if (orderDate.getClass() == Date.class) {
			writer.writeDate(i++, orderDate);
			writer.writeDate(i++, requiredDate);
			writer.writeDate(i++, shippedDate);
		} else {
			writer.writeDate(i++, new Date(orderDate.getTime()));
			writer.writeDate(i++, new Date(requiredDate.getTime()));
			writer.writeDate(i++, new Date(shippedDate.getTime()));
		}

		writer.writeString(i++, shipVia);
		writer.writeDouble(i++, freight);
		writer.writeString(i++, shipName);
		writer.writeString(i++, shipAddress);
		writer.writeString(i++, shipCity);
		writer.writeString(i++, shipRegion);
		writer.writeString(i++, shipPostalCode);
		writer.writeString(i++, shipCountry);
	}
}
