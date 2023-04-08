package idv.teddy

import io.mockk.every
import io.mockk.mockkObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal


class InvoiceTest {
    @Test
    fun testAddItemQuantity_severalQuantity_v1() {
        lateinit var billingAddress: Address
        lateinit var shippingAddress: Address
        lateinit var customer: Customer
        lateinit var product: Product
        lateinit var invoice: Invoice
        try {
            // Set up fixture
            billingAddress = Address("1222 1st St SW", "Calgary", "Alberta", "T2N 2V2", "Canada")
            shippingAddress = Address("1333  1st  St  SW", "Calgary", "Alberta", "T2N  2V2", "Canada")
            customer = Customer(99, "John", "Doe", BigDecimal("30"), billingAddress, shippingAddress)
            product = Product(88, "SomeWidget", BigDecimal("19.99"))
            invoice = Invoice(customer)

            // Exercise SUT
            invoice.addItemQuantity(product, 5)

            // Verify outcome
            val lineItems: List<LineItem> = invoice.getLineItems()
            if (lineItems.size == 1) {
                val expected = LineItem(invoice, product, 5)
                mockkObject(expected)
                every { expected.getPercentDiscount() } returns BigDecimal("30")
                every { expected.getExtendedPrice() } returns BigDecimal("69.96")
                val actItem: LineItem = lineItems[0]
                assertEquals(expected.getInv(), actItem.getInv(), "invoice")
                assertEquals(expected.getProd(), actItem.getProd(), "product")
                assertEquals(expected.getQuantity(), actItem.getQuantity(), "quantity")
                assertEquals(expected.getPercentDiscount(), actItem.getPercentDiscount(), "discount")
                assertEquals(expected.getUnitPrice(), actItem.getUnitPrice(), "unit price")
                assertEquals(expected.getExtendedPrice(), actItem.getExtendedPrice(), "extended price")
            } else {
                fail("Invoice should have exactly one line item")
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

    private fun fail(@Suppress("SameParameterValue") msg: String) {
        assertTrue(false, msg)
    }

    private fun deleteObject(@Suppress("UNUSED_PARAMETER") obj: Any?) {
    }
}
