package idv.teddy

import java.math.BigDecimal

data class Customer(val id: Int, val firstName: String, val secondName: String, val discount: BigDecimal, val billingAddress: Address, val shippingAddress: Address) {

}
