package idv.teddy

import java.math.BigDecimal
import java.math.RoundingMode

class LineItem(private val invoice: Invoice, private val product: Product, private val quantity: Int) {
    fun getInv(): Invoice {
        return invoice
    }

    fun getProd(): Product {
        return product
    }

    fun getQuantity(): Int {
        return quantity
    }

    fun getPercentDiscount(): BigDecimal {
        return invoice.customer.discount
    }

    fun getUnitPrice(): BigDecimal {
        return product.price
    }

    fun getExtendedPrice(): BigDecimal {
        val originalPrice = getUnitPrice().multiply(BigDecimal.valueOf(getQuantity().toLong()))
        val discount = BigDecimal.valueOf(100).minus(getPercentDiscount()).divide(BigDecimal.valueOf(100))
        return originalPrice.multiply(discount).setScale(2, RoundingMode.DOWN)
    }

}
