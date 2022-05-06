package org.hazelcast.demo.nw.data;

import java.io.IOException;


import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.nio.serialization.VersionedPortable;

/**
  * OrderDetail is generated code. To modify this class, you must follow the
  * guidelines below.
  * <ul>
  * <li>Always add new fields and do NOT delete old fields.</li>
  * <li>If new fields have been added, then make sure to increment the version number.</li>
  * </ul>
  *
  * @generator com.netcrest.pado.tools.hazelcast.VersionedPortableClassGenerator
  * @schema order_details.schema
  * @date Fri May 17 20:50:06 EDT 2019
**/
public class OrderDetail extends BaseEntity implements VersionedPortable
{
	private String orderId;
	private String productId;
	private double unitPrice;
	private int quantity;
	private double discount;

	public OrderDetail()
	{
	}

	public void setOrderId(String orderId) {
		this.orderId=orderId;
	}

	public String getOrderId() {
		return this.orderId;
	}

	public void setProductId(String productId) {
		this.productId=productId;
	}

	public String getProductId() {
		return this.productId;
	}

	public void setUnitPrice(double unitPrice) {
		this.unitPrice=unitPrice;
	}

	public double getUnitPrice() {
		return this.unitPrice;
	}

	public void setQuantity(int quantity) {
		this.quantity=quantity;
	}

	public int getQuantity() {
		return this.quantity;
	}

	public void setDiscount(double discount) {
		this.discount=discount;
	}

	public double getDiscount() {
		return this.discount;
	}


	@Override
	public int getClassId() 
	{
		return PortableFactoryImpl.OrderDetail_CLASS_ID;
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
		writer.writeUTF("orderId", orderId);
		writer.writeUTF("productId", productId);
		writer.writeDouble("unitPrice", unitPrice);
		writer.writeInt("quantity", quantity);
		writer.writeDouble("discount", discount);
	}

	@Override
	public void readPortable(PortableReader reader) throws IOException {
		super.readPortable(reader);
		this.orderId = reader.readUTF("orderId");
		this.productId = reader.readUTF("productId");
		this.unitPrice = reader.readDouble("unitPrice");
		this.quantity = reader.readInt("quantity");
		this.discount = reader.readDouble("discount");
	}
    
	@Override
	public String toString()
	{
		return "[discount=" + this.discount
			 + ", orderId=" + this.orderId
			 + ", productId=" + this.productId
			 + ", quantity=" + this.quantity
			 + ", unitPrice=" + this.unitPrice + "]";
	}
}
