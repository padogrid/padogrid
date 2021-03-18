package org.apache.geode.addon.demo.nw.data;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.geode.pdx.PdxReader;
import org.apache.geode.pdx.PdxSerializable;
import org.apache.geode.pdx.PdxWriter;

@Entity
@Table(name = "orders")
public class Order implements PdxSerializable, Comparable<Order>
{
	/**
	 * Source time factor. Date long values are divided by this number. 
	 */
	private static int TIME_FACTOR = Integer.getInteger("padogrid.data.time.factor", 1);
	
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
	
	public void setOrderDateTime(long orderDateTime) {
		this.orderDate = new Date(orderDateTime/TIME_FACTOR);
	}

	public Date getOrderDate() {
		return this.orderDate;
	}

	public void setRequiredDate(Date requiredDate) {
		this.requiredDate=requiredDate;
	}
	
	public void setRequredDateTime(long requiredDateTime) {
		this.requiredDate = new Date(requiredDateTime/TIME_FACTOR);
	}

	public Date getRequiredDate() {
		return this.requiredDate;
	}

	public void setShippedDate(Date shippedDate) {
		this.shippedDate=shippedDate;
	}
	
	public void setShippedDateTime(long shippedDateTime) {
		this.shippedDate = new Date(shippedDateTime/TIME_FACTOR);
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

	@Override
	public void toData(PdxWriter writer) {
		writer.writeString("orderId", orderId);
		writer.writeString("customerId", customerId);
		writer.writeString("employeeId", employeeId);
		
		// date object type could be other than Date if
		// it is extracted from the database. writeDate() fails
		// if it is a subclass of Date.
		if (orderDate.getClass() == Date.class) {
			writer.writeDate("orderDate", orderDate);
			writer.writeDate("requiredDate", requiredDate);
			writer.writeDate("shippedDate", shippedDate);
		} else {
			writer.writeDate("orderDate", new Date(orderDate.getTime()));
			writer.writeDate("requiredDate", new Date(requiredDate.getTime()));
			writer.writeDate("shippedDate", new Date(shippedDate.getTime()));
		}
		
		writer.writeString("shipVia", shipVia);
		writer.writeDouble("freight", freight);
		writer.writeString("shipName", shipName);
		writer.writeString("shipAddress", shipAddress);
		writer.writeString("shipCity", shipCity);
		writer.writeString("shipRegion", shipRegion);
		writer.writeString("shipPostalCode", shipPostalCode);
		writer.writeString("shipCountry", shipCountry);
	}

	@Override
	public void fromData(PdxReader reader) {
		this.orderId = reader.readString("orderId");
		this.customerId = reader.readString("customerId");
		this.employeeId = reader.readString("employeeId");
		this.orderDate = reader.readDate("orderDate");
		this.requiredDate = reader.readDate("requiredDate");
		this.shippedDate = reader.readDate("shippedDate");
		this.shipVia = reader.readString("shipVia");
		this.freight = reader.readDouble("freight");
		this.shipName = reader.readString("shipName");
		this.shipAddress = reader.readString("shipAddress");
		this.shipCity = reader.readString("shipCity");
		this.shipRegion = reader.readString("shipRegion");
		this.shipPostalCode = reader.readString("shipPostalCode");
		this.shipCountry = reader.readString("shipCountry");
	}
}
