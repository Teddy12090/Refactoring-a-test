package idv.teddy

import io.mockk.every
import io.mockk.mockkObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal


class InvoiceTest {


    private lateinit var testObjects: MutableList<Any>

    @BeforeEach
    fun setUp() {
        testObjects = ArrayList()
    }

    @AfterEach
    fun tearDown() {
        val i = testObjects.iterator()
        while (i.hasNext()) {
            try {
                deleteObject(i.next())
            } catch (e: Exception) {
                // Nothing to do; we just want to make sure we continue on to the next object in the list
            }
        }
    }

    @Test
    fun testAddItemQuantity_severalQuantity_v1() {
        val billingAddress = Address("1222 1st St SW", "Calgary", "Alberta", "T2N 2V2", "Canada")
        registerTestObjects(billingAddress)
        val shippingAddress = Address("1333  1st  St  SW", "Calgary", "Alberta", "T2N  2V2", "Canada")
        registerTestObjects(shippingAddress)
        val customer = Customer(99, "John", "Doe", BigDecimal("30"), billingAddress, shippingAddress)
        registerTestObjects(customer)
        val product = Product(88, "SomeWidget", BigDecimal("19.99"))
        registerTestObjects(product)
        val invoice = Invoice(customer)
        registerTestObjects(invoice)

        // Exercise SUT
        invoice.addItemQuantity(product, 5)

        // Verify outcome
        val expected = LineItem(invoice, product, 5)
        mockkObject(expected)
        every { expected.getPercentDiscount() } returns BigDecimal("30")
        every { expected.getExtendedPrice() } returns BigDecimal("69.96")
        assertContainsExactlyOneLineItem(invoice, expected)
    }

    private fun assertContainsExactlyOneLineItem(invoice: Invoice, expected: LineItem) {
        val lineItems: List<LineItem> = invoice.getLineItems()
        assertEquals(1, lineItems.size)
        val actItem: LineItem = lineItems[0]
        assertEquals(expected.getInv(), actItem.getInv(), "invoice")
        assertEquals(expected.getProd(), actItem.getProd(), "product")
        assertEquals(expected.getQuantity(), actItem.getQuantity(), "quantity")
        assertEquals(expected.getPercentDiscount(), actItem.getPercentDiscount(), "discount")
        assertEquals(expected.getUnitPrice(), actItem.getUnitPrice(), "unit price")
        assertEquals(expected.getExtendedPrice(), actItem.getExtendedPrice(), "extended price")
    }

    private fun deleteObject(@Suppress("UNUSED_PARAMETER") obj: Any?) {
    }

    private fun registerTestObjects(testObject: Any) {
        testObjects.add(testObject)
    }

}
