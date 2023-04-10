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


    private val unitPrice = BigDecimal("19.99")
    private val percentDiscount = BigDecimal("30")
    private lateinit var testObjects: MutableList<Any>
    private lateinit var customer: Customer
    private lateinit var product: Product

    @BeforeEach
    fun setUp() {
        testObjects = ArrayList()
        customer = createACustomer(this.percentDiscount)
        product = createProduct(this.unitPrice)
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
        // setup fixture
        val quantity = 5
        val invoice = createInvoice(this.customer)

        // Exercise SUT
        invoice.addItemQuantity(this.product, quantity)

        // Verify outcome
        val basePrice = this.unitPrice.multiply(BigDecimal.valueOf(quantity.toLong()))
        val extendedPrice = basePrice.subtract(basePrice.multiply(this.percentDiscount.movePointLeft(2))).setScale(2, RoundingMode.DOWN)
        val expected = createLineItem(invoice, quantity, extendedPrice)
        assertContainsExactlyOneLineItem(invoice, expected)
    }

    @Test
    fun testAddItemQuantity_oneQuantity() {
        // setup fixture
        val quantity = 1
        val invoice = createInvoice(this.customer)

        // Exercise SUT
        invoice.addItemQuantity(this.product, quantity)

        // Verify outcome
        val basePrice = this.unitPrice.multiply(BigDecimal.valueOf(quantity.toLong()))
        val extendedPrice = basePrice.subtract(basePrice.multiply(this.percentDiscount.movePointLeft(2))).setScale(2, RoundingMode.DOWN)
        val expected = createLineItem(invoice, quantity, extendedPrice)
        assertContainsExactlyOneLineItem(invoice, expected)
    }

    @Test
    fun testChangeQuantity_severalQuantity() {
        // setup fixture
        val originalQuantity = 3
        val newQuantity = 5
        val invoice = createInvoice(this.customer)
        invoice.addItemQuantity(this.product, originalQuantity)

        // Exercise SUT
        invoice.changeQuantityForProduct(this.product, newQuantity)

        // Verify outcome
        val basePrice = this.unitPrice.multiply(BigDecimal.valueOf(newQuantity.toLong()))
        val extendedPrice = basePrice.subtract(basePrice.multiply(this.percentDiscount.movePointLeft(2))).setScale(2, RoundingMode.DOWN)
        val expected = createLineItem(invoice, newQuantity, extendedPrice)
        assertContainsExactlyOneLineItem(invoice, expected)
    }

    private fun createLineItem(invoice: Invoice, quantity: Int, extendedPrice: BigDecimal): LineItem {
        val expected = LineItem(invoice, this.product, quantity)
        mockkObject(expected)
        every { expected.getPercentDiscount() } returns this.percentDiscount
        every { expected.getExtendedPrice() } returns extendedPrice
        return expected
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
