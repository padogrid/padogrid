package org.redisson.demo.nw.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

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
public class OrderDetail extends BaseEntity implements Externalizable
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
	
	/**
	 * Writes the state of this object to the given <code>ObjectOutput</code>.
	 */
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeUTF(orderId);
		out.writeUTF(productId);
		out.writeDouble(unitPrice);
		out.writeInt(quantity);
		out.writeDouble(discount);
	}

	/**
	 * Reads the state of this object from the given <code>ObjectInput</code>.
	 */
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		this.orderId = in.readUTF();
		this.productId = in.readUTF();
		this.unitPrice = in.readDouble();
		this.quantity = in.readInt();
		this.discount = in.readDouble();
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
