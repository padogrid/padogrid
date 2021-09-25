package org.hazelcast.demo.nw.data;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;

/**
 * PortableFactoryImpl is generated code. To manually modified the code, make sure
 * to follow the same naming conventions. Otherwise, the code generator may not work.
 
 * @generator com.netcrest.pado.tools.hazelcast.VersionedPortableClassGenerator
 * @date Fri May 17 20:50:06 EDT 2019
 */
public class PortableFactoryImpl implements PortableFactory {
	public static final int FACTORY_ID = 1;

	static final int __FIRST_CLASS_ID = 100;
	static final int OrderDetail_CLASS_ID = __FIRST_CLASS_ID;
	static final int Customer_CLASS_ID = OrderDetail_CLASS_ID + 1;
	static final int Employee_CLASS_ID = Customer_CLASS_ID + 1;
	static final int Product_CLASS_ID = Employee_CLASS_ID + 1;
	static final int Territory_CLASS_ID = Product_CLASS_ID + 1;
	static final int Category_CLASS_ID = Territory_CLASS_ID + 1;
	static final int EmployeeTerritory_CLASS_ID = Category_CLASS_ID + 1;
	static final int Supplier_CLASS_ID = EmployeeTerritory_CLASS_ID + 1;
	static final int Shipper_CLASS_ID = Supplier_CLASS_ID + 1;
	static final int Order_CLASS_ID = Shipper_CLASS_ID + 1;
	static final int Region_CLASS_ID = Order_CLASS_ID + 1;
	static final int __LAST_CLASS_ID = Region_CLASS_ID;

	public Portable create(int classId) {
		switch (classId) {
		case OrderDetail_CLASS_ID:
			return new OrderDetail();
		case Customer_CLASS_ID:
			return new Customer();
		case Employee_CLASS_ID:
			return new Employee();
		case Product_CLASS_ID:
			return new Product();
		case Territory_CLASS_ID:
			return new Territory();
		case Category_CLASS_ID:
			return new Category();
		case EmployeeTerritory_CLASS_ID:
			return new EmployeeTerritory();
		case Supplier_CLASS_ID:
			return new Supplier();
		case Shipper_CLASS_ID:
			return new Shipper();
		case Order_CLASS_ID:
			return new Order();
		case Region_CLASS_ID:
			return new Region();
		default:
			return null;
		}
	}
}
