package idv.teddy

import com.github.javafaker.Faker
import io.mockk.every
import io.mockk.mockkObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID
import kotlin.math.absoluteValue


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
        val quantity = 5
        val unitPrice = BigDecimal("19.99")
        val percentDiscount = BigDecimal("30")
        val customer = createACustomer(percentDiscount)
        val product = createProduct(unitPrice)
        val invoice = createInvoice(customer)

        // Exercise SUT
        invoice.addItemQuantity(product, quantity)

        // Verify outcome
        val basePrice = unitPrice.multiply(BigDecimal.valueOf(quantity.toLong()))
        val extendedPrice = basePrice.subtract(basePrice.multiply(percentDiscount.movePointLeft(2))).setScale(2, RoundingMode.DOWN)
        val expected = LineItem(invoice, product, quantity)
        mockkObject(expected)
        every { expected.getPercentDiscount() } returns percentDiscount
        every { expected.getExtendedPrice() } returns extendedPrice
        assertContainsExactlyOneLineItem(invoice, expected)
    }

    private fun createACustomer(percentDiscount: BigDecimal): Customer {
        val billingAddress = createAddress()
        val shippingAddress = createAddress()
        val customer = Customer(getUniqueNumber(), "John", "Doe", percentDiscount, billingAddress, shippingAddress)
        registerTestObjects(customer)
        return customer
    }

    private fun createInvoice(customer: Customer): Invoice {
        val invoice = Invoice(customer)
        registerTestObjects(invoice)
        return invoice
    }

    private fun createProduct(unitPrice: BigDecimal): Product {
        val product = Product(getUniqueNumber(), "SomeWidget", unitPrice)
        registerTestObjects(product)
        return product
    }

    private fun getUniqueNumber() = UUID.randomUUID().hashCode().absoluteValue

    private fun createAddress(): Address {
        val faker = Faker()
        val addressFaker = faker.address()
        val address = Address(addressFaker.fullAddress(), addressFaker.city(), addressFaker.state(), addressFaker.zipCode(), addressFaker.country())
        registerTestObjects(address)
        return address
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
