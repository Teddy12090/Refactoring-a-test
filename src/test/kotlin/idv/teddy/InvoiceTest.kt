package idv.teddy

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal


class InvoiceTest {
    @Test
    fun testAddItemQuantity_severalQuantity() {
        lateinit var billingAddress: Address
        lateinit var shippingAddress: Address
        lateinit var customer: Customer
        lateinit var product: Product
        lateinit var invoice: Invoice
        try {
            // Set up fixture
            billingAddress = Address("1222 1st St SW", "Calgary", "Alberta", "T2N 2V2", "Canada")
            shippingAddress = Address("1333 1st St SW", "Calgary", "Alberta", "T2N 2V2", "Canada")
            customer = Customer(99, "John", "Doe", BigDecimal("30"), billingAddress, shippingAddress)
            product = Product(88, "SomeWidget", BigDecimal("19.99"))
            invoice = Invoice(customer)

            // Exercise SUT
            invoice.addItemQuantity(product, 5)

            // Verify outcome
            val lineItems: List<LineItem> = invoice.getLineItems()
            if (lineItems.size == 1) {
                val actItem: LineItem = lineItems[0]
                assertEquals(invoice, actItem.getInvoice(), "invoice")
                assertEquals(product, actItem.getProduct(), "product")
                assertEquals(5, actItem.getQuantity(), "quantity")
                assertEquals(BigDecimal("30"), actItem.getPercentDiscount(), "discount")
                assertEquals(BigDecimal("19.99"), actItem.getUnitPrice(), "unit price")
                assertEquals(BigDecimal("69.96"), actItem.getExtendedPrice(), "extended price")
            } else {
                assertTrue(false, "Invoice should have 1 item")
            }
        } finally {
            // Teardown
            deleteObject(invoice)
            deleteObject(product)
            deleteObject(customer)
            deleteObject(billingAddress)
            deleteObject(shippingAddress)
        }
    }

    private fun deleteObject(@Suppress("UNUSED_PARAMETER") obj: Any?) {
    }
}
